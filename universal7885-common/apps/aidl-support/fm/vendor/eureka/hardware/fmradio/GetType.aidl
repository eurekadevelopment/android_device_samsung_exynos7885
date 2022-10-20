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
enum GetType {
   GET_TYPE_FM_FREQ,
   GET_TYPE_FM_UPPER_LIMIT,
   GET_TYPE_FM_LOWER_LIMIT,
   GET_TYPE_FM_RMSSI,
   GET_TYPE_FM_BEFORE_CHANNEL,
   GET_TYPE_FM_NEXT_CHANNEL,
   GET_TYPE_FM_SYSFS_IF,
   GET_TYPE_FM_MUTEX_LOCKED,
}
