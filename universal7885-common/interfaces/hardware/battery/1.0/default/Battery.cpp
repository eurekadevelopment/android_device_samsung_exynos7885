// Copyright (C) 2021 Eureka Team
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "Battery.h"
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <sstream>

namespace vendor::eureka::hardware::battery::V1_0 {

// Methods from ::android::hardware::battery::V1_0::IBattery follow.
Return<int32_t> Battery::getBatteryStats(battery::V1_0::SysfsType stats) {
    std::ifstream file;
    std::string filename;
    switch (stats) {
        case SysfsType::CAPACITY_MAX:
            filename = "/sys/devices/platform/battery/power_supply/battery/charge_full";
            break;
        case SysfsType::TEMP:
            filename = "/sys/devices/platform/battery/power_supply/battery/batt_temp";
            break;
        case SysfsType::CAPACITY_CURRENT:
            filename = "/sys/devices/platform/battery/power_supply/battery/capacity";
            break;
        case SysfsType::CURRENT:
            filename = "/sys/devices/platform/battery/power_supply/battery/current_now";
            break;
        case SysfsType::FASTCHARGE:
            filename = "/sys/class/sec/switch/afc_disable";
            break;
        case SysfsType::CHARGE:
            filename = "/sys/devices/platform/battery/power_supply/battery/batt_slate_mode";
            break;
        default:
            filename = "";
            break;
    }
    std::string value;
    int32_t intvalue;
    file.open(filename);
    if (file.is_open()) {
        getline(file, value);
        file.close();
        std::stringstream val(value);
        val >> intvalue;
        return intvalue;
    }
    return -1;
}

Return<int32_t> Battery::setBatteryWritable(battery::V1_0::SysfsType stats,
                                            battery::V1_0::Number value) {
    std::ofstream file;
    std::string filename;
    bool FastCharge = false;
    switch (stats) {
        case SysfsType::CAPACITY_MAX:
            filename = "/sys/devices/platform/battery/power_supply/battery/charge_full";
            break;
        case SysfsType::TEMP:
            filename = "/sys/devices/platform/battery/power_supply/battery/batt_temp";
            break;
        case SysfsType::CAPACITY_CURRENT:
            filename = "/sys/devices/platform/battery/power_supply/battery/capacity";
            break;
        case SysfsType::CURRENT:
            filename = "/sys/devices/platform/battery/power_supply/battery/current_now";
            break;
        case SysfsType::FASTCHARGE:
            filename = "/sys/class/sec/switch/afc_disable";
            FastCharge = true;
            break;
        case SysfsType::CHARGE:
            filename = "/sys/devices/platform/battery/power_supply/battery/batt_slate_mode";
            break;
        default:
            filename = "";
            break;
    }
    if (FastCharge) seteuid(ANDROID_SYSTEM_UID);
    file.open(filename);
    int write;
    if (value == Number::ENABLE) {
        write = 1;
    } else {
        write = 0;
    }
    file << write;
    file.close();
    if (FastCharge) seteuid(ANDROID_ROOT_UID);
    return 0;
}

IBattery* Battery::getInstance(void) {
    return new Battery();
}
}  // namespace vendor::eureka::hardware::battery::V1_0
