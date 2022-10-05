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

#include <string>

#include <FileIO.h>

namespace aidl::vendor::eureka::hardware::parts {

constexpr const char *SEC_TSP_CMD = "/sys/class/sec/tsp/cmd";

::ndk::ScopedAStatus DisplayConfigs::writeDisplay(bool enable,
                                                  DisplaySys type) {
  std::string writevalue;
  if (type == DisplaySys::DOUBLE_TAP) {
    writevalue = "aot_enable";
  } else if (type == DisplaySys::GLOVE_MODE) {
    writevalue = "glove_mode";
  }
  writevalue += ",";
  if (enable) {
    writevalue += "1";
  } else {
    writevalue += "0";
  }
  FileIO::writeline(SEC_TSP_CMD, writevalue);
  return ::ndk::ScopedAStatus::ok();
}
} // namespace aidl::vendor::eureka::hardware::parts
