package com.eurekateam.samsungextras;

public class GlobalConstants {
    /**
     * Global Variables
     */
    public static final String FLASHLIGHT_SYSFS = "/sys/class/camera/flash/" +
            "torch_brightness_lvl";
    public static final String FLASHLIGHT_SYSFS_ENABLE = "/sys/class/camera/" +
            "flash/torch_brightness_lvl_enable";
    public static final String TMU_SYSFS = "/sys/devices/platform/11500000.mali/tmu";
    public static final String TAG = "SamsungExtras";
    public static final String FASTCHARGE_SYSFS = "/sys/class/sec/switch/afc_disable";
    public static final String CHARGE_DISABLE_SYSFS = "/sys/devices/platform/battery/" +
            "power_supply/battery/batt_slate_mode";
    public static final String BATTERY_CAPACITY_MAX_SYSFS = "/sys/devices/platform/battery/"
            + "power_supply/battery/charge_full";
    public static final String BATTERY_TEMP_SYSFS = "/sys/devices/platform/battery/"
            + "power_supply/battery/batt_temp";
    public static final String BATTERY_CAPACITY_CURRENT_SYSFS = "/sys/devices/platform/battery/"
            + "power_supply/battery/capacity";
    public static final String BATTERY_CURRENT_SYSFS = "/sys/devices/platform/battery/"
            + "power_supply/battery/current_now";
    public static final String BATTERY_TIME_TO_FULL_SYSFS = "/sys/devices/platform/battery/"
            + "power_supply/battery/time_to_full_now";
}
