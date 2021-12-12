#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/flashlight/1.0/IFlashlight.h>
#include "jni.h"

using android::sp;
using vendor::eureka::hardware::flashlight::V1_0::Device;
using vendor::eureka::hardware::flashlight::V1_0::Enable;
using vendor::eureka::hardware::flashlight::V1_0::IFlashlight;
using vendor::eureka::hardware::flashlight::V1_0::Number;

extern "C" JNIEXPORT void JNICALL Java_com_eurekateam_samsungextras_interfaces_Flashlight_setFlash(
        JNIEnv* env, __unused jobject obj, jint value) {
    android::sp<IFlashlight> service = IFlashlight::getService();
    service->setFlashlightEnable(Enable::ENABLE);
    switch (value) {
        case 1:
            service->setFlashlightWritable(Number::ONEUI);
            break;
        case 2:
            service->setFlashlightWritable(Number::TWOUI);
            break;
        case 3:
            service->setFlashlightWritable(Number::THREEUI);
            break;
        case 4:
            service->setFlashlightWritable(Number::FOURUI);
            break;
        case 5:
            service->setFlashlightWritable(Number::FIVEUI);
            break;
        case 6:
            service->setFlashlightWritable(Number::SIXUI);
            break;
        case 7:
            service->setFlashlightWritable(Number::SEVENUI);
            break;
        case 8:
            service->setFlashlightWritable(Number::EIGHTUI);
            break;
        case 9:
            service->setFlashlightWritable(Number::NINEUI);
            break;
        case 10:
            service->setFlashlightWritable(Number::TENUI);
            break;
        default:
            break;
    }
}
extern "C" JNIEXPORT jint JNICALL Java_com_eurekateam_samsungextras_interfaces_Flashlight_getFlash(
        JNIEnv* env, jobject clazz, jint isA10) {
    android::sp<IFlashlight> service = IFlashlight::getService();
    int ret;
    if (isA10 == 1) {
        ret = service->readFlashlightstats(Device::A10);
    } else {
        ret = service->readFlashlightstats(Device::NOTA10);
    }
    return ret;
}
