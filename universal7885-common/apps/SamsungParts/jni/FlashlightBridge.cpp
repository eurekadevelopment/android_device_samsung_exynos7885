#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/IFlashLight.h>
#include "jni.h"

using android::sp;
using vendor::eureka::hardware::parts::V1_0::Device;
using vendor::eureka::hardware::parts::V1_0::Value;
using vendor::eureka::hardware::parts::V1_0::IFlashLight;
using vendor::eureka::hardware::parts::V1_0::Number;

extern "C" JNIEXPORT void JNICALL Java_com_eurekateam_samsungextras_interfaces_Flashlight_setFlash(
        JNIEnv* env, __unused jobject obj, jint value) {
    android::sp<IFlashLight> service = IFlashLight::getService();
    service->setFlashlightEnable(Number::ENABLE);
    switch (value) {
        case 1:
            service->setFlashlightWritable(Value::ONEUI);
            break;
        case 2:
            service->setFlashlightWritable(Value::TWOUI);
            break;
        case 3:
            service->setFlashlightWritable(Value::THREEUI);
            break;
        case 4:
            service->setFlashlightWritable(Value::FOURUI);
            break;
        case 5:
            service->setFlashlightWritable(Value::FIVEUI);
            break;
        case 6:
            service->setFlashlightWritable(Value::SIXUI);
            break;
        case 7:
            service->setFlashlightWritable(Value::SEVENUI);
            break;
        case 8:
            service->setFlashlightWritable(Value::EIGHTUI);
            break;
        case 9:
            service->setFlashlightWritable(Value::NINEUI);
            break;
        case 10:
            service->setFlashlightWritable(Value::TENUI);
            break;
        default:
            break;
    }
}
extern "C" JNIEXPORT jint JNICALL Java_com_eurekateam_samsungextras_interfaces_Flashlight_getFlash(
        JNIEnv* env, jobject clazz, jint isA10) {
    android::sp<IFlashLight> service = IFlashLight::getService();
    int ret;
    if (isA10 == 1) {
        ret = service->readFlashlightstats(Device::A10);
    } else {
        ret = service->readFlashlightstats(Device::NOTA10);
    }
    return ret;
}
