#include <filesystem>
#include <jni.h>
#include <sys/stat.h>

namespace fs = std::filesystem;

double roundDouble(double origin) {
  int first = static_cast<int>(origin);
  double second = origin - first;
  second = static_cast<int>(second * 10);
  return static_cast<double>(first *= 10 + second) / 10;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_getFreeSpace(JNIEnv *env,
                                                               jclass clazz) {
  fs::space_info data = fs::space("/data");
  auto freespace = data.free / 1024 / 1024;
  auto result = static_cast<double>(freespace) / 1024;
  return roundDouble(result);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_eurekateam_samsungextras_interfaces_Swap_getSwapSize(JNIEnv *env,
                                                              jclass clazz) {
  struct stat stat_buf;
  int rc = stat("/data/swap/swapfile", &stat_buf);
  return rc == 0 ? stat_buf.st_size / 1024 / 1024 : -1;
}
