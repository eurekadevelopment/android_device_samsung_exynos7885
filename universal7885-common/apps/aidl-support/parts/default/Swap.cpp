// Copyright (C) 2022 Eureka Team
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
#include <cerrno>
#include <cstdio>
#include <cstring>
#include <fcntl.h>
#include <fstream>
#include <iostream>
#include <mutex>
#include <sys/stat.h>
#include <sys/swap.h>
#include <sys/types.h>
#include <thread>
#include <unistd.h>
#include <vector>

namespace {

/* XXX This needs to be obtained from kernel headers. See b/9336527 */
struct linux_swap_header {
  char bootbits[1024]; /* Space for disklabel etc. */
  u_int32_t version;
  u_int32_t last_page;
  u_int32_t nr_badpages;
  unsigned char sws_uuid[16];
  unsigned char sws_volume[16];
  u_int32_t padding[117];
  u_int32_t badpages[1];
};

void mkfile(int filesize, const char *path) {
  std::vector<char> empty(1024, 0);
  std::ofstream ofs(std::string(path), std::ios::binary | std::ios::out);

  for (int i = 0; i < 1024 * filesize; i++) {
    ofs.write(empty.data(), empty.size());
  }
}

#define MAGIC_SWAP_HEADER "SWAPSPACE2"
#define MAGIC_SWAP_HEADER_LEN 10
#define MIN_PAGES 10

int mkswap(const char *filename) {
  int err = 0;
  int fd;
  ssize_t len;
  off_t swap_size;
  int pagesize;
  struct linux_swap_header sw_hdr;
  fd = open(filename, O_WRONLY | O_CLOEXEC);
  if (fd < 0) {
    err = errno;
    return err;
  }
  pagesize = getpagesize();
  /* Determine the length of the swap file */
  swap_size = lseek(fd, 0, SEEK_END);
  if (swap_size < MIN_PAGES * pagesize) {
    err = -ENOSPC;
    goto err;
  }
  if (lseek(fd, 0, SEEK_SET)) {
    err = errno;
    goto err;
  }
  memset(&sw_hdr, 0, sizeof(sw_hdr));
  sw_hdr.version = 1;
  sw_hdr.last_page = (swap_size / pagesize) - 1;
  len = write(fd, &sw_hdr, sizeof(sw_hdr));
  if (len != sizeof(sw_hdr)) {
    err = errno;
    goto err;
  }
  /* Write the magic header */
  if (lseek(fd, pagesize - MAGIC_SWAP_HEADER_LEN, SEEK_SET) < 0) {
    err = errno;
    goto err;
  }
  len = write(fd, MAGIC_SWAP_HEADER, MAGIC_SWAP_HEADER_LEN);
  if (len != MAGIC_SWAP_HEADER_LEN) {
    err = errno;
    goto err;
  }
  if (fsync(fd) < 0) {
    err = errno;
    goto err;
  }
err:
  close(fd);
  return err;
}

} // namespace

constexpr const char *SWAP_PATH = "/data/swap/swapfile";

namespace aidl::vendor::eureka::hardware::parts {

static std::mutex thread_lock;

static inline bool swapfile_exist(void) { return access(SWAP_PATH, R_OK | W_OK) == 0; }

static void makeFile(int32_t mSwapSize) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  mkfile(mSwapSize * 10, SWAP_PATH);
  mkswap(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::makeSwapFile(int32_t size) {
  if (swapfile_exist())
    return ::ndk::ScopedAStatus::ok();
  std::thread makefile_thread(makeFile, size);
  makefile_thread.detach();

  return ::ndk::ScopedAStatus::ok();
}

static void rmswap(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  std::remove(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::removeSwapFile(void) {
  if (!swapfile_exist())
    return ::ndk::ScopedAStatus::ok();
  std::thread rmswap_thread(rmswap);
  rmswap_thread.detach();
  return ::ndk::ScopedAStatus::ok();
}

static void swapon_func(cb_t cb) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  int res =
      swapon(SWAP_PATH, (10 << SWAP_FLAG_PRIO_SHIFT) & SWAP_FLAG_PRIO_MASK);
  if (cb != nullptr)
    cb->respondToBool(res == 0);
}

::ndk::ScopedAStatus SwapOnData::setSwapOn(cb_t cb) {
  if (!swapfile_exist())
    return ::ndk::ScopedAStatus::ok();
  std::thread swapon_thread(swapon_func, cb);
  swapon_thread.detach();
  return ::ndk::ScopedAStatus::ok();
}

static void swapoff_func(void) {
  const std::lock_guard<std::mutex> lock(thread_lock);
  swapoff(SWAP_PATH);
}

::ndk::ScopedAStatus SwapOnData::setSwapOff() {
  if (!swapfile_exist())
    return ::ndk::ScopedAStatus::ok();
  std::thread swapoff_thread(swapoff_func);
  swapoff_thread.detach();
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
