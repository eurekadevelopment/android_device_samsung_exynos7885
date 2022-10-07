#include <sys/ioctl.h>
#include <sys/poll.h>
#include <cstdio>
#include <cstdlib>
#include <fcntl.h>
#include <stdint.h>
#include <cstring>
#include <unistd.h>
#include <cerrno>
#include <iostream>
#include "S610_FMRadio.h"
#include "V4L2_4_4_API.h"
#include <vector>

namespace fm_radio_slsi {

static bool FMThread = false;

int open_device(void) {
  int fd;
  if ((fd = open("/dev/radio0", O_RDWR | O_CLOEXEC)) < 0) {
    printf("Cannot open /dev/radio0.\n");
    return -1;
  }
  return fd;
}

int get_frequency(const int fd, int64_t *channel) {
  struct v4l2_frequency freq {};
  int ret;

  freq.tuner = 0;
  freq.type = V4L2_TUNER_RADIO;

  ret = ioctl(fd, VIDIOC_G_FREQUENCY, &freq);
  if (ret < 0)
     return FM_FAILURE;

  *channel = static_cast<int64_t>(freq.frequency) / 16000;

  return FM_SUCCESS;
}

int set_frequency(const int fd, int64_t channel) {
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

static int set_control(const int fd, unsigned int id, int64_t val) {
  struct v4l2_control ctrl {};
  int ret;
  ctrl.id = id;
  if (val)
    ctrl.value = static_cast<unsigned int>(val);
  else
    ctrl.value = 0;

  ret = ioctl(fd, VIDIOC_S_CTRL, &ctrl);
  if (ret < 0) {
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}

static int seek_frequency(int fd, unsigned int upward,
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
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}

static int channel_search(const int fd, unsigned int upward,
                                      unsigned int wrap_around,
                                      unsigned int spacing, int64_t *channel) {
  int ret;

  ret = set_control(fd, V4L2_CID_S610_SEEK_MODE,
                             FM_TUNER_AUTONOMOUS_SEARCH_MODE);
  if (ret < 0)
    return ret;

  ret = seek_frequency(fd, upward, wrap_around, spacing);
  if (ret < 0)
    return ret;

  ret = get_frequency(fd, channel);
  if (ret < 0)
    return ret;

  return ret;
}
int next_channel(const int fd) {
	int64_t ret;
	channel_search(fd, 1, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
	return ret;
}
int before_channel(const int fd) {
	int64_t ret;
	channel_search(fd, 0, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
	return ret;
}
int set_mute(const int fd, bool mute) {
  int ret = set_control(fd, V4L2_CID_AUDIO_MUTE, !mute);
  if (ret < 0) {
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}

int set_volume(int fd, int volume /* 1 ~ 15 */) {
  int ret = set_control(fd, V4L2_CID_AUDIO_VOLUME, volume);
  if (ret < 0) {
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}

std::vector<int64_t> get_freqs(const int fd) {
  set_mute(fd, true);

  auto map = std::vector<int64_t>();
  for (int i = 0; i < TRACK_SIZE; i++) {
    int64_t found = 0;
    channel_search(fd, 1, 0, FM_CHANNEL_SPACING_50KHZ, &found);
    for (auto k : map) {
	if (k == found)
            continue;
    }
    map.push_back(found);    
  }

  set_mute(fd, false);
  return map;
}

static int fm_poll(int fd, struct pollfd *poll_fd) {
  int ret;

  poll_fd->fd = fd;
  poll_fd->events = POLLIN;
  poll_fd->revents = 0;

  ret = poll(poll_fd, 1, 360);
  if (ret > 0) {
    if (poll_fd->revents & POLLIN) {
      return FM_SUCCESS;
    }
    return FM_FAILURE;
  }

  if (!ret) {
    return FM_FAILURE;
  }

  return FM_FAILURE - 1;
}

static int fm_read(int fd, unsigned char *buf) {
  int ret;

  ret = read(fd, buf, FM_RADIO_RDS_DATA_MAX);
  if (ret < 0) {
    printf("FmRadioController: failed to read\n");
    return FM_FAILURE;
  }

  return ret;
}

static void fm_thread(int fd) {
  struct pollfd radio_poll {};
  unsigned char read_buf[FM_RADIO_RDS_DATA_MAX];
  int ret;

  while (FMThread) {
    ret = fm_poll(fd, &radio_poll);
    if (ret < 0) {
      if (ret < FM_FAILURE)
        break;
      else
        continue;
    }

    ret = fm_read(fd, read_buf);
    if (ret < 0)
      break;
  }
}

void fm_thread_set(const int fd, const bool enable) {
  FMThread = enable;
  if (enable) fm_thread(fd);
}

unsigned int get_upperband_limit(int fd) {
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

unsigned int get_lowerband_limit(int fd) {
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

int64_t get_rmssi(int fd) {
  struct v4l2_tuner tuner {};
  int ret;
  int64_t rmssi;
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

int set_rssi(int fd, int64_t rssi) {
  int ret = set_control(fd, V4L2_CID_S610_RSSI_TH, rssi);
  if (ret < 0) {
    return FM_FAILURE;
  }
  return FM_SUCCESS;
}

void bootctrl(const int fd) {
  set_control(fd, V4L2_CID_S610_IF_COUNT1, 4800); // SetIFCount 1
  set_control(fd, V4L2_CID_S610_IF_COUNT2, 5600); // SetIFCount 2
  set_control(fd, V4L2_CID_S610_SOFT_STEREO_BLEND,
                       3172); // Set Soft Stereo Blend
  set_control(fd, V4L2_CID_S610_SOFT_MUTE_COEFF,
                       16); // SetSoftMuteCoeff
  set_control(fd, V4L2_CID_S610_CH_BAND,
                       S610_BAND_FM); // Set Band (To FM)
  set_control(fd, V4L2_CID_S610_CH_SPACING,
                       FM_CHANNEL_SPACING_50KHZ);                // Spacing 5kHz
  set_control(fd, V4L2_CID_S610_RDS_ON, FM_RDS_ENABLE); // RDS on
}

void stop_search(const int fd) {
  set_control(fd, V4L2_CID_S610_SEEK_CANCEL, 1);
}

}
