#include "jni.h"
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/IBatteryStats.h>

using android::sp;
using vendor::eureka::hardware::parts::V1_0::IBatteryStats;
using vendor::eureka::hardware::parts::V1_0::Status;
using vendor::eureka::hardware::parts::V1_0::BatterySys;

enum {
  BATTERY_CAPACITY_MAX = 1,
  BATTERY_CAPACITY_CURRENT,
  BATTERY_CAPACITY_CURRENT_MAH,
  CHARGING_STATE,
  BATTERY_TEMP,
  BATTERY_CURRENT = 6
};

static android::sp<IBatteryStats> service = IBatteryStats::getService();
extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setChargeSysfs(
    JNIEnv /*env*/, __unused jclass obj, jint enable) {
  if (enable == 1) {
    service->setBatteryWritable(BatterySys::CHARGE, Status::ENABLE);
  } else {
    service->setBatteryWritable(BatterySys::CHARGE, Status::DISABLE);
  }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getChargeSysfs(
    JNIEnv /*env*/, __unused jclass obj) {
  int ret = service->getBatteryStats(BatterySys::CHARGE);
  return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setFastCharge(
    JNIEnv /*env*/, __unused jobject obj, jint enable) {
  if (enable == 1) {
    service->setBatteryWritable(BatterySys::FASTCHARGE, Status::ENABLE);
  } else {
    service->setBatteryWritable(BatterySys::FASTCHARGE, Status::DISABLE);
  }
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getFastChargeSysfs(
    JNIEnv /*env*/, __unused jclass obj) {
  int ret = service->getBatteryStats(BatterySys::FASTCHARGE);
  return ret;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getGeneralBatteryStats(
    JNIEnv /*env*/, __unused jobject obj, jint id) {
  int ret;
  switch (id) {
  case BATTERY_CAPACITY_MAX:
    ret = service->getBatteryStats(BatterySys::CAPACITY_MAX) / 1000;
    break;
  case BATTERY_CAPACITY_CURRENT:
    ret = service->getBatteryStats(BatterySys::CAPACITY_CURRENT);
    break;
  case BATTERY_CAPACITY_CURRENT_MAH:
    ret = (float)service->getBatteryStats(BatterySys::CAPACITY_CURRENT) *
          (float)service->getBatteryStats(BatterySys::CAPACITY_MAX) / 100000;
    break;
  case CHARGING_STATE:
    if (service->getBatteryStats(BatterySys::CURRENT) > 0) {
      ret = 1;
    } else {
      ret = 0;
    }
    break;
  case BATTERY_TEMP:
    ret = service->getBatteryStats(BatterySys::TEMP) / 10;
    break;
  case BATTERY_CURRENT:
    ret = service->getBatteryStats(BatterySys::CURRENT);
    break;
  default:
    ret = -1;
    break;
  }
  return ret;
}
