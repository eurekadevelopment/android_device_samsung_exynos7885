/*
 * Copyright 2021 Soo Hwan Na "Royna"
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

#define CHECK_LOGCAT "/data/debug/logcat"
#define WRITE_LOGCAT "/data/debug/logs/logcat.txt"

#include <fstream>
#include <iostream>

bool check_data_logcat() {
    std::ifstream datafile(CHECK_LOGCAT);
    if (datafile.good()) {
        datafile.close();
        return true;
    } else {
        return false;
    }
}

void copy_logcat() {
    if (check_data_logcat()) {
        system("/system/bin/logcat -f /data/debug/logs/logcat.txt");
    }
}
