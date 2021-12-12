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

#define LOG_TAG "vendor.eureka.security.selinux@1.0-service"

#include <vendor/eureka/security/selinux/1.0/ISELinux.h>

#include <hidl/LegacySupport.h>

#include "SELinux.h"

using android::sp;
using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using vendor::eureka::security::selinux::V1_0::ISELinux;
using vendor::eureka::security::selinux::V1_0::SELinux;

int main() {
    int ret;
    android::sp<ISELinux> service = SELinux::getInstance();
    configureRpcThreadpool(1, true /*callerWillJoin*/);

    if (service != nullptr) {
        ret = service->registerAsService();
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
