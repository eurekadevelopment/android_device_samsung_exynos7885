DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

# Inherit common device configuration
$(call inherit-product, device/samsung/universal7885-common/universal7885-common.mk)

$(call inherit-product, vendor/samsung/a20/a20-vendor.mk)

# Keymaster
PRODUCT_PACKAGES += \
    android.hardware.keymaster@4.0-service.samsung \
    libkeymaster4_1support.vendor

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/android.hardware.keymaster@4.0-service.samsung.xml:$(TARGET_COPY_OUT_VENDOR)/etc/vintf/manifest/android.hardware.keymaster@4.0-service.samsung.xml

# Image
#TARGET_PREBUILT_KERNEL := device/samsung/a20e/Image
#PRODUCT_COPY_FILES += \
	$(TARGET_PREBUILT_KERNEL):kernel

# NFC
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/libnfc-sec-vendor.conf:$(TARGET_COPY_OUT_VENDOR)/etc/libnfc-sec-vendor.conf

# Rootdir
PRODUCT_PACKAGES += \
	fstab.exynos7884B \
	init.target.rc \
	init.baseband.rc
