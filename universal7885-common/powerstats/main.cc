/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "DevFreq.h"
#include <PowerStatsAidl.h>

#include <android-base/logging.h>
#include <android-base/properties.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <log/log.h>

using aidl::android::hardware::power::stats::DevfreqStateResidencyDataProvider;
using aidl::android::hardware::power::stats::PowerStats;

void addDevFreq(std::shared_ptr<PowerStats> p) {
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "MIF", "/sys/devices/platform/17000010.devfreq_mif/devfreq/"
                 "17000010.devfreq_mif"));
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "INT", "/sys/devices/platform/17000020.devfreq_int/devfreq/"
                 "17000020.devfreq_int"));
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "DISP", "/sys/devices/platform/17000040.devfreq_disp/devfreq/"
                  "17000040.devfreq_disp"));
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "CAM", "/sys/devices/platform/17000050.devfreq_cam/devfreq/"
                 "17000050.devfreq_cam"));
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "AUD", "/sys/devices/platform/17000060.devfreq_aud/devfreq/"
                 "17000060.devfreq_aud"));
  p->addStateResidencyDataProvider(
      std::make_unique<DevfreqStateResidencyDataProvider>(
          "FSYS", "/sys/devices/platform/17000070.devfreq_fsys/devfreq/"
                  "17000070.devfreq_fsys"));
}


int main() {
    LOG(INFO) << "PowerStats HAL AIDL Service is starting.";
    // single thread
    ABinderProcess_setThreadPoolMaxThreadCount(0);
    std::shared_ptr<PowerStats> p = ndk::SharedRefBase::make<PowerStats>();
    addDevFreq(p);

    const std::string instance = std::string() + PowerStats::descriptor + "/default";
    binder_status_t status = AServiceManager_addService(p->asBinder().get(), instance.c_str());
    LOG_ALWAYS_FATAL_IF(status != STATUS_OK);

    ABinderProcess_joinThreadPool();
    return EXIT_FAILURE;  // should not reach
}
