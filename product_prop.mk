# Configstore
PRODUCT_PRODUCT_PROPERTIES += \
    ro.surface_flinger.max_frame_buffer_acquired_buffers=3

# Graphics
PRODUCT_PRODUCT_PROPERTIES += \
    debug.sf.latch_unsignaled=1

# LMKD
PRODUCT_PRODUCT_PROPERTIES += \
    ro.lmk.log_stats=true \
    ro.lmk.use_minfree_levels=true \
    ro.lmk.use_psi=false
