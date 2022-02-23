/*
* Copyright (c) 2022 Eureka Team.
*      https://github.com/eurekadevelopment
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*/

#include "CameraDevice_3_2.h"
#include <include/convert.h>
#include <log/log.h>

namespace android {
namespace hardware {
namespace camera {
namespace device {
namespace V3_2 {
namespace implementation {

using ::android::hardware::camera::common::V1_0::Status;

Return<void> CameraDevice::getCameraCharacteristics(ICameraDevice::getCameraCharacteristics_cb _hidl_cb)  {
    Status status = initStatus();
    CameraMetadata cameraCharacteristics;
    if (status == Status::OK) {
        //Module 2.1+ codepath.
        struct camera_info info;
        if (mCameraIdInt == 2) mCameraIdInt = 1;
        int ret = mModule->getCameraInfo(mCameraIdInt, &info);
        if (ret == OK) {
            convertToHidl(info.static_camera_characteristics, &cameraCharacteristics);
        } else {
            ALOGE("%s: get camera info failed!", __FUNCTION__);
            status = Status::INTERNAL_ERROR;
        }
    }
    _hidl_cb(status, cameraCharacteristics);
    return Void();
}

} // namespace implementation
}  // namespace V3_2
}  // namespace device
}  // namespace camera
}  // namespace hardware
}  // namespace android
