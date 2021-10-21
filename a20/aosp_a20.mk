$(call inherit-product, device/samsung/a20/full_a20.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/aosp/config/common_full_phone.mk)

PRODUCT_NAME := aosp_a20
TARGET_FACE_UNLOCK_SUPPORTED := true

TARGET_SUPPORTS_GOOGLE_RECORDER := true
TARGET_INCLUDE_STOCK_ARCORE := true
TARGET_INCLUDE_LIVE_WALLPAPERS := true
# Set bootanimation resolution to 7200p
TARGET_BOOT_ANIMATION_RES := 720
