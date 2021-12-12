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

#define LOG_TAG "vendor.eureka.hardware.gpu@1.0-service"

#include <vendor/eureka/hardware/gpu/1.0/IGpu.h>

#include <hidl/LegacySupport.h>

#include "Gpu.h"

using android::sp;
using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using vendor::eureka::hardware::gpu::V1_0::Gpu;
using vendor::eureka::hardware::gpu::V1_0::IGpu;

int main() {
    int ret;
    android::sp<IGpu> service = Gpu::getInstance();
    configureRpcThreadpool(1, true /*callerWillJoin*/);

    if (service != nullptr) {
        ret = service->registerAsService();
        if (ret != 0) {
            ALOGE("Can't register instance of Gpu HAL, nullptr");
        } else {
            ALOGI("registered Gpu HAL");
        }
    } else {
        ALOGE("Can't create instance of Gpu HAL, nullptr");
    }

    joinRpcThreadpool();

    return -1;  // should never get here
}
