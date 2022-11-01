#include <sstream>
#include <vector>

#define make_str(a, ...) _make_str(__FILE__, __LINE__, a, ##__VA_ARGS__).c_str()

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
