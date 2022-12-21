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

#define LOG_TAG "PartsHAL-Display"

#include "Display.h"

#include <sstream>
#include <string>

#include <FileIO.h>
#include <LogFormat.h>

namespace aidl::vendor::eureka::hardware::parts {

constexpr const char *SEC_TSP_CMD = "/sys/class/sec/tsp/cmd";
constexpr const char *SEC_TSP_CMD_RESULT = "/sys/class/sec/tsp/cmd_result";

static std::string DisplayStrBuilder(bool enable, DisplaySys type) {
  std::stringstream builder;
  switch (type) {
  case DisplaySys::DOUBLE_TAP:
    builder << "aot_enable";
    break;
  case DisplaySys::GLOVE_MODE:
    builder << "glove_mode";
    break;
  };
  builder << ",";
  builder << enable; // Implicit conversion
  return builder.str();
}

enum DisplayResult {
  OK,
  NOOP,
  INVALID,
};

constexpr const char *OK_STR = "OK";
constexpr const char *NOOP_STR = "NOOP";

static DisplayResult parseResult(std::string *result, DisplaySys from, bool enabled) {
  std::stringstream ss;
  std::string parsed;
  auto addEmpty = [](std::string *str) {
    if (str->empty())
      *str = std::string("(empty)");
  };
  ss << DisplayStrBuilder(enabled, from);
  ss << ":";
  if (result->find(ss.str()) == std::string::npos) {
    LOG_E("Unexpected: Failed to find built string in result string");
    goto error;
  }
  parsed = result->substr(result->find(ss.str()) + 1);
  if (parsed == OK_STR) {
    return DisplayResult::OK;
  } else if (parsed == NOOP_STR) {
    return DisplayResult::NOOP;
  }
  addEmpty(&parsed);
  LOG_E("Unexpected: Failed to parse by matching parsed result string");
  LOG_E("Parsed string was %s", parsed.c_str());
error:
  LOG_W("Falling back to find");
  if (result->find(OK_STR) != std::string::npos)
    return DisplayResult::OK;
  else if (result->find(NOOP_STR) != std::string::npos)
    return DisplayResult::NOOP;
  else {
    addEmpty(result);
    LOG_E("Failed to find result string in result string");
    LOG_E("Resulting string was: %s", result->c_str());
    return DisplayResult::INVALID;
  }
}

::ndk::ScopedAStatus DisplayConfigs::writeDisplay(bool enable,
                                                  DisplaySys type) {
  FileIO::writeline(SEC_TSP_CMD, DisplayStrBuilder(enable, type));
  return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus DisplayConfigs::readDisplay(DisplaySys type,
                                                 bool *_aidl_return) {
  std::string res;
  writeDisplay(true, type);
  res = FileIO::readline(SEC_TSP_CMD_RESULT);
  switch (parseResult(&res, type, true)) {
  case DisplayResult::NOOP: {
    *_aidl_return = true;
    break;
  }
  case DisplayResult::OK: {
    writeDisplay(true, type);
    res = FileIO::readline(SEC_TSP_CMD_RESULT);
    switch (parseResult(&res, type, true)) {
    case DisplayResult::NOOP: {
      *_aidl_return = false;
      writeDisplay(false, type);
      break;
    }
    case DisplayResult::OK: {
      LOG_W("Double enable returns OK: function not implemented.");
      return ::ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }
    case DisplayResult::INVALID:
      return ::ndk::ScopedAStatus::fromExceptionCode(EX_ILLEGAL_ARGUMENT);
    };
  } break;
  case DisplayResult::INVALID:
    return ::ndk::ScopedAStatus::fromExceptionCode(EX_ILLEGAL_ARGUMENT);
  };
  return ::ndk::ScopedAStatus::ok();
}
} // namespace aidl::vendor::eureka::hardware::parts
