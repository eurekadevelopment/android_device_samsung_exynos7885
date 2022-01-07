ifeq ($(TARGET_DEVICE),a40)
LOCAL_PATH := $(call my-dir)
$(call add-radio-file,eureka_dtbo.img)
endif
