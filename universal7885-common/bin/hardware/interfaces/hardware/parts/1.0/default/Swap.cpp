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

static int mSwapSize = 100;

#define SWAP_PATH "/data/swap/swapfile"
using namespace std;
namespace vendor::eureka::hardware::parts::V1_0 {

Return<void> SwapOnData::setSwapSize(int32_t size) {
  mSwapSize = size;
  return Void();
}

Return<void> SwapOnData::setSwapOn() {
  string cmd = string("dd if=/dev/zero of=") + string(SWAP_PATH) +
               string(" bs=") + std::to_string(mSwapSize) + "M count=10";
  system(cmd.c_str());
  cmd = string("mkswap ") + string(SWAP_PATH);
  system(cmd.c_str());
  cmd = string("swapon -p 99 ") + string(SWAP_PATH);
  system(cmd.c_str());
  return Void();
}

Return<void> SwapOnData::setSwapOff() {
  std::string cmd = string("swapoff ") + string(SWAP_PATH);
  system(cmd.c_str());
  cmd = string("rm ") + string(SWAP_PATH);
  system(cmd.c_str());
  return Void();
}

ISwapOnData *SwapOnData::getInstance(void) { return new SwapOnData(); }
} // namespace vendor::eureka::hardware::parts::V1_0
