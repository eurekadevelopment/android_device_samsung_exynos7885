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

#include "SmartCharge.h"

#include "BatteryConstants.h"
#include <FileIO.h>
#include <chrono>

namespace aidl::vendor::eureka::hardware::parts {

void SmartCharge::battery_monitor(void) {
  while (kShouldRun) {
    auto batt = FileIO::readint(BATTERY_CAPACITY_CURRENT);
    if (batt >= limit) {
      if (kTookAction)
        goto sleep;
      FileIO::writeline(BATTERY_CHARGE, 1);
      kTookAction = true;
      limit_stat += 1;
    } else if (batt <= restart) {
      if (kTookAction)
        goto sleep;
      FileIO::writeline(BATTERY_CHARGE, 0);
      kTookAction = true;
      restart_stat += 1;
    } else {
      if (!kTookAction)
        goto sleep;
      kTookAction = false;
    }
  sleep:
    std::this_thread::sleep_for(std::chrono::seconds(5));
  }
}

::ndk::ScopedAStatus SmartCharge::start(void) {
  if (limit == 0 || restart == 0)
    return ::ndk::ScopedAStatus::fromExceptionCodeWithMessage(
        EX_ILLEGAL_ARGUMENT, "Start called without configuring.");

  kShouldRun = true;
  monitor_th = new std::thread([this] { battery_monitor(); });
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SmartCharge::stop(void) {
  kShouldRun = false;
  if (monitor_th != nullptr) {
    monitor_th = nullptr;
  }
  FileIO::writeline(BATTERY_CHARGE, 0);
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SmartCharge::setConfig(int32_t limit_user,
                                            int32_t restart_user) {
  limit = limit_user;
  restart = restart_user;
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SmartCharge::getLimitCnt(int32_t *_aidl_return) {
  *_aidl_return = limit_stat;
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SmartCharge::getRestartCnt(int32_t *_aidl_return) {
  *_aidl_return = restart_stat;
  return ::ndk::ScopedAStatus::ok();
}

} // namespace aidl::vendor::eureka::hardware::parts
