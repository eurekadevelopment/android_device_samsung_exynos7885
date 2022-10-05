#include "jni.h"
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include <hidl/LegacySupport.h>
#include <hidl/Status.h>
#include <vendor/eureka/hardware/parts/1.0/IDisplayConfigs.h>

using android::sp;
using vendor::eureka::hardware::parts::V1_0::DisplaySys;
using vendor::eureka::hardware::parts::V1_0::IDisplayConfigs;
using vendor::eureka::hardware::parts::V1_0::Status;

static android::sp<IDisplayConfigs> service = IDisplayConfigs::getService();

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Display_setDT2W(JNIEnv /*env*/,
                                                             jclass /*clazz*/,
                                                             jboolean enable) {
  if (enable) {
    service->writeDisplay(Status::ENABLE, DisplaySys::DOUBLE_TAP);
  } else {
    service->writeDisplay(Status::DISABLE, DisplaySys::DOUBLE_TAP);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Display_setGloveMode(
    JNIEnv /**env*/, jclass /*clazz*/, jboolean enable) {
  if (enable) {
    service->writeDisplay(Status::ENABLE, DisplaySys::GLOVE_MODE);
  } else {
    service->writeDisplay(Status::DISABLE, DisplaySys::GLOVE_MODE);
  }
}
