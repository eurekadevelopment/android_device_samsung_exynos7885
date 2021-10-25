#include <vendor/eureka/hardware/flashlight/1.0/IFlashlight.h>
#include <hidl/Status.h>
#include <hidl/LegacySupport.h>
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include "jni.h"

using vendor::eureka::hardware::battery::V1_0::IFlashlight;
using vendor::eureka::hardware::battery::V1_0::Device;
using vendor::eureka::hardware::battery::V1_0::Enable;
using vendor::eureka::hardware::battery::V1_0::Number;
using android::sp;

extern "C"
JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Flashlight_setFlash(JNIEnv *env, __unused jclass obj,
                                                                 jint value) {
      android::sp<IFlashlight> service = IFlashlight::getService();
      service->setFlashlightEnable(Enable::ENABLE);
      switch (value) {
          case 1:
                service->setFlashlightWritable(Number::ONEUI);
          case 2:
                service->setFlashlightWritable(Number::TWOUI);
          case 3:
                service->setFlashlightWritable(Number::THREEUI);
          case 4:
                service->setFlashlightWritable(Number::FOURUI);
          case 5:
                service->setFlashlightWritable(Number::FIVEUI);
          case 6:
                service->setFlashlightWritable(Number::SIXUI);
          case 7:
                service->setFlashlightWritable(Number::SEVENUI);
          case 8:
                service->setFlashlightWritable(Number::EIGHTUI);
          case 9:
                service->setFlashlightWritable(Number::NINEUI);
          case 10:
                service->setFlashlightWritable(Number::TENUI);

      }
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_Flashlight_getFlash(JNIEnv *env, jclass clazz,
                                                                 jint isA10) {
      android::sp<IFlashlight> service = IFlashlight::getService();
      int ret;
      if(isA10 == 1) {
            ret = service->readFlashlightstats(Device::A10);
      }else{
            ret = service->readFlashlightstats(Device::NOTA10);
      }
      return ret;
}