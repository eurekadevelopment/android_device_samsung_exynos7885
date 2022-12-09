#define LOG_TAG "FMHAL-impl-lib"

#include <LogFormat.h>

#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/poll.h>
#include <linux/videodev2.h>
#include <unistd.h>

#include <atomic>
#include <cerrno>
#include <cstring>
#include <thread>
#include <vector>

#include "Radio_S610.h"

namespace fm_radio_slsi {

static std::atomic_bool kShouldRun;
static std::thread *poll_thread = nullptr;

constexpr const char *FM_DEV_PATH = "/dev/radio0";

#define LOG_IOCTL_ERR(cmd) \
  LOG_E(std::string("Failed to call" cmd "ioctl(), %d (%s)"), errno, strerror(errno))

#define LOG_IOCTL_ERR_ON_COND_NORETURN(cmd, cond) \
({									\
 	if ((cond)) {							\
		LOG_IOCTL_ERR(cmd);					\
 	}								\
})

#define LOG_IOCTL_ERR_ON_COND(cmd, cond) \
({                                                                      \
	if ((cond)) {                                                   \
		LOG_IOCTL_ERR(cmd); 					\
		return;                                                 \
	}                                                               \
})

int open_device(void) {
  int fd;
  if ((fd = open(FM_DEV_PATH, O_RDWR | O_CLOEXEC)) < 0) {
    LOG_E("Failed to open %s, %d (%s)", FM_DEV_PATH, errno, strerror(errno));
    return -1;
  }
  LOG_D("Opened %s, fd %d", FM_DEV_PATH, fd);
  return fd;
}

int get_frequency(const int fd, int *channel) {
  struct v4l2_frequency freq {};
  int ret;

  freq.tuner = 0;
  freq.type = V4L2_TUNER_RADIO;

  ret = ioctl(fd, VIDIOC_G_FREQUENCY, &freq);
  if (ret < 0) {
    LOG_IOCTL_ERR("VIDIOC_G_FREQUENCY");
    *channel = FM_FAILURE;
    return ret;
  }

  *channel = static_cast<int>(freq.frequency) / 16000;

  LOG_D("%s: Channel freq: %d", __func__, *channel);
  return ret;
}

void set_frequency(const int fd, int channel) {
  struct v4l2_frequency freq {};
  int ret;

  LOG_D("%s: Channel freq: %d", __func__, channel);

  freq.tuner = 0;
  freq.type = V4L2_TUNER_RADIO;
  freq.frequency = (unsigned int)channel * 16000;

  ret = ioctl(fd, VIDIOC_S_FREQUENCY, &freq);

  LOG_IOCTL_ERR_ON_COND("VIDIOC_S_FREQUENCY", ret < 0);
}

static int set_control(const int fd, unsigned int id, int val) {
  struct v4l2_control ctrl {};
  int ret;
  ctrl.id = id;

  if (val)
    ctrl.value = static_cast<unsigned int>(val);
  else
    ctrl.value = 0;

  ret = ioctl(fd, VIDIOC_S_CTRL, &ctrl);

  LOG_IOCTL_ERR_ON_COND_NORETURN("VIDIOC_S_CTRL", ret < 0);

  return ret;
}

static int seek_frequency(int fd, unsigned int upward, unsigned int wrap_around,
                          unsigned int spacing) {
  struct v4l2_hw_freq_seek seek {};
  int ret;

  seek.tuner = 0;
  seek.type = V4L2_TUNER_RADIO;
  seek.seek_upward = upward;
  seek.wrap_around = wrap_around;
  seek.spacing = spacing;

  ret = ioctl(fd, VIDIOC_S_HW_FREQ_SEEK, &seek);

  LOG_IOCTL_ERR_ON_COND_NORETURN("VIDIOC_S_HW_FREQ_SEEK", ret < 0);

  return ret;
}

static void channel_search(const int fd, unsigned int upward,
                           unsigned int wrap_around, unsigned int spacing,
                           int *channel) {
  int ret;

  ret = set_control(fd, V4L2_CID_S610_SEEK_MODE, FM_TUNER_AUTONOMOUS_SEARCH_MODE);
  
  LOG_IOCTL_ERR_ON_COND("V4L2_CID_S610_SEEK_MODE", ret < 0);

  ret = seek_frequency(fd, upward, wrap_around, spacing);
  if (ret < 0) return;

  ret = get_frequency(fd, channel);
  if (ret < 0) return;
}

/*
int next_channel(const int fd) {
        int ret;
        channel_search(fd, 1, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
        return ret;
}
int before_channel(const int fd) {
        int ret;
        channel_search(fd, 0, 0, FM_CHANNEL_SPACING_100KHZ, &ret);
        return ret;
}
*/

void set_mute(const int fd, bool mute) {
  int ret = set_control(fd, V4L2_CID_AUDIO_MUTE, !mute);
  LOG_IOCTL_ERR_ON_COND("V4L2_CID_AUDIO_MUTE", ret < 0);
}

void set_volume(int fd, int volume /* 1 ~ 15 */) {
  int ret = set_control(fd, V4L2_CID_AUDIO_VOLUME, volume);
  LOG_IOCTL_ERR_ON_COND("V4L2_CID_AUDIO_VOLUME", ret < 0);
}

std::vector<int> get_freqs(const int fd) {
  set_mute(fd, true);

  auto map = std::vector<int>();
  for (int i = 0; i < TRACK_SIZE; i++) {
    int found = 0;
    channel_search(fd, 1, 0, FM_CHANNEL_SPACING_50KHZ, &found);
    for (auto k : map) {
      if (k == found) continue;
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
  } else if (ret < 0) {
    return FM_FAILURE - 1;
  }
  return FM_FAILURE;
}

static int fm_read(int fd, unsigned char *buf) {
  int ret;

  ret = read(fd, buf, FM_RADIO_RDS_DATA_MAX);

  if (ret < 0) {
    return FM_FAILURE;
  }

  return ret;
}

static void fm_thread(int fd) {
  struct pollfd radio_poll {};
  unsigned char read_buf[FM_RADIO_RDS_DATA_MAX];
  int ret;

  while (kShouldRun.load()) {
    ret = fm_poll(fd, &radio_poll);
    if (ret < 0) {
      if (ret < FM_FAILURE)
        break;
      else
        continue;
    }

    ret = fm_read(fd, read_buf);
    if (ret < 0) break;
  }
}

void fm_thread_set(const int fd, const bool enable) {
  kShouldRun.store(enable);
  if (enable) {
    poll_thread = new std::thread(fm_thread, fd);
  } else {
    poll_thread = nullptr;
  }
}

unsigned int get_upperband_limit(int fd) {
  int ret;
  struct v4l2_tuner tuner {};
  unsigned int freq;
  tuner.index = 0;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    LOG_IOCTL_ERR("VIDIOC_G_TUNER");
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
    LOG_IOCTL_ERR("VIDIOC_G_TUNER");
    return FM_FAILURE;
  } else {
    freq = (tuner.rangelow / 16000);
    return freq;
  }
}

int get_rmssi(int fd) {
  struct v4l2_tuner tuner {};
  int ret;
  tuner.index = 0;
  tuner.signal = 0;
  ret = ioctl(fd, VIDIOC_G_TUNER, &tuner);
  if (ret < 0) {
    LOG_IOCTL_ERR("VIDIOC_G_TUNER");
    ret = FM_FAILURE;
  } else {
    ret = tuner.signal;
  }
  return ret;
}

void set_rssi(int fd, int rssi) {
  int ret = set_control(fd, V4L2_CID_S610_RSSI_TH, rssi);
  LOG_IOCTL_ERR_ON_COND("V4L2_CID_S610_RSSI_TH", ret < 0);
}

void bootctrl(const int fd) {
  set_control(fd, V4L2_CID_S610_IF_COUNT1, 4800);  // SetIFCount 1
  set_control(fd, V4L2_CID_S610_IF_COUNT2, 5600);  // SetIFCount 2
  set_control(fd, V4L2_CID_S610_SOFT_STEREO_BLEND,
              3172);  // Set Soft Stereo Blend
  set_control(fd, V4L2_CID_S610_SOFT_MUTE_COEFF,
              16);  // SetSoftMuteCoeff
  set_control(fd, V4L2_CID_S610_CH_BAND,
              S610_BAND_FM);  // Set Band (To FM)
  set_control(fd, V4L2_CID_S610_CH_SPACING,
              FM_CHANNEL_SPACING_50KHZ);                 // Spacing 5kHz
  set_control(fd, V4L2_CID_S610_RDS_ON, FM_RDS_ENABLE);  // RDS on
}

void stop_search(const int fd) {
  set_control(fd, V4L2_CID_S610_SEEK_CANCEL, 1);
}

}  // namespace fm_radio_slsi
