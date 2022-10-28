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

package vendor.eureka.hardware.fmradio;

@Backing(type="int")
@VintfStability
enum SetType {
    SET_TYPE_FM_FREQ,
    SET_TYPE_FM_MUTE,
    SET_TYPE_FM_VOLUME,
    SET_TYPE_FM_THREAD,
    SET_TYPE_FM_RMSSI,
    SET_TYPE_FM_SEARCH_CANCEL,
    SET_TYPE_FM_SPEAKER_ROUTE,
    SET_TYPE_FM_SEARCH_START,
    SET_TYPE_FM_APP_PID,
}
