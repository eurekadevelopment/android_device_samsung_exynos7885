#define LOG_TAG "android.hardware.radio@1.4-service.samsung"

#include <android/hardware/radio/1.4/IRadio.h>
#include <hidl/HidlTransportSupport.h>
#include <vendor/samsung/hardware/radio/2.1/ISehRadio.h>

using android::OK;
using android::sp;
using android::status_t;
using android::hardware::configureRpcThreadpool;
using android::hardware::joinRpcThreadpool;
using android::hardware::radio::V1_4::IRadio;
using vendor::samsung::hardware::radio::V2_1::ISehRadio;

#define SLOT_1 "slot1"
#define SLOT_2 "slot2"

static void registerInstance(const char *name) {
  sp<ISehRadio> sehRadio = ISehRadio::getService(name);
  sp<IRadio> radio = IRadio::castFrom(static_cast<sp<IRadio>>(sehRadio));
  if (radio) {
    status_t status = radio->registerAsService(name);
    ALOGW_IF(status != OK, "Could not register IRadio v1.4 %s", name);
  } else {
    ALOGE("Failed to cast to Radio V1.4");
  }
}

int main(void) {
  configureRpcThreadpool(1, true);

  registerInstance(SLOT_1);
  registerInstance(SLOT_2);
  ALOGD("Default service is ready.");

  joinRpcThreadpool();
  return 1;
}
