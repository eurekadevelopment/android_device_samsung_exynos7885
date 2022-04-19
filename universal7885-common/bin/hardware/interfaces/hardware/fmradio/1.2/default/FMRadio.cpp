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
#include <sys/stat.h>
#include <sys/types.h>

static int mChannelSpacing = 3;

namespace vendor::eureka::hardware::fmradio::V1_2 {

Return<void> FMRadio::setManualFreq(float freq) {
  std::ofstream file;
  file.open("/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl");
  file << freq * 1000;
  file.close();
  return Void();
}

Return<void> FMRadio::adjustFreqByStep(fmradio::V1_0::Direction dir) {
  std::ofstream file;
  std::string value = "";
  if (dir == V1_0::Direction::UP) {
    value = "1 " + std::to_string(mChannelSpacing * 10);
  } else if (dir == V1_0::Direction::DOWN) {
    value = "0 " + std::to_string(mChannelSpacing * 10);
  }
  file.open("/sys/devices/virtual/s610_radio/s610_radio/radio_freq_seek");
  file << value;
  file.close();
  return Void();
}
Return<V1_1::Status> FMRadio::isAvailable() {
  struct stat info;
  if (stat("/sys/devices/virtual/s610_radio/s610_radio/", &info) != 0) {
    return V1_1::Status::NO;
  } else {
    return V1_1::Status::YES;
  }
}
Return<void> FMRadio::setChannelSpacing(V1_2::Space space) {
  mChannelSpacing = (int)space;
  return Void();
}
Return<int32_t> FMRadio::getFreqFromSysfs() {
  std::ifstream file;
  std::string value;
  file.open("/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl");
  std::getline(file, value);
  file.close();
  return std::stoi(value);
}
Return<V1_2::Space> FMRadio::getChannelSpacing() {
  switch (mChannelSpacing) {
  case 1:
    return V1_2::Space::CHANNEL_SPACING_10HZ;
  case 2:
    return V1_2::Space::CHANNEL_SPACING_20HZ;
  case 3:
    return V1_2::Space::CHANNEL_SPACING_30HZ;
  case 4:
    return V1_2::Space::CHANNEL_SPACING_40HZ;
  case 5:
    return V1_2::Space::CHANNEL_SPACING_50HZ;
  default:
    return V1_2::Space::CHANNEL_SPACING_30HZ;
  }
}
IFMRadio *FMRadio::getInstance(void) { return new FMRadio(); }
} // namespace vendor::eureka::hardware::fmradio::V1_2
