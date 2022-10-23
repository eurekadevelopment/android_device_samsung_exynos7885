TARGET_LOCAL_ARCH := arm

$(call inherit-product, vendor/samsung/a10-arm/a10-vendor.mk)

PRODUCT_PACKAGES += \
   android.hardware.sensors@1.0-service

include device/samsung/a10/common.mk
