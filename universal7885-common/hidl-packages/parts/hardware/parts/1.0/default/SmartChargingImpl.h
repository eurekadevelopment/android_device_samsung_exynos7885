#include "BatteryConstants.h"

#include <fstream>
#include <iostream>

#include <FileIO.h>

namespace vendor::eureka::hardware::parts::V1_0 {

class SmartChargeImpl {
public:
  SmartChargeImpl(int limit, int restart);
  void start(void);
  void stop(void);

  int charge_limit_cnt;
  int restart_cnt;

private:
  const int limit_percent;
  const int restart_percent;
};

} // namespace vendor::eureka::hardware::parts::V1_0

static inline void disableSysfs(const char *path) {
  FileIO::writeline(path, 0);
}

static inline void enableSysfs(const char *path) { FileIO::writeline(path, 1); }
