#include <iostream>
#include <fstream>
#include <vector>
#include <filesystem>
using namespace std;
std::vector<std::string> get_directories(const std::string& s)
{
	std::vector<std::string> r;
	for(auto& p : std::filesystem::recursive_directory_iterator(s))
		if (p.is_directory())
			r.push_back(p.path().string());
	return r;
}
int main(){
	std::vector<std::string> vendors = get_directories("vendor");
	for (int i = 0; i < vendors.size(); i++){
		std::string name = vendors[i].substr(7, vendors[i].length());
		name = name.substr(0, name.find('/'));
		if (name != "qcom" && name != "nxp" && name != "samsung"
				&& name != "gapps" && name != "google" &&
				name != "gms" && name != "codeaurora"){
			ofstream output;
			output.open("device/samsung/universal7885-common/vendor_name");
			output << name;
			output.close();
		}
	}
	return 0;
}
