DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

# Inherit common device configuration
$(call inherit-product, device/samsung/universal7885-common/universal7885-common.mk)

$(call inherit-product, vendor/samsung/a20e/a20e-vendor.mk)

$(call inherit-product, frameworks/native/build/phone-xhdpi-4096-dalvik-heap.mk)

# Derp
DERP_BUILDTYPE := Official

PRODUCT_PACKAGES += \
    CameraLightSensor

# USB
PRODUCT_PACKAGES += \
    android.hardware.usb@1.3-service.samsung

TARGET_SCREEN_HEIGHT := 1560
TARGET_SCREEN_WIDTH := 720

# Fingerprint
PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.fingerprint.xml

PRODUCT_PACKAGES += \
   fstab.exynos7884B
   
PRODUCT_PACKAGES += \
   android.hardware.sensors@1.0-service
