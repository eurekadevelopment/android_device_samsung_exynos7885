/*
 * Copyright (C) 2021, The LineageOS Project
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

#include <android-base/properties.h>

#include "edify/expr.h"
#include "otautil/error_code.h"

Value *VerifyBootloader(const char *name, State *state,
                        const std::vector<std::unique_ptr<Expr>> &argv) {
  int ret = 0;
  std::string bootloader = android::base::GetProperty("ro.boot.bootloader", "");
  if (bootloader.empty()) {
    return ErrorAbort(state, kFileGetPropFailure,
                      "%s() failed to read current bootloader version", name);
  }

  std::string min_supported_bootloader_arg;
  if (argv.empty() || !Evaluate(state, argv[0], &min_supported_bootloader_arg)) {
    return ErrorAbort(state, kArgsParsingFailure,
                      "%s() error parsing arguments", name);
  }
  int min_supported_bootloader = int(min_supported_bootloader_arg[0]);

  int version = 0;
  if (bootloader.length() >= 4)
    version = int(bootloader[bootloader.length() - 4]);

  if (version >= min_supported_bootloader) {
    ret = 1;
  }

  return StringValue(std::to_string(ret));
}

void Register_librecovery_updater_universal7904() {
  RegisterFunction("universal7904.verify_bootloader_min", VerifyBootloader);
}
