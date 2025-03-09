# Inherit A10 ARM64 board flags
include device/samsung/a10/BoardConfig.mk

# Keymaster
$(call soong_config_set,samsungVars,target_keymaster4_library,//vendor/samsung/a10-arm:libskeymaster4device)
