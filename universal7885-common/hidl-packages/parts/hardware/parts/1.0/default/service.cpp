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

#define LOG_TAG "vendor.eureka.hardware.parts@1.0-service"

#include <hidl/LegacySupport.h>
#include <vendor/eureka/hardware/parts/1.0/IBatteryStats.h>
#include <vendor/eureka/hardware/parts/1.0/IDisplayConfigs.h>
#include <vendor/eureka/hardware/parts/1.0/IFlashBrightness.h>
#include <vendor/eureka/hardware/parts/1.0/ISmartCharge.h>
#include <vendor/eureka/hardware/parts/1.0/ISwapOnData.h>

#include "Battery.h"
#include "Display.h"
#include "FlashLight.h"
#include "SmartCharge.h"
#include "Swap.h"

using android::sp;
using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using vendor::eureka::hardware::parts::V1_0::BatteryStats;
using vendor::eureka::hardware::parts::V1_0::DisplayConfigs;
using vendor::eureka::hardware::parts::V1_0::FlashBrightness;
using vendor::eureka::hardware::parts::V1_0::IBatteryStats;
using vendor::eureka::hardware::parts::V1_0::IDisplayConfigs;
using vendor::eureka::hardware::parts::V1_0::IFlashBrightness;
using vendor::eureka::hardware::parts::V1_0::ISmartCharge;
using vendor::eureka::hardware::parts::V1_0::ISwapOnData;
using vendor::eureka::hardware::parts::V1_0::SmartCharge;
using vendor::eureka::hardware::parts::V1_0::SwapOnData;

template <class C> static inline int registerAsService(C kClass) {
  int ret = -1;
  if (kClass != nullptr) {
    ret = kClass->registerAsService();
  }
  return ret;
}

int main() {
  android::sp<IBatteryStats> mBatteryService = BatteryStats::getInstance();
  android::sp<IFlashBrightness> mFlashLightService =
      FlashBrightness::getInstance();
  android::sp<IDisplayConfigs> mDisplayService = DisplayConfigs::getInstance();
  android::sp<ISwapOnData> mSwapService = SwapOnData::getInstance();
  android::sp<ISmartCharge> mSmartChargeService = SmartCharge::getInstance();

  configureRpcThreadpool(4, true /*callerWillJoin*/);

  registerAsService(mBatteryService);
  registerAsService(mFlashLightService);
  registerAsService(mDisplayService);
  registerAsService(mSwapService);
  registerAsService(mSmartChargeService);

  joinRpcThreadpool();

  return -1; // should never get here
}
