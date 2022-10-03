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

#include "FlashLight.h"
#include "CachedClass.h"

#include <FileIO.h>

namespace vendor::eureka::hardware::parts::V1_0 {

static FlashBrightness *kCached;

constexpr const char *TORCH_ENABLE =
    "/sys/class/camera/flash/torch_brightness_lvl_enable";
constexpr const char *TORCH_LVL =
    "/sys/class/camera/flash/torch_brightness_lvl";

// Methods from ::android::hardware::parts::V1_0::IFlashLight follow.
Return<void> FlashBrightness::setFlashlightEnable(parts::V1_0::Status enable) {
  FileIO::writeline(TORCH_ENABLE, static_cast<int32_t>(enable));
  return Void();
}

Return<void> FlashBrightness::setFlashlightWritable(int32_t value) {
  FileIO::writeline(TORCH_LVL, value);
  return Void();
}

Return<int32_t> FlashBrightness::readFlashlightstats(bool s2mu106) {
  return s2mu106 ? FileIO::readline(TORCH_LVL) / 21
                 : FileIO::readline(TORCH_LVL);
}

IFlashBrightness *FlashBrightness::getInstance(void) { USE_CACHED(kCached); }

} // namespace vendor::eureka::hardware::parts::V1_0
