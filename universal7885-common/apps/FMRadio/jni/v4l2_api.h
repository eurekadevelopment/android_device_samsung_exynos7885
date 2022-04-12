
typedef u_int8_t __u8;
typedef int32_t __s32;
typedef u_int32_t __u32;

struct v4l2_tuner {
  __u32 index;
  __u8 name[32];
  __u32 type; /* enum v4l2_tuner_type */
  __u32 capability;
  __u32 rangelow;
  __u32 rangehigh;
  __u32 rxsubchans;
  __u32 audmode;
  __s32 signal;
  __s32 afc;
  __u32 reserved[4];
};

struct v4l2_frequency {
  __u32 tuner;
  __u32 type; /* enum v4l2_tuner_type */
  __u32 frequency;
  __u32 reserved[8];
};

struct v4l2_hw_freq_seek {
  __u32 tuner;
  __u32 type; /* enum v4l2_tuner_type */
  __u32 seek_upward;
  __u32 wrap_around;
  __u32 spacing;
  __u32 rangelow;
  __u32 rangehigh;
  __u32 reserved[5];
};

enum v4l2_tuner_type {
  V4L2_TUNER_RADIO = 1,
  V4L2_TUNER_ANALOG_TV = 2,
  V4L2_TUNER_DIGITAL_TV = 3,
  V4L2_TUNER_SDR = 4,
  V4L2_TUNER_RF = 5,
};
struct v4l2_control {
  __u32 id;
  __s32 value;
};
/**
 * struct v4l2_capability - Describes V4L2 device caps returned by
 * VIDIOC_QUERYCAP
 *
 * @driver:	   name of the driver module (e.g. "bttv")
 * @card:	   name of the card (e.g. "Hauppauge WinTV")
 * @bus_info:	   name of the bus (e.g. "PCI:" + pci_name(pci_dev) )
 * @version:	   KERNEL_VERSION
 * @capabilities: capabilities of the physical device as a whole
 * @device_caps:  capabilities accessed via this particular device (node)
 * @reserved:	   reserved fields for future extensions
 */
struct v4l2_capability {
  __u8 driver[16];
  __u8 card[32];
  __u8 bus_info[32];
  __u32 version;
  __u32 capabilities;
  __u32 device_caps;
  __u32 reserved[3];
};

/*
 *	T I M E C O D E
 */
struct v4l2_timecode {
  __u32 type;
  __u32 flags;
  __u8 frames;
  __u8 seconds;
  __u8 minutes;
  __u8 hours;
  __u8 userbits[4];
};

struct v4l2_buffer {
  __u32 index;
  __u32 type;
  __u32 bytesused;
  __u32 flags;
  __u32 field;
  struct timeval timestamp;
  struct v4l2_timecode timecode;
  __u32 sequence;

  /* memory location */
  __u32 memory;
  union {
    __u32 offset;
    unsigned long userptr;
    struct v4l2_plane *planes;
    __s32 fd;
  } m;
  __u32 length;
  __u32 reserved2;
  __u32 reserved;
};

#define V4L2_TUNER_MODE_MONO 0x0000
#define V4L2_TUNER_MODE_STEREO 0x0001

// Ioctl
#define VIDIOC_G_FREQUENCY _IOWR('V', 56, struct v4l2_frequency)
#define VIDIOC_S_FREQUENCY _IOW('V', 57, struct v4l2_frequency)
#define VIDIOC_G_TUNER _IOWR('V', 29, struct v4l2_tuner)
#define VIDIOC_S_HW_FREQ_SEEK _IOW('V', 82, struct v4l2_hw_freq_seek)
#define VIDIOC_S_TUNER _IOW('V', 30, struct v4l2_tuner)
#define VIDIOC_S_CTRL _IOWR('V', 28, struct v4l2_control)
#define VIDIOC_QUERYCAP _IOR('V', 0, struct v4l2_capability)
#define VIDIOC_G_CTRL _IOWR('V', 27, struct v4l2_control)

#define V4L2_CTRL_CLASS_USER 0x00980000 /* Old-style 'user' controls */
#define V4L2_CID_BASE (V4L2_CTRL_CLASS_USER | 0x900)
#define V4L2_CID_AUDIO_VOLUME (V4L2_CID_BASE + 5)
#define V4L2_CID_AUDIO_MUTE (V4L2_CID_BASE + 9)
