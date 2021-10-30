DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

# Inherit common device configuration
$(call inherit-product, device/samsung/universal7885-common/universal7885-common.mk)

$(call inherit-product, vendor/samsung/a20e/a20e-vendor.mk)

# Image
#TARGET_PREBUILT_KERNEL := device/samsung/a20e/Image
#PRODUCT_COPY_FILES += \
	$(TARGET_PREBUILT_KERNEL):kernel

