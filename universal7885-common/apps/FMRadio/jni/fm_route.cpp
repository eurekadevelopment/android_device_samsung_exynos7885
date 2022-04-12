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
#include <jni.h>
#include <media/AudioSystem.h>
#include <media/IAudioFlinger.h>

#define FM_FAILURE -1
#define FM_SUCCESS 0
#define IOHANDLE 13
using namespace android;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_eurekateam_fmradio_NativeFMInterface_setAudioRoute(
    __unused JNIEnv *env, __unused jobject thiz, jboolean speaker) {
  const sp<IAudioFlinger> &af = AudioSystem::get_audio_flinger();
  if (af == 0)
    return PERMISSION_DENIED;
  if (speaker) {
    af->setParameters(IOHANDLE, String8("routing=2"));
  } else {
    af->setParameters(IOHANDLE, String8("routing=8"));
  }
  return FM_SUCCESS;
}
