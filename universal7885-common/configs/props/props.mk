PROPS_PATH := device/samsung/universal7885-common/configs/props

BOARD_PROPERTY_OVERRIDES_SPLIT_ENABLED := true
TARGET_PRODUCT_PROP += $(PROPS_PATH)/product.prop
TARGET_VENDOR_PROP += $(PROPS_PATH)/vendor.prop
TARGET_SYSTEM_PROP += $(PROPS_PATH)/system.prop
