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

#include "Flashlight.h"
#include <fstream>
#include <iostream>
#include <sstream>
namespace vendor::eureka::hardware::flashlight::V1_0 {

// Methods from ::android::hardware::flashlight::V1_0::IFlashlight follow.
Return<int32_t> Flashlight::setFlashlightEnable(flashlight::V1_0::Enable enable) {
    std::ofstream file;
    std::string writevalue;
    switch (enable) {
        case Enable::ENABLE:
            writevalue = "1";
            break;
        case Enable::DISABLE:
            writevalue = "0";
            break;
        default:
            writevalue = "";
            break;
    }
    file.open("/sys/class/camera/flash/torch_brightness_lvl_enable");
    file << writevalue;
    file.close();
    return 0;
}

Return<int32_t> Flashlight::setFlashlightWritable(flashlight::V1_0::Number value) {
    std::ofstream file;
    std::string writevalue;
    switch (value) {
        case Number::ONEUI:
            writevalue = "1";
            break;
        case Number::TWOUI:
            writevalue = "2";
            break;
        case Number::THREEUI:
            writevalue = "3";
            break;
        case Number::FOURUI:
            writevalue = "4";
            break;
        case Number::FIVEUI:
            writevalue = "5";
            break;
        case Number::SIXUI:
            writevalue = "6";
            break;
        case Number::SEVENUI:
            writevalue = "7";
            break;
        case Number::EIGHTUI:
            writevalue = "8";
            break;
        case Number::NINEUI:
            writevalue = "9";
            break;
        case Number::TENUI:
            writevalue = "10";
            break;
        default:
            writevalue = "";
            break;
    }
    file.open("/sys/class/camera/flash/torch_brightness_lvl");
    file << writevalue;
    file.close();
    return 0;
}

Return<int32_t> Flashlight::readFlashlightstats(flashlight::V1_0::Device device) {
    std::ifstream file;
    std::string value;
    int32_t intvalue;
    file.open("/sys/class/camera/flash/torch_brightness_lvl");
    if (file.is_open()) {
        getline(file, value);
        file.close();
        std::stringstream val(value);
        val >> intvalue;
        if (device == Device::A10) {
            return intvalue;
        } else if (device == Device::NOTA10) {
            return intvalue / 21;
        }
        // Never Here
        return -1;
    }
    return -1;
}

IFlashlight* Flashlight::getInstance(void) {
    return new Flashlight();
}
}  // namespace vendor::eureka::hardware::flashlight::V1_0
