PRODUCT_GMS_CLIENTID_BASE := android-google

# Set BUILD_FINGERPRINT variable to be picked up by both system and vendor build.prop
BUILD_FINGERPRINT := "google/raven/raven:12/SP2A.220405.004/8233519:user/release-keys"

PRODUCT_BUILD_PROP_OVERRIDES += \
        PRIVATE_BUILD_DESC="raven-user 12 SP2A.220405.004 8233519 release-keys"
