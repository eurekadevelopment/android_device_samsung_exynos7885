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

extern int mkswap(const char *filename);
extern void mkfile(int filesize, const char *name);

constexpr const char *SWAP_PATH = "/data/swap/swapfile";

namespace aidl::vendor::eureka::hardware::parts {

static std::mutex thread_lock;

static bool swapOnRes = false;

static inline bool swapfile_exist(void) {
  return access(SWAP_PATH, F_OK) == 0;
}

static void makeFile(int32_t mSwapSize) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  mkfile(mSwapSize * 10, SWAP_PATH);
  mkswap(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::makeSwapFile(int32_t size) {
  if (swapfile_exist()) return ::ndk::ScopedAStatus::ok();
  std::thread makefile_thread(makeFile, size);
  makefile_thread.detach();

  return ::ndk::ScopedAStatus::ok();
}

static void rmswap(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  std::remove(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::removeSwapFile(void) {
  if (!swapfile_exist()) return ::ndk::ScopedAStatus::ok();
  std::thread rmswap_thread(rmswap);
  rmswap_thread.detach();
  return ::ndk::ScopedAStatus::ok();
}

static void swapon_func(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  int res = swapon(SWAP_PATH, (10 << SWAP_FLAG_PRIO_SHIFT) & SWAP_FLAG_PRIO_MASK);
  swapOnRes = res == 0;
}

::ndk::ScopedAStatus SwapOnData::setSwapOn() {
  if (!swapfile_exist()) return ::ndk::ScopedAStatus::ok();
  std::thread swapon_thread(swapon_func);
  swapon_thread.detach();
  return ::ndk::ScopedAStatus::ok();
}

static void swapoff_func(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  swapoff(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::setSwapOff() {
  if (!swapfile_exist()) return ::ndk::ScopedAStatus::ok();
  std::thread swapoff_thread(swapoff_func);
  swapoff_thread.detach();
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SwapOnData::getSwapOnResult(bool *_aidl_return) {
  *_aidl_return = swapOnRes;
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus SwapOnData::isMutexLocked(bool *_aidl_return) {
  if (thread_lock.try_lock()) { 
	  thread_lock.unlock(); 
	  *_aidl_return = false; 
  } else { 
	  *_aidl_return = true; 
  }
  return ::ndk::ScopedAStatus::ok();
}

} // namespace aidl::vendor::eureka::hardware::parts
