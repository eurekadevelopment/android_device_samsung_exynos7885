#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/gpu/1.0/IGpu.h>
#include "jni.h"

using android::sp;
using vendor::eureka::hardware::gpu::V1_0::Enable;
using vendor::eureka::hardware::gpu::V1_0::IGpu;

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_GPU_setGPU(JNIEnv* env, jclass clazz, jint enable) {
    android::sp<IGpu> service = IGpu::getService();
    if (enable == 1) {
        service->setGpuWritable(Enable::ENABLE);
    } else {
        service->setGpuWritable(Enable::DISABLE);
    }
}
extern "C" JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_GPU_getGPU(JNIEnv* env, jclass clazz) {
    android::sp<IGpu> service = IGpu::getService();
    int ret = service->readGpustats();
    return ret;
}
