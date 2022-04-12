#include <sys/ioctl.h>
#include <sys/poll.h>
#include <cstdio>
#include <cstdlib>
#include <fcntl.h>
#include <cstring>
#include <unistd.h>
#include <cerrno>
#include <iostream>
#include "s610_radio.h"
#include "v4l2_api.h"
#include <algorithm>
#include <jni.h>
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/fmradio/1.2/IFMRadio.h>


using android::sp;
using vendor::eureka::hardware::fmradio::V1_0::Direction;
using vendor::eureka::hardware::fmradio::V1_1::Status;
using vendor::eureka::hardware::fmradio::V1_2::IFMRadio;

// #define DEBUG
#define TRACK_SIZE 30
long tracks[TRACK_SIZE] = {0};
bool FMThread = false;

int open_fm_device() {
  int fd;
  if ((fd = open("/dev/radio0", O_RDWR)) < 0) {
    printf("Cannot open /dev/radio0.\n");
    return -1;
  }
  return fd;
}
static int fm_radio_get_frequency(int fd, long *channel) {
  struct v4l2_frequency freq {};
  int ret;

  freq.tuner = 0;
  freq.type = V4L2_TUNER_RADIO;

  ret = ioctl(fd, VIDIOC_G_FREQUENCY, &freq);
  if (ret < 0) {
    printf("FmRadioController: failed to get frequency\n");
    return FM_FAILURE;
  }

  *channel = (long)freq.frequency / 16000;

  return FM_SUCCESS;
}

static int fm_radio_set_frequency(int fd, long channel) {
  struct v4l2_frequency freq {};
  int ret;

  freq.tuner = 0;
  freq.type = V4L2_TUNER_RADIO;
  freq.frequency = (unsigned int)channel * 16000;

  ret = ioctl(fd, VIDIOC_S_FREQUENCY, &freq);
  if (ret < 0) {
    printf("FmRadioController: failed to set frequency\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}

static int fm_radio_set_control(int fd, unsigned int id, long val) {
  struct v4l2_control ctrl {};
  int ret;
#ifdef DEBUG
  printf("FmRadioController:fm_radio_set_control: id(%d) val(%ld)\n", id, val);
#endif
  ctrl.id = id;
  if (val)
    ctrl.value = (unsigned int)val;
  else
    ctrl.value = 0;

  ret = ioctl(fd, VIDIOC_S_CTRL, &ctrl);
  if (ret < 0) {
    printf("FmRadioController: failed to set control\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}

static int fm_radio_seek_frequency(int fd, unsigned int upward,
                                   unsigned int wrap_around,
                                   unsigned int spacing) {
  struct v4l2_hw_freq_seek seek {};
  int ret;

  seek.tuner = 0;
  seek.type = V4L2_TUNER_RADIO;
  seek.seek_upward = upward;
  seek.wrap_around = wrap_around;
  seek.spacing = spacing;

  ret = ioctl(fd, VIDIOC_S_HW_FREQ_SEEK, &seek);
  if (ret < 0) {
    printf("FmRadioController: failed to seek frequency\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}
static int fm_radio_channel_searching(int fd, unsigned int upward,
                                      unsigned int wrap_around,
                                      unsigned int spacing, long *channel) {
  int ret;

  ret = fm_radio_set_control(fd, V4L2_CID_S610_SEEK_MODE,
                             FM_TUNER_AUTONOMOUS_SEARCH_MODE);
  if (ret < 0)
    return ret;

  ret = fm_radio_seek_frequency(fd, upward, wrap_around, spacing);
  if (ret < 0)
    return ret;

  ret = fm_radio_get_frequency(fd, channel);
  if (ret < 0)
    return ret;

  return ret;
}
static int fm_radio_set_tuner(int fd, unsigned int mode) {
  struct v4l2_tuner tuner {};
  int ret;

  tuner.index = 0;
  tuner.audmode = mode;
  tuner.type = 1;

  ret = ioctl(fd, VIDIOC_S_TUNER, &tuner);
  if (ret < 0) {
    printf("FmRadioController: failed to set tuner\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}
static int fm_radio_get_tuner(int fd) {
  struct v4l2_tuner tuner {};
  int ret;

  tuner.index = 0;
  tuner.type = 1;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    printf("FmRadioController: failed to set tuner\n");
    return FM_FAILURE;
  }

  return tuner.audmode;
}

static int fm_radio_set_mute(int fd, bool mute) {
  int muteint = 1;
  if (mute)
    muteint = 0;
  int ret = fm_radio_set_control(fd, V4L2_CID_AUDIO_MUTE, muteint);
  if (ret < 0) {
    printf("FmRadioController: failed to set mute\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}

static int fm_radio_set_volume(int fd, int volume /* 1 ~ 15 */) {
  int ret = fm_radio_set_control(fd, V4L2_CID_AUDIO_VOLUME, volume);
  if (ret < 0) {
    printf("FmRadioController: failed to set volume\n");
    return FM_FAILURE;
  }

  return FM_SUCCESS;
}

template <class C, typename T> bool contains(C &&c, T e) {
  return std::find(std::begin(c), std::end(c), e) != std::end(c);
}

static long fm_radio_get_freqs(int fd) {
  long ret = 0;
  fm_radio_set_mute(fd, true);
  sp<IFMRadio> service = IFMRadio::getService();
  bool mSysfs = service->isAvailable() == Status::YES;
  for (long &track : tracks) {
    if (mSysfs) {
      service->adjustFreqByStep(Direction::UP);
      ret = (long)service->getFreqFromSysfs();
    } else {
      fm_radio_channel_searching(fd, 1, 0, FM_CHANNEL_SPACING_50KHZ, &ret);
    }
    if (contains(tracks, ret))
      break;
    track = ret;
    printf("Found Freq %ld\n", ret);
  }
  fm_radio_set_mute(fd, false);
  return ret;
}

static int fm_radio_poll(int fd, struct pollfd *poll_fd) {
  int ret;

  poll_fd->fd = fd;
  poll_fd->events = POLLIN;
  poll_fd->revents = 0;

  ret = poll(poll_fd, 1, 360);
  if (ret > 0) {
    if (poll_fd->revents & POLLIN) {
      printf("FmRadioController: ready to read\n");
      return FM_SUCCESS;
    }

    printf("FmRadioController: cannot read yet\n");
    return FM_FAILURE;
  }

  if (!ret) {
    printf("FmRadioController: polling timeout\n");
    return FM_FAILURE;
  }

  printf("FmRadioController: pollig fail: %d\n", ret);
  return FM_FAILURE - 1;
}

static int fm_radio_read(int fd, unsigned char *buf) {
  int ret;

  ret = read(fd, buf, FM_RADIO_RDS_DATA_MAX);
  if (ret < 0) {
    printf("FmRadioController: failed to read\n");
    return FM_FAILURE;
  }

  return ret;
}

static int fm_radio_thread(int fd) {
  struct pollfd radio_poll {};
  unsigned char read_buf[FM_RADIO_RDS_DATA_MAX];
  int ret;

  while (FMThread) {
    ret = fm_radio_poll(fd, &radio_poll);
    if (ret < 0) {
      if (ret < FM_FAILURE)
        break;
      else
        continue;
    }

    ret = fm_radio_read(fd, read_buf);
    if (ret < 0)
      break;
  }
  return 0;
}
static unsigned int fm_radio_get_upperband_limit(int fd) {
  int ret;
  struct v4l2_tuner tuner {};
  unsigned int freq;
  tuner.index = 0;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    return FM_FAILURE;
  } else {
    freq = (tuner.rangehigh / 16000);
    return freq;
  }
}

static unsigned int fm_radio_get_lowerband_limit(int fd) {
  int ret;
  unsigned int freq;
  struct v4l2_tuner tuner {};

  tuner.index = 0;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    return FM_FAILURE;
  } else {
    freq = (tuner.rangelow / 16000);
    return freq;
  }
}
static long fm_radio_get_rmssi(int fd) {
  struct v4l2_tuner tuner {};
  int ret;
  long rmssi;
  tuner.index = 0;
  tuner.signal = 0;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    ret = FM_FAILURE;
  } else {
    rmssi = tuner.signal;
    ret = rmssi;
  }
  return ret;
}
static int fm_radio_set_rssi(int fd, long rssi) {
  int ret = fm_radio_set_control(fd, V4L2_CID_S610_RSSI_TH, rssi);
  if (ret < 0) {
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_openFMDevice(
    __unused JNIEnv *env, __unused jobject thiz) {
  return open_fm_device();
}
extern "C" JNIEXPORT jlong JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getFMFreq(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd) {
  long freq;
  fm_radio_get_frequency(fd, &freq);
  return freq;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMFreq(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd, jint freq) {
  return fm_radio_set_frequency(fd, freq);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMVolume(__unused JNIEnv *env,
                                                          __unused jobject thiz,
                                                          jint fd,
                                                          jint volume) {
  return fm_radio_set_volume(fd, volume);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMMute(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd,
                                                        jboolean mute) {
  return fm_radio_set_mute(fd, mute);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getFmUpper(__unused JNIEnv *env,
                                                         __unused jobject thiz,
                                                         jint fd) {
  return fm_radio_get_upperband_limit(fd);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getFMLower(__unused JNIEnv *env,
                                                         __unused jobject thiz,
                                                         jint fd) {
  return fm_radio_get_lowerband_limit(fd);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getRMSSI(__unused JNIEnv *env,
                                                       __unused jobject thiz,
                                                       jint fd) {
  return fm_radio_get_rmssi(fd);
}
extern "C" JNIEXPORT jlongArray JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getFMTracks(__unused JNIEnv *env,
                                                          __unused jobject thiz,
                                                          jint fd) {
  fm_radio_get_freqs(fd);
  jlongArray result;
  result = (*env).NewLongArray(TRACK_SIZE);
  if (result == nullptr) {
    return nullptr; /* out of memory error thrown */
  }
  int i;
  // fill a temp structure to use to populate the java int array
  jlong fill[TRACK_SIZE];
  for (i = 0; i < TRACK_SIZE; i++) {
    fill[i] =
        tracks[i]; // put whatever logic you want to populate the values here.
  }
  // move from the temp structure to the java structure
  (*env).SetLongArrayRegion(result, 0, TRACK_SIZE, fill);
  return result;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMStereo(__unused JNIEnv *env,
                                                          __unused jobject thiz,
                                                          jint fd) {
  return fm_radio_set_tuner(fd, 1);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMMono(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd) {
  return fm_radio_set_tuner(fd, 0);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMThread(__unused JNIEnv *env,
                                                          __unused jobject thiz,
                                                          jint fd,
                                                          jboolean run) {
  if (run) {
    FMThread = true;
    fm_radio_thread(fd);
  } else {
    FMThread = false;
  }
  return FM_SUCCESS;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMBoot(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd) {
  fm_radio_set_control(fd, V4L2_CID_S610_IF_COUNT1, 4800); // SetIFCount 1
  fm_radio_set_control(fd, V4L2_CID_S610_IF_COUNT2, 5600); // SetIFCount 2
  fm_radio_set_control(fd, V4L2_CID_S610_SOFT_STEREO_BLEND,
                       3172); // Set Soft Stereo Blend
  fm_radio_set_control(fd, V4L2_CID_S610_SOFT_MUTE_COEFF,
                       16); // SetSoftMuteCoeff
  fm_radio_set_control(fd, V4L2_CID_S610_CH_BAND,
                       S610_BAND_FM); // Set Band (To FM)
  fm_radio_set_control(fd, V4L2_CID_S610_CH_SPACING,
                       FM_CHANNEL_SPACING_50KHZ);                // Spacing 5kHz
  fm_radio_set_control(fd, V4L2_CID_S610_RDS_ON, FM_RDS_ENABLE); // RDS on
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getNextChannel(
    __unused JNIEnv *env, __unused jobject thiz, jint fd) {
  long ret;
  sp<IFMRadio> service = IFMRadio::getService();
  bool mSysfs = service->isAvailable() == Status::YES;
  if (!mSysfs) {
    fm_radio_channel_searching(fd, 1, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
  } else {
    service->adjustFreqByStep(Direction::UP);
    ret = service->getFreqFromSysfs();
  }
  return ret;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getBeforeChannel(
    __unused JNIEnv *env, __unused jobject thiz, jint fd) {
  long ret;
  sp<IFMRadio> service = IFMRadio::getService();
  bool mSysfs = service->isAvailable() == Status::YES;
  if (!mSysfs) {
    fm_radio_channel_searching(fd, 0, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
  } else {
    service->adjustFreqByStep(Direction::DOWN);
    ret = service->getFreqFromSysfs();
  }
  return ret;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getAudioChannel(
    __unused JNIEnv *env, __unused jobject thiz, jint fd) {
  int channel = fm_radio_get_tuner(fd);
  if (channel == V4L2_TUNER_MODE_STEREO) {
    return true; // Is EarPhones
  } else {
    return false; // Is Speaker
  }
}
extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_stopSearching(
    __unused JNIEnv *env, __unused jobject thiz, jint fd) {
  fm_radio_set_control(fd, V4L2_CID_S610_SEEK_CANCEL, 1);
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setFMRSSI(__unused JNIEnv *env,
                                                        __unused jobject thiz,
                                                        jint fd, jlong rssi) {
  return fm_radio_set_rssi(fd, rssi);
}
extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_closeFMDevice(
    __unused JNIEnv *env, __unused jobject thiz, jint fd) {
  close(fd);
}
extern "C" JNIEXPORT jboolean JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_getSysfsSupport(
    __unused JNIEnv *env, __unused jobject thiz) {
  sp<IFMRadio> service = IFMRadio::getService();
  return service->isAvailable() == Status::YES;
}
