# BOARD_CUSTOM_DTBOIMG_MK := $(PLATFORM_PATH)/kernel/dtbo.mk

MKDTIMG := $(HOST_OUT_EXECUTABLES)/mkdtimg$(HOST_EXECUTABLE_SUFFIX)
KERNEL_OUT := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ
DTBO_DIR   := $(KERNEL_OUT)/arch/$(KERNEL_ARCH)/boot/dts/exynos/dtbo
DTBO_CFG := $(COMMON_PATH)/dtbo/$(TARGET_DEVICE).cfg

INSTALLED_DTBOIMAGE_TARGET := $(PRODUCT_OUT)/eureka_dtbo.img
	
$(INSTALLED_DTBOIMAGE_TARGET): $(PRODUCT_OUT)/kernel $(MKDTIMG)
	$(call pretty,"Target dtbo image: $(INSTALLED_DTBIMAGE_TARGET)")
	$(hide) echo "Building eureka_dtbo.img"
	$(MKDTIMG) cfg_create $@ $(DTBO_CFG) -d $(DTBO_DIR)
#	$(hide) $(call assert-max-image-size,$@,$(BOARD_DTBOIMAGE_PARTITION_SIZE),raw)
	$(hide) chmod a+r $@
	
.PHONY: dtboimage
dtboimage: $(INSTALLED_DTBIMAGE_TARGET)

INSTALLED_RADIOIMAGE_TARGET += $(INSTALLED_DTBOIMAGE_TARGET)
