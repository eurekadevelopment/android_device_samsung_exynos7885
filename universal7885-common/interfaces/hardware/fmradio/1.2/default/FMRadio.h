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

#include <hidl/MQDescriptor.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/fmradio/1.2/IFMRadio.h>

namespace vendor::eureka::hardware::fmradio::V1_2 {

using ::android::sp;
using ::android::hardware::hidl_array;
using ::android::hardware::hidl_memory;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Return;
using ::android::hardware::Void;

struct FMRadio : public IFMRadio {
  // Methods from ::vendor::eureka::hardware::fmradio::V1_0::IFMRadio follow.
  Return<void> setManualFreq(float freq);
  Return<void> adjustFreqByStep(V1_0::Direction dir);
  // Methods from ::vendor::eureka::hardware::fmradio::V1_1::IFMRadio follow.
  Return<V1_1::Status> isAvailable();
  Return<int32_t> getFreqFromSysfs();
  // Methods from ::vendor::eureka::hardware::fmradio::V1_2::IFMRadio follow.
  Return<void> setChannelSpacing(V1_2::Space space);
  Return<V1_2::Space> getChannelSpacing();
  // Methods from ::android::hidl::base::V1_0::IBase follow.
  static IFMRadio *getInstance(void);
};
} // namespace vendor::eureka::hardware::fmradio::V1_2
