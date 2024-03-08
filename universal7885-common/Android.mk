LOCAL_PATH := $(call my-dir)

ifneq ($(filter a10dd a10 a20 a20e a30 a30s a40, $(TARGET_DEVICE)),)

  subdir_makefiles=$(call first-makefiles-under,$(LOCAL_PATH))
  $(foreach mk,$(subdir_makefiles),$(info including $(mk) ...)$(eval include $(mk)))

endif
