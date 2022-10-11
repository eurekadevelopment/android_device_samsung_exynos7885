#include "SwapCallbackImpl.h"

#include <android/binder_manager.h>

#include <aidl/vendor/eureka/hardware/parts/BnSwapOnData.h>

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_setSwapOn
(JNIEnv *env, jclass clazz, jboolean cb) {
	auto svc = ISwapOnData::fromBinder(ndk::SpAIBinder(AServiceManager_waitForService("vendor.eureka.hardware.parts.ISwapOnData/default")));
	svc->setSwapOn(cb ? ndk::SharedRefBase::make<SwapCallbackImpl>(env, clazz) : nullptr);
}
