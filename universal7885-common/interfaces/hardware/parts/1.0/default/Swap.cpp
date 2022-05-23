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
#include <sys/swap.h>
#include <stdio.h>

static int mSwapSize = 100;
extern int mkswap (std::string filename);
extern void mkfile(int filesize, std::string name);

static std::string SWAP_PATH = "/data/swap/swapfile";

namespace vendor::eureka::hardware::parts::V1_0 {

Return<void> SwapOnData::setSwapSize(int32_t size) {
  mSwapSize = size;
  return Void();
}

Return<void> SwapOnData::setSwapOn() {
  mkfile(mSwapSize * 10, SWAP_PATH);
  mkswap(SWAP_PATH);
  swapon(SWAP_PATH.c_str(), (10 << SWAP_FLAG_PRIO_SHIFT) & SWAP_FLAG_PRIO_MASK);
  return Void();
}

Return<void> SwapOnData::setSwapOff() {
  swapoff(SWAP_PATH.c_str());
  remove(SWAP_PATH.c_str());
  return Void();
}

ISwapOnData *SwapOnData::getInstance(void) { return new SwapOnData(); }
} // namespace vendor::eureka::hardware::parts::V1_0
