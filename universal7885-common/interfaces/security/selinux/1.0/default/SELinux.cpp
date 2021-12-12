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

#include "SELinux.h"
#include <fstream>
#include <iostream>
#include <sstream>
namespace vendor::eureka::security::selinux::V1_0 {

Return<int32_t> SELinux::setSELinuxWritable(selinux::V1_0::Enable enable) {
    std::ofstream file;
    std::string writevalue;
    if (enable == Enable::ENABLE) {
        writevalue = "1";
    } else {
        writevalue = "0";
    }
    file.open("/sys/fs/selinux/enforce");
    file << writevalue;
    file.close();
    return 0;
}

Return<int32_t> SELinux::readSELinuxstats(void) {
    std::ifstream file;
    std::string value;
    int32_t intvalue;
    file.open("/sys/fs/selinux/enforce");
    if (file.is_open()) {
        getline(file, value);
        file.close();
        std::stringstream val(value);
        val >> intvalue;
        return intvalue;
    }
    return -1;
}

ISELinux* SELinux::getInstance(void) {
    return new SELinux();
}
}  // namespace vendor::eureka::security::selinux::V1_0
