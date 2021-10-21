LOCAL_PATH := $(call my-dir)

ifneq ($(filter a20, $(TARGET_DEVICE)),)

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
