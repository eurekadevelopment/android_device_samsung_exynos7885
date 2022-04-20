#include "jni.h"
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/ISwapOnData.h>

using android::sp;
using vendor::eureka::hardware::parts::V1_0::ISwapOnData;

static sp<ISwapOnData> service = ISwapOnData::getService();

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_setSize(JNIEnv *env,
                                                          jclass clazz,
                                                          jint size) {
  service->setSwapSize(size);
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_setSwapOn(JNIEnv *env,
                                                            jclass clazz,
                                                            jboolean enable) {
  if (enable) {
    service->setSwapOn();
  } else {
    service->setSwapOff();
  }
}
