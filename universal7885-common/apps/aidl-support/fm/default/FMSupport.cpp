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

#include <algorithm>

#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include <FileIO.h>

#include "CommonMacro.h"

namespace aidl::vendor::eureka::hardware::fmradio {

#define FM_SYSFS_BASE "/sys/devices/virtual/s610_radio/s610_radio"
constexpr const char *FM_FREQ_CTL = FM_SYSFS_BASE "/radio_freq_ctrl";
constexpr const char *FM_FREQ_SEEK = FM_SYSFS_BASE "/radio_freq_seek";

::ndk::ScopedAStatus FMSupport::open(void) { NOT_SUPPORTED; }

::ndk::ScopedAStatus FMSupport::getValue(GetType type, int *_aidl_return) {
  if (type != GetType::GET_TYPE_FM_MUTEX_LOCKED) {
    RETURN_IF_FAILED_LOCK;
  }
  switch (type) {
  case GetType::GET_TYPE_FM_FREQ:
    *_aidl_return = FileIO::readint(FM_FREQ_CTL);
    break;
  case GetType::GET_TYPE_FM_UPPER_LIMIT:
  case GetType::GET_TYPE_FM_LOWER_LIMIT:
  case GetType::GET_TYPE_FM_RMSSI:
    NOT_SUPPORTED;
  case GetType::GET_TYPE_FM_BEFORE_CHANNEL:
    if (index > 0)
      index -= 1;
    if (kMiddleState != nullptr) {
      index = kMiddleState->first;
      delete kMiddleState;
      kMiddleState = nullptr;
    }
    FileIO::writeline(FM_FREQ_CTL, freqs_list[index]);
    *_aidl_return = freqs_list[index];
    break;
  case GetType::GET_TYPE_FM_NEXT_CHANNEL:
    if (index < freqs_list.size() - 1)
      index += 1;
    if (kMiddleState != nullptr) {
      index = kMiddleState->second;
      delete kMiddleState;
      kMiddleState = nullptr;
    }
    FileIO::writeline(FM_FREQ_CTL, freqs_list[index]);
    *_aidl_return = freqs_list[index];
    break;
  case GetType::GET_TYPE_FM_SYSFS_IF:
    *_aidl_return = access(FM_SYSFS_BASE, F_OK);
    break;
  case GetType::GET_TYPE_FM_MUTEX_LOCKED:
    if (lock.try_lock()) {
      *_aidl_return = false;
      lock.unlock();
    } else {
      *_aidl_return = true;
    }
    break;
  default:
    break;
  };
  if (type != GetType::GET_TYPE_FM_MUTEX_LOCKED) {
    lock.unlock();
  }
  return ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::setValue(SetType type, int value) {
  RETURN_IF_FAILED_LOCK;
  switch (type) {
  case SetType::SET_TYPE_FM_FREQ:
    FileIO::writeline(FM_FREQ_CTL, value);
    if (std::find(freqs_list.begin(), freqs_list.end(), value) ==
        freqs_list.end())
      kMiddleState = saveMiddleState(value, freqs_list);
    break;
  case SetType::SET_TYPE_FM_MUTE:
  case SetType::SET_TYPE_FM_VOLUME:
  case SetType::SET_TYPE_FM_THREAD:
  case SetType::SET_TYPE_FM_RMSSI:
  case SetType::SET_TYPE_FM_SEARCH_CANCEL:
  case SetType::SET_TYPE_FM_SPEAKER_ROUTE:
  case SetType::SET_TYPE_FM_APP_PID:
    NOT_SUPPORTED;
  case SetType::SET_TYPE_FM_SEARCH_START:
    lock.unlock();
    search_thread = new std::thread([this] {
      const std::lock_guard<std::timed_mutex> guard(lock);
      for (int i = 0; i < TRACK_SIZE; i++) {
        FileIO::writeline(FM_FREQ_SEEK,
                          "1 " + std::to_string(SYSFS_SPACING * 10));
        int freq = FileIO::readint(FM_FREQ_CTL);
        if (std::find(freqs_list.begin(), freqs_list.end(), freq) !=
            freqs_list.end())
          continue;
        freqs_list.push_back(freq);
      }
      std::sort(freqs_list.begin(), freqs_list.end(), std::less<int>());
    });
    break;
  default:
    break;
  };
  if (type != SetType::SET_TYPE_FM_SEARCH_START)
    lock.unlock();
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMSupport::getFreqsList(std::vector<int> *_aidl_return) {
  RETURN_IF_FAILED_LOCK;
  *_aidl_return = freqs_list;
  lock.unlock();
  return ::ndk::ScopedAStatus::ok();
}
::ndk::ScopedAStatus FMSupport::close() { NOT_SUPPORTED; }
} // namespace aidl::vendor::eureka::hardware::fmradio
