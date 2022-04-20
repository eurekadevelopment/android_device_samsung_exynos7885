#include "jni.h"
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/IFlashBrightness.h>

using android::sp;
using vendor::eureka::hardware::parts::V1_0::Device;
using vendor::eureka::hardware::parts::V1_0::IFlashBrightness;
using vendor::eureka::hardware::parts::V1_0::Number;
using vendor::eureka::hardware::parts::V1_0::Value;

static android::sp<IFlashBrightness> service = IFlashBrightness::getService();

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Flashlight_setEnabled(
    JNIEnv *env, __unused jobject obj, jboolean enabled) {
  if (enabled) {
    service->setFlashlightEnable(Number::ENABLE);
  } else {
    service->setFlashlightEnable(Number::DISABLE);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Flashlight_setFlash(
    JNIEnv *env, __unused jobject obj, jint value) {
  service->setFlashlightWritable(static_cast<Value>(value));
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Flashlight_getFlash(JNIEnv *env,
                                                                 jobject clazz,
                                                                 jint isA10) {
  int ret;
  if (isA10 == 1) {
    ret = service->readFlashlightstats(Device::A10);
  } else {
    ret = service->readFlashlightstats(Device::NOTA10);
  }
  return ret;
}
