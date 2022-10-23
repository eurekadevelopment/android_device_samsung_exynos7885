ifneq ($(filter $(TARGET_DEVICE), a10 a10dd),)
LOCAL_PATH := $(call my-dir)
$(call add-radio-file,eureka_dtbo.img)
endif
