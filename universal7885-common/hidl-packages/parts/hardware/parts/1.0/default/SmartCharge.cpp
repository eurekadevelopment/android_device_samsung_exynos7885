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
#include "CachedClass.h"
#include "SmartChargingImpl.h"
#include <fstream>
#include <iostream>
#include <sstream>

namespace vendor::eureka::hardware::parts::V1_0 {

static SmartCharge *kCached;
static SmartChargeImpl *kInst = nullptr;

static int limit = 0;
static int restart = 0;

static int limit_stat = 0;
static int restart_stat = 0;

Return<void> SmartCharge::start(void) {
  if (limit == 0 || restart == 0)
    return Void();
  kInst = new SmartChargeImpl(limit, restart);
  kInst->start();
  return Void();
}

Return<void> SmartCharge::stop(void) {
  if (kInst != nullptr) {
    kInst->stop();
    limit_stat += kInst->charge_limit_cnt;
    restart_stat += kInst->restart_cnt;
    delete kInst;
    kInst = nullptr;
  }
  return Void();
}

Return<void> SmartCharge::setConfig(int32_t limit_user, int32_t restart_user) {
  limit = limit_user;
  restart = restart_user;
  return Void();
}

Return<int32_t> SmartCharge::getLimitCnt(void) { return limit_stat; }

Return<int32_t> SmartCharge::getRestartCnt(void) { return restart_stat; }

ISmartCharge *SmartCharge::getInstance(void) { USE_CACHED(kCached); }
} // namespace vendor::eureka::hardware::parts::V1_0
