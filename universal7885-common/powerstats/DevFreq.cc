/*
 * Copyright (C) 2021 The Android Open Source Project
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
#include "DevFreq.h"

#include <android-base/logging.h>

static const std::string nameSuffix = "-DVFS";
static const std::string pathSuffix = "/time_in_state";

namespace aidl {
namespace android {
namespace hardware {
namespace power {
namespace stats {

DevfreqStateResidencyDataProvider::DevfreqStateResidencyDataProvider(const std::string& name,
        const std::string& path) : mName(name + nameSuffix), mPath(path + pathSuffix) {}

bool DevfreqStateResidencyDataProvider::extractNum(const char *str, char **str_end, int base,
        int64_t* num) {
    // errno can be set to any non-zero value by a library function call
    // regardless of whether there was an error, so it needs to be cleared
    // in order to check the error set by strtoll
    errno = 0;
    *num = std::strtoll(str, str_end, base);
    return (errno != ERANGE);
}

std::vector<std::pair<int64_t, int64_t>> DevfreqStateResidencyDataProvider::parseTimeInState() {
    // Using FILE* instead of std::ifstream for performance reasons
    std::unique_ptr<FILE, decltype(&fclose)> fp(fopen(mPath.c_str(), "r"), fclose);
    if (!fp) {
        PLOG(ERROR) << "Failed to open file " << mPath;
        return {};
    }

    std::vector<std::pair<int64_t, int64_t>> timeInState;

    char *line = nullptr;
    size_t len = 0;
    while (getline(&line, &len, fp.get()) != -1) {
        char* pEnd;
        int64_t frequencyHz, totalTimeMs;
        if (!extractNum(line, &pEnd, 10, &frequencyHz) ||
            !extractNum(pEnd, &pEnd, 10, &totalTimeMs)) {
            PLOG(ERROR) << "Failed to parse " << mPath;
            free(line);
            return {};
        }

        timeInState.push_back({frequencyHz, totalTimeMs});
    }

    free(line);
    return timeInState;
}

bool DevfreqStateResidencyDataProvider::getStateResidencies(
        std::unordered_map<std::string, std::vector<StateResidency>> *residencies) {
    std::vector<std::pair<int64_t, int64_t>> timeInState = parseTimeInState();

    if (timeInState.empty()) {
        return false;
    }

    int32_t id = 0;
    std::vector<StateResidency> stateResidencies;
    for (const auto[frequencyHz, totalTimeMs] : timeInState) {
        StateResidency s = {.id = id++, .totalTimeInStateMs = totalTimeMs};
        stateResidencies.push_back(s);
    }

    residencies->emplace(mName, stateResidencies);
    return true;
}

std::unordered_map<std::string, std::vector<State>> DevfreqStateResidencyDataProvider::getInfo() {
    std::vector<std::pair<int64_t, int64_t>> timeInState = parseTimeInState();

    if (timeInState.empty()) {
        return {};
    }

    int32_t id = 0;
    std::vector<State> states;
    for (const auto[frequencyHz, totalTimeMs] : timeInState) {
        State s = {.id = id++, .name = std::to_string(frequencyHz / 1000) + "MHz"};
        states.push_back(s);
    }

    return {{mName, states}};
}

}  // namespace stats
}  // namespace power
}  // namespace hardware
}  // namespace android
}  // namespace aidl
