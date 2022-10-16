/*
 * Copyright 2021 Soo Hwan Na "Royna"
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

#include <media/AudioSystem.h>
#include <media/IAudioFlinger.h>

#include "AudioRoute.h"

#define IOHANDLE 13

using namespace android;

namespace aidl::vendor::eureka::hardware::audio_route {

ndk::ScopedAStatus AudioRoute::setParam(const std::string& param) {
  const sp<IAudioFlinger> &af = AudioSystem::get_audio_flinger();
  if (af == 0)
    return ndk::ScopedAStatus::fromExceptionCode(EX_SECURITY);
  af->setParameters(IOHANDLE, String8(param.c_str()));
  return ndk::ScopedAStatus::ok();;
}

}
