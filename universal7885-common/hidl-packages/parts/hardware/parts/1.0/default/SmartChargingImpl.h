#include "BatteryConstants.h"

#include <iostream>
#include <fstream>

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

}

static inline void disableSysfs(const char* path) {
	const std::string pathcpp = std::string(path);
	std::ofstream file;
	file.open(pathcpp);
	if (file.is_open()) {
		file << 0;
		file.close();
	}
}

static inline void enableSysfs(const char* path) {
	const std::string pathcpp = std::string(path);
	std::ofstream file;
	file.open(pathcpp);
	if (file.is_open()) {
		file << 1;
		file.close();
	}
}

static inline std::string readFile(const char* path) {
	const std::string pathcpp = std::string(path);
	std::ifstream file;
	file.open(pathcpp);
	std::string result;
	if (file.is_open()) {
		getline(file, result);
		file.close();
	}
	return result;
}
