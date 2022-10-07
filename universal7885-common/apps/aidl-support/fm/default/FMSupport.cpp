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

#include <iostream>

#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include <FileIO.h>

#include <android-base/logging.h>

#define NOT_SUPPORTED                                                          \
  ({                                                                           \
    LOG(ERROR) << __func__ << ": Attempted to invoke unsupported operation";   \
    return ::ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);  \
  })

namespace aidl::vendor::eureka::hardware::fmradio {

constexpr const char *FM_FREQ_CTL =
    "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_ctrl";
constexpr const char *FM_FREQ_SEEK =
    "/sys/devices/virtual/s610_radio/s610_radio/radio_freq_seek";

::ndk::ScopedAStatus FMSupport::open(void) { NOT_SUPPORTED; }

::ndk::ScopedAStatus FMSupport::getValue(GetType type, int *_aidl_return) {
  switch (type) {
  case GetType::GET_TYPE_FM_FREQ:
    *_aidl_return = FileIO::readline(FM_FREQ_CTL);
    break;
  case GetType::GET_TYPE_FM_UPPER_LIMIT:
  case GetType::GET_TYPE_FM_LOWER_LIMIT:
  case GetType::GET_TYPE_FM_RMSSI:
    NOT_SUPPORTED;
  case GetType::GET_TYPE_FM_BEFORE_CHANNEL:
    FileIO::writeline(FM_FREQ_SEEK, "0 " + std::to_string(3 * 10));
    *_aidl_return = FileIO::readline(FM_FREQ_CTL);
    break;
  case GetType::GET_TYPE_FM_NEXT_CHANNEL:
    FileIO::writeline(FM_FREQ_SEEK, "1 " + std::to_string(3 * 10));
    *_aidl_return = FileIO::readline(FM_FREQ_CTL);
    break;
  case GetType::GET_TYPE_FM_SYSFS_IF:
    *_aidl_return = access("/sys/devices/virtual/s610_radio/s610_radio/", F_OK);
    break;
  default:
    break;
  };
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::setValue(SetType type, int value) {
  switch (type) {
  case SetType::SET_TYPE_FM_FREQ:
    FileIO::writeline(FM_FREQ_CTL, freq * 1000);
    break;
  case SetType::SET_TYPE_FM_MUTE:
  case SetType::SET_TYPE_FM_VOLUME:
  case SetType::SET_TYPE_FM_THREAD:
  case SetType::SET_TYPE_FM_RMSSI:
  case SetType::SET_TYPE_FM_SEARCH_CANCEL:
    NOT_SUPPORTED;
  default:
    break;
  };
  return ::ndk::ScopedAStatus::ok();
}

static inline bool vector_contains(const std::vector<int> vec,
                                   const int search) {
  for (auto i : vec) {
    if (i == search)
      return true;
  }
  return false;
}

::ndk::ScopedAStatus FMSupport::getFreqsList(std::vector<int> *_aidl_return) {
  for (int i = 0; i < TRACK_SIZE; i++) {
    FileIO::writeline(FM_FREQ_SEEK, "1 " + std::to_string(3 * 10));
    int freq = FileIO::readline(FM_FREQ_CTL);
    if (vector_contains(*_aidl_return, freq))
      continue;
    _aidl_return->push_back(freq);
  }
  return ::ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::close() { NOT_SUPPORTED; }
} // namespace aidl::vendor::eureka::hardware::fmradio
