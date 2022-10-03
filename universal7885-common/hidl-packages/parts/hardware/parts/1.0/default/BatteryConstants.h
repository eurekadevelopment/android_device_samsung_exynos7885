#pragma once

constexpr const char *BATTERY_CAPACITY_MAX =
    "/sys/devices/platform/battery/power_supply/battery/charge_full";
constexpr const char *BATTERY_TEMP =
    "/sys/devices/platform/battery/power_supply/battery/batt_temp";
constexpr const char *BATTERY_CAPACITY_CURRENT =
    "/sys/devices/platform/battery/power_supply/battery/capacity";
constexpr const char *BATTERY_CURRENT =
    "/sys/devices/platform/battery/power_supply/battery/current_now";
constexpr const char *BATTERY_FASTCHARGE = "/sys/class/sec/switch/afc_disable";
constexpr const char *BATTERY_CHARGE =
    "/sys/devices/platform/battery/power_supply/battery/batt_slate_mode";
