#include <memory>
#include <string_view>
#include <unordered_map>
#include <fstream>
#include <sstream>
#include <filesystem> // C++17

#include <health/utils.h>
#include <health2impl/Health.h>
#include <hidl/Status.h>

using ::android::sp;
using ::android::hardware::Return;
using ::android::hardware::Void;
using ::android::hardware::hidl_vec;
using ::android::hardware::health::InitHealthdConfig;
using ::android::hardware::health::V2_1::IHealth;
using ::android::hardware::health::V2_0::Result;
using ::android::hardware::health::V1_0::BatteryStatus;
using ::android::hidl::base::V1_0::IBase;
using namespace std::literals;
namespace fs = std::filesystem;

static int charge_cnt = 0;

enum battery_stats {
	CHARGE_CNT,
	CURRENT_NOW,
	CURRENT_AVG,
	CAPACITY,
	CHARGE_ENABLED,
	FULL
};

std::unordered_map<battery_stats, std::string> battery_sysfs =  {
	{ CHARGE_CNT, "/efs/FactoryApp/batt_cable_count" },
	{ CURRENT_NOW, "/sys/devices/platform/battery/power_supply/battery/current_now" },
	{ CURRENT_AVG, "/sys/devices/platform/battery/power_supply/battery/current_avg" },
	{ CAPACITY, "/sys/devices/platform/battery/power_supply/battery/charge_full" },
	{ CHARGE_ENABLED, "/sys/devices/platform/battery/power_supply/battery/batt_slate_mode" },
	{ FULL, "/sys/devices/platform/battery/power_supply/battery/capacity" },
};

struct callBack {
	Result result;
	std::string value;
};

namespace android {
namespace hardware {
namespace health {
namespace V2_1 {
namespace implementation {

// android::hardware::health::V2_1::implementation::Health implements most
// defaults. Uncomment functions that you need to override.
class HealthImpl : public Health {
  public:
    HealthImpl(std::unique_ptr<healthd_config>&& config)
        : Health(std::move(config)) {}
    
    static struct callBack ReadFile(const std::string sysfs, const std::string def) {
	std::string ret = def;
	std::ifstream file;
	file.open(sysfs);
	Result result = Result::SUCCESS;
	if (file.is_open()){
		getline(file, ret);
    		file.close();
	} else {
		result = Result::NOT_FOUND;
	}
	ALOGI ("%s: sysfs : %s, returns : %s", __func__, sysfs.c_str(), ret.c_str());
	struct callBack cb = { result, ret };
    	return cb;
    }

    static struct callBack ReadBattFile(battery_stats type, const std::string def) {
    	return ReadFile(battery_sysfs[type], def);
    }

    Return<void> getChargeCounter(getChargeCounter_cb _hidl_cb) {
    	struct callBack ret = ReadBattFile(CHARGE_CNT, "-1");
	_hidl_cb(ret.result, std::stoi(ret.value));
	return Void();
    }

    Return<void> getCurrentNow(getCurrentNow_cb _hidl_cb){
	struct callBack ret = ReadBattFile(CURRENT_NOW, "-1");
	_hidl_cb(ret.result, std::stoi(ret.value));
	return Void();
    }

    Return<void> getCurrentAverage(getCurrentAverage_cb _hidl_cb){
	struct callBack ret = ReadBattFile(CURRENT_NOW, "-1");
	_hidl_cb(ret.result, std::stoi(ret.value));
	return Void();
    }

    Return<void> getCapacity(getCapacity_cb _hidl_cb){
	struct callBack ret = ReadBattFile(CURRENT_NOW, "-1");
	_hidl_cb(ret.result, std::stoi(ret.value));
	return Void();
     }

     Return<void> getChargeStatus(getChargeStatus_cb _hidl_cb){
	struct callBack ret = ReadBattFile(CURRENT_NOW, "-1");
	Result result = ret.result;
	BatteryStatus batt = BatteryStatus::UNKNOWN;
	if (std::stoi(ret.value) > 0) {
		struct callBack res = ReadBattFile(FULL, "0");
		if (std::stoi(res.value) == 100){
			batt = BatteryStatus::FULL;
		} else if (std::stoi(res.value) > 0) {
			batt = BatteryStatus::CHARGING;
		}
		result = res.result;
	} else if (std::stoi(ret.value) < 0) {
		struct callBack res = ReadBattFile(CHARGE_ENABLED, "0");
		if (std::stoi(res.value) == 0){
			batt = BatteryStatus::DISCHARGING;
		} else if (std::stoi(res.value) == 1) {
			batt = BatteryStatus::NOT_CHARGING;
		}
		result = res.result;
	}
	_hidl_cb(result, batt);
	return Void();
     }

    // Return<void> getDiskStats(getDiskStats_cb _hidl_cb) override;
    // Return<void> getHealthInfo(getHealthInfo_cb _hidl_cb) override;

    // Functions introduced in Health HAL 2.1.
    // Return<void> getHealthConfig(getHealthConfig_cb _hidl_cb) override;
    // Return<void> getHealthInfo_2_1(getHealthInfo_2_1_cb _hidl_cb) override;
    // Return<void> shouldKeepScreenOn(shouldKeepScreenOn_cb _hidl_cb) override;

  protected:
    // A subclass can override this to modify any health info object before
    // returning to clients. This is similar to healthd_board_battery_update().
    // By default, it does nothing.
    // void UpdateHealthInfo(HealthInfo* health_info) override;
};

}  // namespace implementation
}  // namespace V2_1
}  // namespace health
}  // namespace hardware
}  // namespace android

extern "C" IHealth* HIDL_FETCH_IHealth(const char* instance) {
    using ::android::hardware::health::V2_1::implementation::HealthImpl;
    if (instance != "default"sv) {
        return nullptr;
    }
    auto config = std::make_unique<healthd_config>();
    InitHealthdConfig(config.get());

    // healthd_board_init(config.get());

    return new HealthImpl(std::move(config));
}
