/*
 * Copyright (c) 2022 Eureka Team.
 *      https://github.com/eurekadevelopment
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */

#include "CamDevice_3_2.h"
#include <include/convert.h>
#include <log/log.h>

#include <utility>

namespace remapper {

// Reversed means changing second to first
static void applyRemap(std::pair<int, int> config, int *intversion, std::string *stringversion)
{
	int cameraid = 0, selectedFromConfig = 0;

	if (intversion != nullptr)
		cameraid = *intversion;
	else if (stringversion != nullptr)
		cameraid = std::stoi(*stringversion);
	else return;

	if (config.first == cameraid)
		selectedFromConfig = config.second;
	else if (config.second == cameraid)
		selectedFromConfig = config.first;
	else return;

	if (intversion != nullptr)
		*intversion = selectedFromConfig;
	if (stringversion != nullptr) {
		std::vector<char> buf(2 /* single digit and null char */);
		std::snprintf(buf.data(), buf.size(), "%d", selectedFromConfig);
		*stringversion = std::string(buf.begin(), buf.end());
	}
}

} // namespace remapper

namespace android {
namespace hardware {
namespace camera {
namespace device {
namespace V3_2 {
namespace implementation {

using ::android::hardware::camera::common::V1_0::Status;

Return<void> CameraDevice::getCameraCharacteristics(
    ICameraDevice::getCameraCharacteristics_cb _hidl_cb) {
  Status status = initStatus();
  CameraMetadata cameraCharacteristics;
  if (status == Status::OK) {
    // Module 2.1+ codepath.
    struct camera_info info;
    remapper::applyRemap(std::make_pair(1, 2), &mCameraIdInt, nullptr);
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

Return<void> CameraDevice::open(const sp<ICameraDeviceCallback> &callback,
                                ICameraDevice::open_cb _hidl_cb) {
  Status status = initStatus();
  sp<CameraDeviceSession> session = nullptr;

  if (callback == nullptr) {
    ALOGE("%s: cannot open camera %s. callback is null!", __FUNCTION__,
          mCameraId.c_str());
    _hidl_cb(Status::ILLEGAL_ARGUMENT, nullptr);
    return Void();
  }

  if (status != Status::OK) {
    // Provider will never pass initFailed device to client, so
    // this must be a disconnected camera
    ALOGE("%s: cannot open camera %s. camera is disconnected!", __FUNCTION__,
          mCameraId.c_str());
    _hidl_cb(Status::CAMERA_DISCONNECTED, nullptr);
    return Void();
  } else {
    mLock.lock();

    ALOGV("%s: Initializing device for camera %d", __FUNCTION__, mCameraIdInt);
    session = mSession.promote();
    if (session != nullptr && !session->isClosed()) {
      ALOGE("%s: cannot open an already opened camera!", __FUNCTION__);
      mLock.unlock();
      _hidl_cb(Status::CAMERA_IN_USE, nullptr);
      return Void();
    }

    /** Open HAL device */
    status_t res;
    camera3_device_t *device;

    std::string mCameraID = mCameraId;
    remapper::applyRemap(std::make_pair(1, 2), &mCameraIdInt, &mCameraID);

    res = mModule->open(mCameraID.c_str(),
                        reinterpret_cast<hw_device_t **>(&device));
    if (res != OK) {
      ALOGE("%s: cannot open camera %s!", __FUNCTION__, mCameraID.c_str());
      mLock.unlock();
      _hidl_cb(getHidlStatus(res), nullptr);
      return Void();
    }

    /** Cross-check device version */
    if (device->common.version < CAMERA_DEVICE_API_VERSION_3_2) {
      ALOGE("%s: Could not open camera: "
            "Camera device should be at least %x, reports %x instead",
            __FUNCTION__, CAMERA_DEVICE_API_VERSION_3_2,
            device->common.version);
      device->common.close(&device->common);
      mLock.unlock();
      _hidl_cb(Status::ILLEGAL_ARGUMENT, nullptr);
      return Void();
    }

    struct camera_info info;
    res = mModule->getCameraInfo(mCameraIdInt, &info);
    if (res != OK) {
      ALOGE("%s: Could not open camera: getCameraInfo failed", __FUNCTION__);
      device->common.close(&device->common);
      mLock.unlock();
      _hidl_cb(Status::ILLEGAL_ARGUMENT, nullptr);
      return Void();
    }

    session =
        createSession(device, info.static_camera_characteristics, callback);
    if (session == nullptr) {
      ALOGE("%s: camera device session allocation failed", __FUNCTION__);
      mLock.unlock();
      _hidl_cb(Status::INTERNAL_ERROR, nullptr);
      return Void();
    }
    if (session->isInitFailed()) {
      ALOGE("%s: camera device session init failed", __FUNCTION__);
      session = nullptr;
      mLock.unlock();
      _hidl_cb(Status::INTERNAL_ERROR, nullptr);
      return Void();
    }
    mSession = session;

    IF_ALOGV() {
      session->getInterface()->interfaceChain(
          [](::android::hardware::hidl_vec<::android::hardware::hidl_string>
                 interfaceChain) {
            ALOGV("Session interface chain:");
            for (const auto &iface : interfaceChain) {
              ALOGV("  %s", iface.c_str());
            }
          });
    }
    mLock.unlock();
  }
  _hidl_cb(status, session->getInterface());
  return Void();
}
} // namespace implementation
} // namespace V3_2
} // namespace device
} // namespace camera
} // namespace hardware
} // namespace android
