ifeq ($(TARGET_DEVICE),a30s)
LOCAL_PATH := $(call my-dir)
$(call add-radio-file,eureka_dtbo.img)
endif
