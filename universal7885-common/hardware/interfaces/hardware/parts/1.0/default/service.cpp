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

#include <vendor/eureka/hardware/parts/1.0/IBattery.h>
#include <vendor/eureka/hardware/parts/1.0/IFlashLight.h>
#include <vendor/eureka/hardware/parts/1.0/IGpu.h>
#include <vendor/eureka/hardware/parts/1.0/ISELinux.h>

#include <hidl/LegacySupport.h>

#include "Battery.h"
#include "FlashLight.h"
#include "Gpu.h"
#include "SELinux.h"

using android::sp;
using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using vendor::eureka::hardware::parts::V1_0::Battery;
using vendor::eureka::hardware::parts::V1_0::IBattery;
using vendor::eureka::hardware::parts::V1_0::FlashLight;
using vendor::eureka::hardware::parts::V1_0::IFlashLight;
using vendor::eureka::hardware::parts::V1_0::Gpu;
using vendor::eureka::hardware::parts::V1_0::IGpu;
using vendor::eureka::hardware::parts::V1_0::SELinux;
using vendor::eureka::hardware::parts::V1_0::ISELinux;

int main() {
    int ret;
    android::sp<IBattery> mBatteryService = Battery::getInstance();
    android::sp<IFlashLight> mFlashLightService = FlashLight::getInstance();
    android::sp<IGpu> mGPUService = Gpu::getInstance();
    android::sp<ISELinux> mSEService = SELinux::getInstance();
    configureRpcThreadpool(1, true /*callerWillJoin*/);

    if (mBatteryService != nullptr) {
        ret = mBatteryService->registerAsService();
        if (ret != 0) {
            ALOGE("Can't register instance of Battery HAL, nullptr");
        } else {
            ALOGI("registered Battery HAL");
        }
    } else {
        ALOGE("Can't create instance of Battery HAL, nullptr");
    }
    if (mFlashLightService != nullptr) {
        ret = mFlashLightService->registerAsService();
        if (ret != 0) {
            ALOGE("Can't register instance of FlashLight HAL, nullptr");
        } else {
            ALOGI("registered FlashLight HAL");
        }
    } else {
        ALOGE("Can't create instance of FlashLight HAL, nullptr");
    }
    if (mGPUService != nullptr) {
        ret = mGPUService->registerAsService();
        if (ret != 0) {
            ALOGE("Can't register instance of GPU HAL, nullptr");
        } else {
            ALOGI("registered GPU HAL");
        }
    } else {
        ALOGE("Can't create instance of GPU HAL, nullptr");
    }
    if (mSEService != nullptr) {
        ret = mSEService->registerAsService();
        if (ret != 0) {
            ALOGE("Can't register instance of SELinux HAL, nullptr");
        } else {
            ALOGI("registered SELinux HAL");
        }
    } else {
        ALOGE("Can't create instance of SELinux HAL, nullptr");
    }
    joinRpcThreadpool();

    return -1;  // should never get here
}
