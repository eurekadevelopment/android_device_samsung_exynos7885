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
#include <vendor/eureka/hardware/parts/1.0/IFlashBrightness.h>

namespace vendor::eureka::hardware::parts::V1_0 {

using ::android::sp;
using ::android::hardware::hidl_array;
using ::android::hardware::hidl_memory;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Return;
using ::android::hardware::Void;

struct FlashBrightness : public IFlashBrightness {
  // Methods from ::vendor::eureka::hardware::parts::V1_0::IFlashBrightness
  // follow.
  Return<void> setFlashlightEnable(Number enable);
  Return<void> setFlashlightWritable(Value value);
  Return<int32_t> readFlashlightstats(Device device);
  // Methods from ::android::hidl::base::V1_0::IBase follow.
  static IFlashBrightness *getInstance(void);
};
} // namespace vendor::eureka::hardware::parts::V1_0
