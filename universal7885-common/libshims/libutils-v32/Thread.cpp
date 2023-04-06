/*
 * Copyright (C) 2023 The LineageOS Project
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

#include <utils/StrongPointer.h>

#include "Thread.h"

using android::sp;

namespace utils32 {

android::status_t Thread::run(const char* name, int32_t priority, size_t stack) {
    this->forceIncStrong(this);
    return android::Thread::run(name, priority, stack);
}

} // namespace utils32
