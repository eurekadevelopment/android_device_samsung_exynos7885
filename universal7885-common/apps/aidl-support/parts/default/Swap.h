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

#include <aidl/vendor/eureka/hardware/parts/BnSwapOnData.h>

namespace aidl::vendor::eureka::hardware::parts {

using cb_t = const std::shared_ptr<IBoolCallback> &;

struct SwapOnData : public BnSwapOnData {
  // Methods from ::aidl::vendor::eureka::hardware::parts::ISwapOnData
  // follow.
  ::ndk::ScopedAStatus makeSwapFile(int32_t size);
  ::ndk::ScopedAStatus setSwapOn(cb_t cb);
  ::ndk::ScopedAStatus removeSwapFile(void);
  ::ndk::ScopedAStatus setSwapOff(void);
  ::ndk::ScopedAStatus isMutexLocked(bool *_aidl_return);
};
} // namespace aidl::vendor::eureka::hardware::parts
