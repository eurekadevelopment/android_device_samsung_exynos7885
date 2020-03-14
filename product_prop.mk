# Blurs
PRODUCT_PRODUCT_PROPERTIES += \
    ro.surface_flinger.supports_background_blur=1 \
    persist.sys.sf.disable_blurs=1

# Configstore
PRODUCT_PRODUCT_PROPERTIES += \
    ro.surface_flinger.max_frame_buffer_acquired_buffers=3

# fastbootd
PRODUCT_PRODUCT_PROPERTIES += \
    ro.fastbootd.available=true

# Graphics
PRODUCT_PRODUCT_PROPERTIES += \
    debug.sf.latch_unsignaled=1

# LMKD
PRODUCT_PRODUCT_PROPERTIES += \
    ro.lmk.log_stats=true \
    ro.lmk.use_minfree_levels=true \
    ro.lmk.use_psi=false

# RIL
# LTE, GSM and WCDMA
PRODUCT_PRODUCT_PROPERTIES += \
    ro.telephony.default_network=9,9

# VNDK
PRODUCT_PRODUCT_PROPERTIES += \
    ro.vndk.lite=false
