#define LOG_TAG "libFileIO"

#include <fstream>
#include <string>

#include <LogFormat.h>

namespace FileIO {

constexpr int ERR = -1;

std::string readline(const char *path) {
  std::ifstream file;
  std::string value;
  file.open(path);
  LOG_D("%s: Opening %s", __func__, path);
  if (file.is_open()) {
    getline(file, value);
    file.close();
  } else {
    LOG_E("%s: Failed to open %s", __func__, path);
    value = std::to_string(ERR);
  }
  return value;
}

int readint(const char *path) {
  const std::string value = readline(path);
  try {
    return stoi(value);
  } catch (std::invalid_argument const &ex) {
    LOG_E("%s: stoi(): invalid argument: for %s", __func__, value.c_str());
  } catch (std::out_of_range const &ex) {
    LOG_E("%s: stoi(): out of range: for %s", __func__, value.c_str());
  }
  return ERR;
}

void writeline(const char *path, const std::string& data) {
  std::ofstream file;
  file.open(path);
  LOG_D("%s: Opening %s, will write '%s'", __func__, path, data.c_str());
  if (file.is_open()) {
    file << data;
    file.close();
    return;
  }
  LOG_E("%s: Failed to open %s", __func__, path);
}

void writeline(const char *path, const int data) {
  writeline(path, std::to_string(data));
}

} // namespace FileIO
