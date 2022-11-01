#define LOG_TAG "libFileIO"

#include <log/log.h>

#include <fstream>
#include <string>

#include <LogFormat.h>

namespace FileIO {

constexpr const int EXIT_ERR = -1;

int readline(const char *path) {
  std::ifstream file;
  std::string value;
  file.open(path);
  ALOGD(make_str("%s: Opening %s", __func__, path));
  if (file.is_open()) {
    getline(file, value);
    file.close();
  } else {
    ALOGE(make_str("%s: Failed to open %s", __func__, path));
    return EXIT_ERR;
  }
  try {
    return stoi(value);
  } catch (std::invalid_argument const &ex) {
    ALOGE(make_str("%s: stoi(): invalid argument: for %s", __func__, value.c_str()));
  } catch (std::out_of_range const &ex) {
    ALOGE(make_str("%s: stoi(): out of range: for %s", __func__, value.c_str()));
  }
  return EXIT_ERR;
}

void writeline(const char *path, const std::string& data) {
  std::ofstream file;
  file.open(path);
  ALOGD(make_str("%s: Opening %s, will write '%s'", __func__, path, data.c_str()));
  if (file.is_open()) {
    file << data;
    file.close();
    return;
  }
  ALOGE(make_str("%s: Failed to open %s", __func__, path));
}

void writeline(const char *path, const int data) {
  writeline(path, std::to_string(data));
}

} // namespace FileIO
