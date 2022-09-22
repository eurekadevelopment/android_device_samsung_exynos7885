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

#include "Display.h"
#include <fstream>
#include <iostream>
#include <sstream>
namespace vendor::eureka::hardware::parts::V1_0 {

Return<void> DisplayConfigs::writeDisplay(parts::V1_0::Number enable,
                                          parts::V1_0::Display type) {
  std::ofstream file;
  std::string writevalue;
  if (type == Display::DOUBLE_TAP) {
    writevalue = "aot_enable";
  } else if (type == Display::GLOVE_MODE) {
    writevalue = "glove_mode";
  }
  writevalue += ",";
  if (enable == Number::ENABLE) {
    writevalue += "1";
  } else {
    writevalue += "0";
  }
  file.open("/sys/class/sec/tsp/cmd");
  file << writevalue;
  file.close();
  return Void();
}

IDisplayConfigs *DisplayConfigs::getInstance(void) {
  return new DisplayConfigs();
}
} // namespace vendor::eureka::hardware::parts::V1_0
