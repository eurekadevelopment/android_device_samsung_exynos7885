#include "jni.h"
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/IBatteryStats.h>

using android::sp;
using vendor::eureka::hardware::parts::V1_0::IBatteryStats;
using vendor::eureka::hardware::parts::V1_0::Number;
using vendor::eureka::hardware::parts::V1_0::SysfsType;

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
    JNIEnv *env, __unused jclass obj, jint enable) {
  if (enable == 1) {
    service->setBatteryWritable(SysfsType::CHARGE, Number::ENABLE);
  } else {
    service->setBatteryWritable(SysfsType::CHARGE, Number::DISABLE);
  }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getChargeSysfs(
    JNIEnv *env, __unused jclass obj) {
  int ret = service->getBatteryStats(SysfsType::CHARGE);
  return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setFastCharge(
    JNIEnv *env, __unused jobject obj, jint enable) {
  if (enable == 1) {
    service->setBatteryWritable(SysfsType::FASTCHARGE, Number::ENABLE);
  } else {
    service->setBatteryWritable(SysfsType::FASTCHARGE, Number::DISABLE);
  }
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getFastChargeSysfs(
    JNIEnv *env, __unused jclass obj) {
  int ret = service->getBatteryStats(SysfsType::FASTCHARGE);
  return ret;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getGeneralBatteryStats(
    JNIEnv *env, __unused jobject obj, jint id) {
  int ret;
  switch (id) {
  case BATTERY_CAPACITY_MAX:
    ret = service->getBatteryStats(SysfsType::CAPACITY_MAX) / 1000;
    break;
  case BATTERY_CAPACITY_CURRENT:
    ret = service->getBatteryStats(SysfsType::CAPACITY_CURRENT);
    break;
  case BATTERY_CAPACITY_CURRENT_MAH:
    ret = (float)service->getBatteryStats(SysfsType::CAPACITY_CURRENT) *
          (float)service->getBatteryStats(SysfsType::CAPACITY_MAX) / 100000;
    break;
  case CHARGING_STATE:
    if (service->getBatteryStats(SysfsType::CURRENT) > 0) {
      ret = 1;
    } else {
      ret = 0;
    }
    break;
  case BATTERY_TEMP:
    ret = service->getBatteryStats(SysfsType::TEMP) / 10;
    break;
  case BATTERY_CURRENT:
    ret = service->getBatteryStats(SysfsType::CURRENT);
    break;
  default:
    ret = -1;
    break;
  }
  return ret;
}
