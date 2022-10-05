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

#pragma once

#include <aidl/vendor/eureka/hardware/parts/BnBatteryStats.h>

#define ANDROID_SYSTEM_UID 1000
#define ANDROID_ROOT_UID 0

namespace aidl::vendor::eureka::hardware::parts {

struct BatteryStats : public BnBatteryStats {
  // Methods from ::aidl::::vendor::eureka::hardware::parts::IBatteryStats
  // follow.
  ::ndk::ScopedAStatus getBatteryStats(BatterySys stats,
                                       int32_t *_aidl_return) override;
  ::ndk::ScopedAStatus setBatteryWritable(BatterySys stats,
                                          bool value) override;
};
} // namespace aidl::vendor::eureka::hardware::parts
