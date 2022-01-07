ifeq ($(TARGET_DEVICE),a20e)
LOCAL_PATH := $(call my-dir)
$(call add-radio-file,eureka_dtbo.img)
endif
