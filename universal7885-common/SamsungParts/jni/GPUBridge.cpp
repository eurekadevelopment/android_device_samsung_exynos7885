#include <vendor/eureka/hardware/gpu/1.0/IGpu.h>
#include <hidl/Status.h>
#include <hidl/LegacySupport.h>
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include "jni.h"

using vendor::eureka::hardware::gpu::V1_0::IGpu;
using vendor::eureka::hardware::gpu::V1_0::Enable;
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

extern "C"
JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_GPU_setGPU(JNIEnv *env, jclass clazz, jint enable) {
      android::sp<IGpu> service = IGpu::getService();
      if (enable == 1){
            service->setGpuWritable(Enable::ENABLE);
      }else{
            service->setGpuWritable(Enable::DISABLE);
      }
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_GPU_getGPU(JNIEnv *env, jclass clazz) {
      android::sp<IGpu> service = IGpu::getService();
      int ret = service->readGpustats();
      return ret;
}