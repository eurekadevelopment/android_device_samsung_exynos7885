#include <sstream>
#include <vector>

#ifdef LOG_TAG

#include <cstring>
#include <log/log.h>

#define __FILENAME__ (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : __FILE__)

#define make_str(a, ...) _make_str(__FILENAME__, __LINE__, a, ##__VA_ARGS__).c_str()

// Helpers to avoid -Wformat-security
#define LOG_E(fmt, ...) ALOGE("%s", make_str(fmt, ##__VA_ARGS__))
#define LOG_W(fmt, ...) ALOGW("%s", make_str(fmt, ##__VA_ARGS__))
#define LOG_I(fmt, ...) ALOGI("%s", make_str(fmt, ##__VA_ARGS__))
#define LOG_D(fmt, ...) ALOGD("%s", make_str(fmt, ##__VA_ARGS__))
#define LOG_V(fmt, ...) ALOGV("%s", make_str(fmt, ##__VA_ARGS__))

template <typename... Args>
std::string _make_str(const std::string& filename, int line, const std::string& fmt, Args... args) {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wformat-security"
  const int size = std::snprintf(nullptr, 0, fmt.c_str(), args...);
  std::vector<char> buf(size + 1);
  std::snprintf(buf.data(), buf.size(), fmt.c_str(), args...);
#pragma clang diagnostic pop
  std::stringstream ss;
  ss << "[" << filename << ":" << line << "] " << std::string(buf.begin(), buf.end());
  return ss.str();
}

#ifndef __NEED_VERBOSE_LOG__

#undef LOG_I
#undef LOG_D
#undef LOG_V

#define LOG_I(...) do {} while(0)
#define LOG_D(...) do {} while(0)
#define LOG_V(...) do {} while(0)

#endif

#else

#warning LOG_TAG is not defined, disabling logging.

#define LOG_E(...) do {} while(0)
#define LOG_W(...) do {} while(0)
#define LOG_I(...) do {} while(0)
#define LOG_D(...) do {} while(0)
#define LOG_V(...) do {} while(0)

#endif
