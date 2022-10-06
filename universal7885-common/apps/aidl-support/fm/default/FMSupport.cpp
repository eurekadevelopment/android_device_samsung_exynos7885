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

#include "FMSupport.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <sys/stat.h>
#include <sys/types.h>

#include <FileIO.h>

static int mChannelSpacing = 3;

namespace aidl::vendor::eureka::hardware::fmradio {

constexpr const char *FM_FREQ_CTL =
    "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl";
constexpr const char *FM_FREQ_SEEK =
    "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_seek";

::ndk::ScopedAStatus FMSupport::setManualFreq(float freq) {
  FileIO::writeline(FM_FREQ_CTL, freq * 1000);
  return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMSupport::adjustFreqByStep(Direction dir) {
  std::string value = "";
  if (dir == Direction::UP) {
    value = "1 " + std::to_string(mChannelSpacing * 10);
  } else if (dir == Direction::DOWN) {
    value = "0 " + std::to_string(mChannelSpacing * 10);
  }
  FileIO::writeline(FM_FREQ_SEEK, value);
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::isAvailable(bool *_aidl_return) {
  struct stat info;
  *_aidl_return =
      stat("/sys/devices/virtual/s610_radio/s610_radio/", &info) != 0;
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::setChannelSpacing(Space space) {
  mChannelSpacing = static_cast<int>(space);
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::getFreqFromSysfs(int32_t *_aidl_return) {
  *_aidl_return = FileIO::readline(FM_FREQ_CTL);
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::getChannelSpacing(Space *_aidl_return) {
  switch (mChannelSpacing) {
  case 1:
    *_aidl_return = Space::CHANNEL_SPACING_10HZ;
    break;
  case 2:
    *_aidl_return = Space::CHANNEL_SPACING_20HZ;
    break;
  case 3:
    *_aidl_return = Space::CHANNEL_SPACING_30HZ;
    break;
  case 4:
    *_aidl_return = Space::CHANNEL_SPACING_40HZ;
    break;
  case 5:
    *_aidl_return = Space::CHANNEL_SPACING_50HZ;
    break;
  default:
    break;
  }
  return ndk::ScopedAStatus::ok();
}
} // namespace vendor::eureka::hardware::fmradio::V1_2
