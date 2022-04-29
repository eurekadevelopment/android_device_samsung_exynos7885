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
Return<void> FlashBrightness::setFlashlightEnable(parts::V1_0::Number enable) {
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
  return Void();
}

Return<void> FlashBrightness::setFlashlightWritable(parts::V1_0::Value value) {
  std::ofstream file;
  std::string writevalue = std::to_string((int)value);
  file.open("/sys/class/camera/flash/torch_brightness_lvl");
  file << writevalue;
  file.close();
  return Void();
}

Return<int32_t>
FlashBrightness::readFlashlightstats(parts::V1_0::Device device) {
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

IFlashBrightness *FlashBrightness::getInstance(void) {
  return new FlashBrightness();
}
} // namespace vendor::eureka::hardware::parts::V1_0
