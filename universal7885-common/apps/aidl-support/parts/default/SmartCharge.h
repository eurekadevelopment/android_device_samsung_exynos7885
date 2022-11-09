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

#include <aidl/vendor/eureka/hardware/parts/BnSmartCharge.h>
#include <thread>

namespace aidl::vendor::eureka::hardware::parts {

struct SmartCharge : public BnSmartCharge {
public:
  // Methods from ::aidl::vendor::eureka::hardware::parts::ISmartCharge
  // follow.
  ::ndk::ScopedAStatus start(void);
  ::ndk::ScopedAStatus stop(void);
  ::ndk::ScopedAStatus setConfig(int32_t limit, int32_t restart);
  ::ndk::ScopedAStatus getLimitCnt(int32_t *_aidl_return);
  ::ndk::ScopedAStatus getRestartCnt(int32_t *_aidl_return);

private:
  int limit;
  int restart;
  int limit_stat;
  int restart_stat;
  bool kShouldRun;
  bool kTookAction;
  std::thread *monitor_th;
  void battery_monitor(void);
};
} // namespace aidl::vendor::eureka::hardware::parts
