# BOARD_CUSTOM_DTBOIMG_MK := $(PLATFORM_PATH)/kernel/dtbo.mk

INSTALLED_DTBIMAGE_TARGET := $(PRODUCT_OUT)/eureka_dtbo.img
MKDTIMG := $(HOST_OUT_EXECUTABLES)/mkdtimg$(HOST_EXECUTABLE_SUFFIX)

KERNEL_OUT := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ
DTBO_DIR   := $(KERNEL_OUT)/arch/$(KERNEL_ARCH)/boot/dts/exynos/dtbo

DTBO_CFG := $(COMMON_PATH)/dtbo/$(TARGET_DEVICE).cfg

define build-dtboimage-target
    $(call pretty,"Target dtbo image: $(INSTALLED_DTBIMAGE_TARGET)")
    $(MKDTIMG) cfg_create $@ $(DTBO_CFG) -d $(DTBO_DIR)
    $(hide) chmod a+r $@
endef

$(INSTALLED_DTBIMAGE_TARGET): $(MKDTIMG) $(INSTALLED_KERNEL_TARGET)
	$(build-dtboimage-target)
	
.PHONY: dtbimage
dtbimage: $(INSTALLED_DTBIMAGE_TARGET)

INSTALLED_RADIOIMAGE_TARGET += $(INSTALLED_DTBIMAGE_TARGET)
