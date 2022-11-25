#include "SwapCallbackImpl.h"

#include <android/binder_manager.h>

#include <aidl/vendor/eureka/hardware/parts/BnSwapOnData.h>

#include <AIDLInterface.h>

extern "C" JNIEXPORT void JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_setSwapOn
(JNIEnv *env, jclass clazz, jboolean cb) {
	static const struct aidl_vintf SwapVintf = {
		.name = "vendor.eureka.hardware.parts",
		.interface = "ISwapOnData",
	};
	auto svc = ISwapOnData::fromBinder(ndk::SpAIBinder(AServiceManager_waitForService(MKAIDLSTR(&SwapVintf))));
	svc->setSwapOn(cb ? ndk::SharedRefBase::make<SwapCallbackImpl>(env, clazz) : nullptr);
}
