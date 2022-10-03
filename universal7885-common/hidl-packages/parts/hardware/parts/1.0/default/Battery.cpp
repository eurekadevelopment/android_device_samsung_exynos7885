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
#include "CachedClass.h"

#include <FileIO.h>

namespace vendor::eureka::hardware::parts::V1_0 {

static BatteryStats *kCached = nullptr;

static inline const char *BatterySysToPath(parts::V1_0::BatterySys type) {
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
Return<int32_t> BatteryStats::getBatteryStats(parts::V1_0::BatterySys stats) {
  return FileIO::readline(BatterySysToPath(stats));
}

Return<void> BatteryStats::setBatteryWritable(parts::V1_0::BatterySys stats,
                                              parts::V1_0::Status value) {
  bool FastCharge = false;
  if (FastCharge)
    seteuid(ANDROID_SYSTEM_UID);

  FileIO::writeline(BatterySysToPath(stats), static_cast<int32_t>(value));

  if (FastCharge)
    seteuid(ANDROID_ROOT_UID);

  return Void();
}

IBatteryStats *BatteryStats::getInstance(void) { USE_CACHED(kCached); }
} // namespace vendor::eureka::hardware::parts::V1_0
