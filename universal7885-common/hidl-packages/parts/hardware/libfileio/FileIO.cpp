#define LOG_TAG "libFileIO"

#include <log/log.h>

#include <fstream>
#include <memory>
#include <string>

namespace FileIO {

constexpr const int EXIT_ERR = -1;

int readline(const char *path) {
  std::ifstream file;
  std::string value;
  file.open(path);
  ALOGD("%s: Opening %s", __func__, path);
  if (file.is_open()) {
    getline(file, value);
    file.close();
  } else {
    ALOGE("%s: Failed to open %s", __func__, path);
    return EXIT_ERR;
  }
  try {
    return stoi(value);
  } catch (std::invalid_argument const &ex) {
    ALOGE("%s: stoi(): invalid argument: for %s", __func__, value);
  } catch (std::out_of_range const &ex) {
    ALOGE("%s: stoi(): out of range: for %s", __func__, value);
  }
  return EXIT_ERR;
}

template <typename T>
void writeline(const char *path, const T data) {
  std::ofstream file;
  file.open(path);
  ALOGD("%s: Opening %s", __func__, path);
  if (file.is_open()) {
    file << data;
    file.close();
    return;
  }
  ALOGE("%s: Failed to open %s", __func__, path);
}

} // namespace FileIO
