#include <vendor/eureka/hardware/battery/1.0/IBattery.h>
#include <hidl/Status.h>
#include <hidl/LegacySupport.h>
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include "jni.h"

using vendor::eureka::hardware::battery::V1_0::IBattery;
using vendor::eureka::hardware::battery::V1_0::SysfsType;
using vendor::eureka::hardware::battery::V1_0::Number;
using android::sp;
 
extern "C" JNIEXPORT void
JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setChargeSysfs
(JNIEnv *env , __unused jclass obj, jint enable) {
      android::sp<IBattery> service = IBattery::getService();
      if (enable == 1){
      service->setBatteryWritable(SysfsType::CHARGE, Number::ENABLE);
      }else{
      service->setBatteryWritable(SysfsType::CHARGE, Number::DISABLE);
      }
}

extern "C" JNIEXPORT jint
JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getChargeSysfs
(JNIEnv *env , __unused jclass obj) {
      android::sp<IBattery> service = IBattery::getService();
      int ret = service->getBatteryStats(SysfsType::CHARGE);
      return ret;
}
