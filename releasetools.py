#!/bin/env python3
#
# Copyright (C) 2021 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import common
import re

def FullOTA_Assertions(info):
  android_info = info.input_zip.read("OTA/android-info.txt")
  m = re.search(r'require\s+version-bootloader-min\s*=\s*(\S+)', android_info)
  if m:
    bootloader_version = m.group(1)
    cmd = ('assert(universal7904.verify_bootloader_min("{}") == "1" || abort("ERROR: This package requires Android 10 or 11 based firmware. Please upgrade firmware and retry!"););').format(bootloader_version)
    info.script.AppendExtra(cmd)
