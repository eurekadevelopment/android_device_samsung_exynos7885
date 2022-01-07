ifeq ($(TARGET_DEVICE),a10)
LOCAL_PATH := $(call my-dir)
$(call add-radio-file,eureka_dtbo.img)
endif
