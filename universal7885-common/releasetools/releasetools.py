#!/bin/env python3
#
# Copyright (C) 2019 The LineageOS Project
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

def FullOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def IncrementalOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def AddImage(info, folder, basename, dest):
  name = basename
  data = info.input_zip.read(folder + basename)
  common.ZipWriteStr(info.output_zip, name, data)
  info.script.AppendExtra('package_extract_file("%s", "%s");' % (name, dest))

def PrintInfo(info, dest):
  info.script.Print("Patching {} image unconditionally...".format(dest.split('/')[-1]))

def OTA_InstallEnd(info):
  AddImage(info, "RADIO/", "eureka_dtb.img", "/dev/block/platform/13500000.dwmmc0/by-name/dtb")
  AddImage(info, "RADIO/", "eureka_dtbo.img", "/dev/block/platform/13500000.dwmmc0/by-name/dtbo")
  info.script.AppendExtra('run_program("/sbin/mount", "/vendor");')
  info.script.AppendExtra('run_program("/sbin/mount", "-o", "rw,remount", "/vendor");')
  info.script.AppendExtra('run_program("/sbin/rm", "-rf", "/vendor/recovery-from-boot.p");')
  info.script.AppendExtra('run_program("/sbin/umount", "/vendor");')
  return
