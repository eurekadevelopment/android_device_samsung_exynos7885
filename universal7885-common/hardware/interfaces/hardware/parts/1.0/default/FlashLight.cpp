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

#include "FlashLight.h"
#include <fstream>
#include <iostream>
#include <sstream>
namespace vendor::eureka::hardware::parts::V1_0 {

// Methods from ::android::hardware::parts::V1_0::IFlashLight follow.
Return<int32_t> FlashLight::setFlashlightEnable(parts::V1_0::Number enable) {
    std::ofstream file;
    std::string writevalue;
    switch (enable) {
        case Number::ENABLE:
            writevalue = "1";
            break;
        case Number::DISABLE:
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

Return<int32_t> FlashLight::setFlashlightWritable(parts::V1_0::Value value) {
    std::ofstream file;
    std::string writevalue;
    switch (value) {
        case Value::ONEUI:
            writevalue = "1";
            break;
        case Value::TWOUI:
            writevalue = "2";
            break;
        case Value::THREEUI:
            writevalue = "3";
            break;
        case Value::FOURUI:
            writevalue = "4";
            break;
        case Value::FIVEUI:
            writevalue = "5";
            break;
        case Value::SIXUI:
            writevalue = "6";
            break;
        case Value::SEVENUI:
            writevalue = "7";
            break;
        case Value::EIGHTUI:
            writevalue = "8";
            break;
        case Value::NINEUI:
            writevalue = "9";
            break;
        case Value::TENUI:
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

Return<int32_t> FlashLight::readFlashlightstats(parts::V1_0::Device device) {
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

IFlashLight* FlashLight::getInstance(void) {
    return new FlashLight();
}
}  // namespace vendor::eureka::hardware::parts::V1_0
