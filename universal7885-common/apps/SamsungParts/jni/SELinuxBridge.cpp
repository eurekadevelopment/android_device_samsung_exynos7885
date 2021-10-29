#include <vendor/eureka/security/selinux/1.0/ISELinux.h>
#include <hidl/Status.h>
#include <hidl/LegacySupport.h>
#include <hardware/hardware.h>
#include <hidl/HidlSupport.h>
#include "jni.h"

using vendor::eureka::security::selinux::V1_0::ISELinux;
using vendor::eureka::security::selinux::V1_0::Enable;
using android::sp;

extern "C"
JNIEXPORT jint JNICALL
Java_com_eurekateam_samsungextras_interfaces_SELinux_getSELinux(JNIEnv *env, jclass clazz) {
      android::sp<ISELinux> service = ISELinux::getService();
      int ret = service->readSELinuxstats();
      return ret;
}
