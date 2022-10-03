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

#include <FileIO.h>
#include "CachedClass.h"

static int mChannelSpacing = 3;

namespace vendor::eureka::hardware::fmradio::V1_2 {

static FMRadio *kCached = nullptr;

constexpr const char* FM_FREQ_CTL = "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl";
constexpr const char* FM_FREQ_SEEK = "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_seek";

Return<void> FMRadio::setManualFreq(float freq) {
  FileIO::writeline(FM_FREQ_CTL, freq * 1000);
  return Void();
}

Return<void> FMRadio::adjustFreqByStep(fmradio::V1_0::Direction dir) {
  std::string value = "";
  if (dir == V1_0::Direction::UP) {
    value = "1 " + std::to_string(mChannelSpacing * 10);
  } else if (dir == V1_0::Direction::DOWN) {
    value = "0 " + std::to_string(mChannelSpacing * 10);
  }
  FileIO::writeline(FM_FREQ_SEEK, value);
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
  mChannelSpacing = static_cast<int>(space);
  return Void();
}
Return<int32_t> FMRadio::getFreqFromSysfs() {
  return FileIO::readline(FM_FREQ_CTL);
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
IFMRadio *FMRadio::getInstance(void) { USE_CACHED(kCached); }
} // namespace vendor::eureka::hardware::fmradio::V1_2
