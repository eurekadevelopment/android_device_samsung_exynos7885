#include <string>

struct aidl_vintf {
	const std::string name; // android.hardware.camera.provider
	const std::string interface; // ICameraProvider
	const std::string instance = "default"; // default
};

std::string makeAIDLInterfaceString(const struct aidl_vintf *info);

#define MKAIDLSTR(info) makeAIDLInterfaceString(info).c_str()
