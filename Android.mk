LOCAL_PATH := $(call my-dir)

ifneq ($(filter m20lte, $(TARGET_DEVICE)),)

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
