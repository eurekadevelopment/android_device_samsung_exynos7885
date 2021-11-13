PROPS_PATH := device/samsung/universal7885-common/props

BOARD_PROPERTY_OVERRIDES_SPLIT_ENABLED := true
TARGET_PRODUCT_PROP += $(PROPS_PATH)/product.prop
TARGET_VENDOR_PROP += $(PROPS_PATH)/vendor.prop
