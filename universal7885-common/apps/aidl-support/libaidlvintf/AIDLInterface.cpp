#include <sstream>
#include "AIDLInterface.h"

constexpr const char *kInterfacePrefix = "I";

std::string makeAIDLInterfaceString(const struct aidl_vintf *info)
{
	std::stringstream kInterfaceStrBuilder;
	kInterfaceStrBuilder << info->name;
	kInterfaceStrBuilder << ".";
	if (info->interface.rfind(kInterfacePrefix, 0) != 0)
	{
		kInterfaceStrBuilder << kInterfacePrefix;
	}
	kInterfaceStrBuilder << info->interface;
	kInterfaceStrBuilder << "/" << info->instance;
	return kInterfaceStrBuilder.str();
}
