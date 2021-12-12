#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/battery/1.0/IBattery.h>
#include "jni.h"

using android::sp;
using vendor::eureka::hardware::battery::V1_0::IBattery;
using vendor::eureka::hardware::battery::V1_0::Number;
using vendor::eureka::hardware::battery::V1_0::SysfsType;

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setChargeSysfs(JNIEnv* env,
                                                                    __unused jclass obj,
                                                                    jint enable) {
    android::sp<IBattery> service = IBattery::getService();
    if (enable == 1) {
        service->setBatteryWritable(SysfsType::CHARGE, Number::ENABLE);
    } else {
        service->setBatteryWritable(SysfsType::CHARGE, Number::DISABLE);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getChargeSysfs(JNIEnv* env,
                                                                    __unused jclass obj) {
    android::sp<IBattery> service = IBattery::getService();
    int ret = service->getBatteryStats(SysfsType::CHARGE);
    return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_setFastCharge(JNIEnv* env,
                                                                   __unused jobject obj,
                                                                   jint enable) {
    android::sp<IBattery> service = IBattery::getService();
    if (enable == 1) {
        service->setBatteryWritable(SysfsType::FASTCHARGE, Number::ENABLE);
    } else {
        service->setBatteryWritable(SysfsType::FASTCHARGE, Number::DISABLE);
    }
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getFastChargeSysfs(JNIEnv* env,
                                                                        __unused jclass obj) {
    android::sp<IBattery> service = IBattery::getService();
    int ret = service->getBatteryStats(SysfsType::FASTCHARGE);
    return ret;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Battery_getGeneralBatteryStats(JNIEnv* env,
                                                                            __unused jobject obj,
                                                                            jint id) {
    /**
     * id:
     * 1 = BATTERY_CAPACITY_MAX
     * 2 = BATTERY_CAPACITY_CURRENT (%)
     * 3 = BATTERY_CAPACITY_CURRENT (mAh)
     * 4 = CHARGING_STATE
     * 5 = BATTERY_TEMP
     * 6 = BATTERY_CURRENT
     */
    android::sp<IBattery> service = IBattery::getService();
    int ret;
    switch (id) {
        case 1:
            ret = service->getBatteryStats(SysfsType::CAPACITY_MAX) / 1000;
            break;
        case 2:
            ret = service->getBatteryStats(SysfsType::CAPACITY_CURRENT);
            break;
        case 3:
            ret = (float)service->getBatteryStats(SysfsType::CAPACITY_CURRENT) *
                  (float)service->getBatteryStats(SysfsType::CAPACITY_MAX) / 100000;
            break;
        case 4:
            if (service->getBatteryStats(SysfsType::CURRENT) > 0) {
                ret = 1;
            } else {
                ret = 0;
            }
            break;
        case 5:
            ret = service->getBatteryStats(SysfsType::TEMP) / 10;
            break;
        case 6:
            ret = service->getBatteryStats(SysfsType::CURRENT);
            break;
        default:
            ret = -1;
            break;
    }
    return ret;
}
