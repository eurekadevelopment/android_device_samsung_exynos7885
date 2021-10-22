#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <iostream>
#define LOG_TAG "samsungparts@1.0-service"
#include <log/log.h>
#define SYSTEM_ID 1000
void chmod(std::string filename){
const char *file = filename.c_str();
if (chmod(file, 664) < 0){
ALOGE("Failed to chmod");
}
}
void chown(std::string filename){
const char *file = filename.c_str();
if (chown(file, SYSTEM_ID, SYSTEM_ID) < 0) {
ALOGE("Failed to chown");
}
}
int main(){
// GPU TMU
chmod("/sys/devices/platform/11500000.mali/tmu");
chown("/sys/devices/platform/11500000.mali/tmu");

// Flashlight
chmod("/sys/class/camera/flash/torch_brightness_lvl");
chown("/sys/class/camera/flash/torch_brightness_lvl");

// Flashlight Enable
chmod("/sys/class/camera/flash/torch_brightness_lvl_enable");
chown("/sys/class/camera/flash/torch_brightness_lvl_enable");

// Charging Switch
chmod("/sys/devices/platform/battery/power_supply/battery/batt_slate_mode");
chown("/sys/devices/platform/battery/power_supply/battery/batt_slate_mode");

// FastCharge
chmod("/sys/class/sec/switch/afc_disable");
chown("/sys/class/sec/switch/afc_disable");

// SELinux
chmod("/sys/fs/selinux/enforce");
chown("/sys/fs/selinux/enforce");

}
