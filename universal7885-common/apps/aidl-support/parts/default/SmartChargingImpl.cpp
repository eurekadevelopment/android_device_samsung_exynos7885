#include "SmartChargingImpl.h"

#include <chrono>
#include <thread>

namespace aidl::vendor::eureka::hardware::parts {

static std::thread *monitor;
static bool shouldRun = false;

SmartChargeImpl::SmartChargeImpl(int limit, int restart)
    : limit_percent(limit), restart_percent(restart) {
  charge_limit_cnt = 0;
  restart_cnt = 0;
}

static void battery_monitor(const int limit_percent, const int restart_percent,
                            int *charge_limit_cnt, int *restart_cnt) {
  while (shouldRun) {
    auto batt = FileIO::readline(BATTERY_CAPACITY_CURRENT);
    if (batt >= limit_percent) {
      disableSysfs(BATTERY_CHARGE);
      *charge_limit_cnt += 1;
    } else if (batt <= restart_percent) {
      enableSysfs(BATTERY_CHARGE);
      *restart_cnt += 1;
    }
    std::this_thread::sleep_for(std::chrono::seconds(5));
  }
}

void SmartChargeImpl::start(void) {
  shouldRun = true;
  monitor = new std::thread(battery_monitor, limit_percent, restart_percent,
                            &charge_limit_cnt, &restart_cnt);
}

void SmartChargeImpl::stop(void) {
  shouldRun = false;
  monitor = nullptr;
}
} // namespace vendor::eureka::hardware::parts::V1_0
