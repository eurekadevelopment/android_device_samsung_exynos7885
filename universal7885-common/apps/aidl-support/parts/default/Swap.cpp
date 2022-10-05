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

#include "Swap.h"
#include <fstream>
#include <iostream>
#include <mutex>
#include <sys/swap.h>
#include <thread>
#include <unistd.h>

static int mSwapSize = 100;
extern int mkswap(const char *filename);
extern void mkfile(int filesize, const char *name);

constexpr const char *SWAP_PATH = "/data/swap/swapfile";

namespace aidl::vendor::eureka::hardware::parts {

static std::mutex thread_lock;

static bool swapOnRes = false;

::ndk::ScopedAStatus SwapOnData::setSwapSize(int32_t size) {
  mSwapSize = size;
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SwapOnData::removeSwapFile(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  std::remove(SWAP_PATH);
  return ::ndk::ScopedAStatus::ok();
}

static void mkfile_swapon_thread(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  if (access(SWAP_PATH, F_OK) == 0) {
    mkfile(mSwapSize * 10, SWAP_PATH);
    mkswap(SWAP_PATH);
  }
  int res =
      swapon(SWAP_PATH, (10 << SWAP_FLAG_PRIO_SHIFT) & SWAP_FLAG_PRIO_MASK);
  swapOnRes = res == 0;
}

::ndk::ScopedAStatus SwapOnData::setSwapOn() {
  const std::lock_guard<std::mutex> lock(thread_lock);
  std::thread mkswapfile(mkfile_swapon_thread);
  mkswapfile.join();
  return ::ndk::ScopedAStatus::ok();
}

static void swapoff_thread(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  swapoff(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::setSwapOff() {
  std::thread swapoff(swapoff_thread);
  swapoff.join();
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SwapOnData::getSwapOnResult(bool *_aidl_return) {
  *_aidl_return = swapOnRes;
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SwapOnData::isMutexLocked(bool *_aidl_return) {
  *_aidl_return = !thread_lock.try_lock();
  return ::ndk::ScopedAStatus::ok();
}

} // namespace aidl::vendor::eureka::hardware::parts
