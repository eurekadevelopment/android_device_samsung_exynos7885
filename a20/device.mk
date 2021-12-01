DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

# Inherit common device configuration
$(call inherit-product, device/samsung/universal7885-common/universal7885-common.mk)

$(call inherit-product, vendor/samsung/a20/a20-vendor.mk)

# Derp
DERP_BUILDTYPE := Official

PRODUCT_PACKAGES += \
    CameraLightSensor
