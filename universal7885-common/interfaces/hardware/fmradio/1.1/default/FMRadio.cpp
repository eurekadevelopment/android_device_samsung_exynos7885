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

#include "FMRadio.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <sys/types.h>
#include <sys/stat.h>

namespace vendor::eureka::hardware::fmradio::V1_1 {

Return<void> IFMRadio::setManualFreq(float freq) {
    std::ofstream file;
    file.open("/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl");
    file << freq * 1000;
    file.close();
    return Void();
}

Return<void> IFMRadio::adjustFreqByStep(fmradio::V1_0::Direction dir) {
    std::ofstream file;
    std::string value = "";
    if (dir == Direction::UP){
    	value = "1 50";
    } else if (dir == Direction::DOWN){
    	value = "0 50";
    }
    file.open("/sys/devices/virtual/s610_radio/s610_radio/radio_freq_seek");
    file << value;
    file.close();
    return Void();
}
Return<int32_t> IFMRadio::isAvailable(){
    struct stat info;
    if(stat("/sys/devices/virtual/s610_radio/s610_radio/", &info ) != 0) {
    	return Status::NO;
    else
    	return Status::YES;
    }
}
IFMRadio* IFMRadio::getInstance(void) {
    return new FMRadio();
}
}  // namespace vendor::eureka::hardware::fmradio::V1_0
