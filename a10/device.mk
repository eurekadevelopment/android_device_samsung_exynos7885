TARGET_LOCAL_ARCH := arm64

$(call inherit-product, vendor/samsung/a10/a10-vendor.mk)

# Sensors
PRODUCT_PACKAGES += \
    android.hardware.sensors@1.0-service_32

# ART lowmem config
PRODUCT_PRODUCT_PROPERTIES += \
    ro.config.art_lowmem=true

# Dex
PRODUCT_ART_TARGET_INCLUDE_DEBUG_BUILD := false
PRODUCT_DEX_PREOPT_DEFAULT_COMPILER_FILTER := verify
PRODUCT_MINIMIZE_JAVA_DEBUG_INFO := true
USE_DEX2OAT_DEBUG := false

include device/samsung/a10/common.mk

