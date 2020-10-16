LOCAL_PATH := $(call my-dir)

ifneq ($(filter m20lte m30lte a40 a30, $(TARGET_DEVICE)),)

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
