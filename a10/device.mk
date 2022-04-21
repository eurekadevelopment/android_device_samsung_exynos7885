DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

TARGET_BOARD_CAMERA_COUNT := 2
TARGET_BOARD_HAS_FP := false

# Inherit common device configuration
$(call inherit-product, device/samsung/universal7885-common/universal7885-common.mk)

$(call inherit-product, vendor/samsung/a10/a10-vendor.mk)

$(call inherit-product, frameworks/native/build/phone-xhdpi-2048-dalvik-heap.mk)

PRODUCT_PACKAGES += \
    CameraLightSensor

# USB
PRODUCT_PACKAGES += \
    android.hardware.usb@1.0-service.a10

TARGET_SCREEN_HEIGHT := 1560
TARGET_SCREEN_WIDTH := 720

PRODUCT_PACKAGES += \
   fstab.exynos7884B

