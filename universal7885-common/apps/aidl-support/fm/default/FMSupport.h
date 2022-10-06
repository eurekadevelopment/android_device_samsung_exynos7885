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

#include <aidl/vendor/eureka/hardware/fmradio/BnFMSysfsSupport.h>

namespace aidl::vendor::eureka::hardware::fmradio {

struct FMSupport : public BnFMSysfsSupport {
public:
  FMSupport() = default;
  // Methods from aidl::vendor::eureka::hardware::fmradio::IFMRadio follow.
  ::ndk::ScopedAStatus setManualFreq(float freq) override;
  ::ndk::ScopedAStatus adjustFreqByStep(Direction dir) override;
  ::ndk::ScopedAStatus isAvailable(bool *aidl_return) override;
  ::ndk::ScopedAStatus getFreqFromSysfs(int32_t *aidl_return) override;
  ::ndk::ScopedAStatus setChannelSpacing(Space space) override;
  ::ndk::ScopedAStatus getChannelSpacing(Space *_aidl_return) override;
};
} // namespace aidl::vendor::eureka::hardware::fmradio
