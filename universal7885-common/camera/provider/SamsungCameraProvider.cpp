/*
 * Copyright (C) 2021 The LineageOS Project
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

#define LOG_TAG "SamsungCameraProvider@2.5"

#include "SamsungCameraProvider.h"

#include <algorithm>

using ::android::NO_ERROR;
using ::android::OK;

using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Void;

const int kMaxCameraIdLen = 16;

SamsungCameraProvider::SamsungCameraProvider()
    : LegacyCameraProviderImpl_2_5() {
  mExtraIDs.push_back(50);
  mDisabledIDs.push_back(2);
  if (!mInitFailed) {
    for (int i : mExtraIDs) {
      struct camera_info info;
      auto rc = mModule->getCameraInfo(i, &info);

      if (rc != NO_ERROR) {
        continue;
      }

      if (checkCameraVersion(i, info) != OK) {
        ALOGE("Camera version check failed!");
        mModule.clear();
        mInitFailed = true;
        return;
      }

#ifdef SAMSUNG_CAMERA_DEBUG
      ALOGI("ID=%d is at index %d", i, mNumberOfLegacyCameras);
#endif

      char cameraId[kMaxCameraIdLen];
      snprintf(cameraId, sizeof(cameraId), "%d", i);
      std::string cameraIdStr(cameraId);
      mCameraStatusMap[cameraIdStr] = CAMERA_DEVICE_STATUS_PRESENT;

      addDeviceNames(i);
      mNumberOfLegacyCameras++;
    }
  }
}

Return<void> SamsungCameraProvider::getCameraIdList(
    ICameraProvider::getCameraIdList_cb _hidl_cb) {
  std::vector<hidl_string> deviceNameList;
  for (auto const &deviceNamePair : mCameraDeviceNames) {
    int id = std::stoi(deviceNamePair.first);
    if (id >= mNumberOfLegacyCameras ||
        std::find(mDisabledIDs.begin(), mDisabledIDs.end(), id) !=
            mDisabledIDs.end()) {
      // External camera devices must be reported through the device status
      // change callback, not in this list. Linux4: Also skip disabled camera
      // IDs.
      continue;
    }
    if (mCameraStatusMap[deviceNamePair.first] ==
        CAMERA_DEVICE_STATUS_PRESENT) {
      deviceNameList.push_back(deviceNamePair.second);
    }
  }
  hidl_vec<hidl_string> hidlDeviceNameList(deviceNameList);
  _hidl_cb(::android::hardware::camera::common::V1_0::Status::OK,
           hidlDeviceNameList);
  return Void();
}

SamsungCameraProvider::~SamsungCameraProvider() {}
