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

#include <unistd.h>

#include "Battery.h"
#include "BatteryConstants.h"

#include <FileIO.h>

namespace aidl::vendor::eureka::hardware::parts {

static inline const char *BatterySysToPath(BatterySys type) {
  switch (type) {
  case BatterySys::CAPACITY_MAX:
    return BATTERY_CAPACITY_MAX;
  case BatterySys::TEMP:
    return BATTERY_TEMP;
  case BatterySys::CAPACITY_CURRENT:
    return BATTERY_CAPACITY_CURRENT;
  case BatterySys::CURRENT:
    return BATTERY_CURRENT;
  case BatterySys::FASTCHARGE:
    return BATTERY_FASTCHARGE;
  case BatterySys::CHARGE:
    return BATTERY_CHARGE;
  default:
    return "";
  }
}

// Methods from ::android::hardware::battery::V1_0::IBattery follow.
::ndk::ScopedAStatus BatteryStats::getBatteryStats(BatterySys stats,
                                                   int32_t *_aidl_return) {
  *_aidl_return = FileIO::readint(BatterySysToPath(stats));
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus BatteryStats::setBatteryWritable(BatterySys stats,
                                                      bool value) {
  FileIO::writeline(BatterySysToPath(stats), value ? 1 : 0);
  return ::ndk::ScopedAStatus::ok();
}

} // namespace aidl::vendor::eureka::hardware::parts
