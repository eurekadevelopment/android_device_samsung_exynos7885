/*
 * Copyright 2021 Soo Hwan Na "Royna"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "bootlogger"

#include <android-base/properties.h>
#include <errno.h>
#include <fcntl.h>
#include <log/log.h>
#include <unistd.h>

#include <atomic>
#include <fstream>
#include <functional>
#include <iostream>
#include <regex>
#include <sstream>
#include <string>
#include <thread>
#include <vector>

using android::base::WaitForProperty;

#define PLOGE(fmt, ...) ALOGE(fmt ": %s", ##__VA_ARGS__, strerror(errno))

struct LoggerContext;

// Base context for outputs with file
struct OutputContext {
  // Fetch out file name of this context.
  // Note that .txt suffix is auto appended.
  virtual std::string getFileName(void) const = 0;

  /**
   * Returns absolute path of output
   * Basically a wrapper of [getFileName]
   *
   * @return absolute path of out
   */
  std::string getOutFilePath(void) const {
    static std::string kLogDir = "/data/debug/";
    return kLogDir + getFileName() + ".txt";
  }

  /**
   * Open outfilestream.
   */
  bool openOutput(void) {
    auto out = getOutFilePath();
    ALOGI("%s: Open %s", __func__, out.c_str());
    std::remove(out.c_str());
    ofs = std::ofstream(out);
    valid = ofs.good();
    if (!valid) PLOGE("%s: Failed to open %s", __func__, out.c_str());
    return valid;
  }

  /**
   * Writes the string to this context's file
   *
   * @param string data
   */
  void writeStringToOutput(const std::string &data) {
    ofs << data << std::endl;
  }

  operator bool() const { return valid; }

  virtual ~OutputContext() {}

 private:
  std::ofstream ofs;
  bool valid = false;
};

/**
 * Filter support to LoggerContext's stream and outputting to a file.
 */
struct LogFilterContext : OutputContext {
  // Function to be invoked to filter
  virtual bool filter(const std::string &line) const = 0;
  virtual std::string getFilterName(void) const = 0;
  std::string getFileName(void) const override;
  void setParent(LoggerContext *_parent) { parent = _parent; }
  ~LogFilterContext() override = default;

 private:
  LoggerContext *parent = nullptr;
};

struct LoggerContext : OutputContext {
  /**
   * Opens the log file stream handle
   *
   * @return FILE* handle
   */
  virtual FILE *openSource(void) = 0;

  /**
   * Closes log file stream handle and does cleanup
   *
   * @param fp The file stream to close and cleanup. NonNull.
   */
  virtual void closeSource(FILE *fp) = 0;

  /**
   * Register a LogFilterContext to this stream.
   *
   * @param ctx The context to register
   */
  void registerLogFilter(LogFilterContext *ctx) {
    if (ctx) {
      filters.emplace_back(ctx);
      ctx->setParent(this);
    }
  }

  /**
   * Start the associated logger
   *
   * @param run Pointer to run/stop control variable
   */
  void startLogger(std::atomic_bool *run) {
    char buf[1024] = {0};
    auto fp = openSource();
    if (fp) {
      int fd = fileno(fp);
      int flags = fcntl(fd, F_GETFL);
      if (!(flags & O_NONBLOCK)) {
        flags |= O_NONBLOCK;
        fcntl(fd, F_SETFL, flags);
      }
      bool ret = openOutput();
      if (ret) {
        for (auto &f : filters) {
          f->openOutput();
        }
        while (*run) {
          auto ret = fgets(buf, sizeof(buf), fp);
          std::istringstream ss(buf);
          std::string line;
          if (ret) {
            while (getline(ss, line)) {
              for (auto &f : filters) {
                if (*f && f->filter(line)) f->writeStringToOutput(line);
              }
              writeStringToOutput(line);
            }
          }
        }
        // ofstream will auto close
      } else {
        PLOGE("[Context %s] Open output '%s'", getFileName().c_str(),
              getOutFilePath().c_str());
      }
      closeSource(fp);
    } else {
      PLOGE("[Context %s] Open source", getFileName().c_str());
    }
  }
  virtual ~LoggerContext(){};

 private:
  std::vector<LogFilterContext *> filters;
};

// Due to referencing LoggerContext::getFileName()
std::string LogFilterContext::getFileName(void) const {
  return getFilterName() +
         (parent ? std::string(".") + parent->getFileName() : "");
}

// DMESG
struct DmesgContext : LoggerContext {
  FILE *openSource(void) override { return fopen("/proc/kmsg", "r"); }
  void closeSource(FILE *fp) override { fclose(fp); }
  std::string getFileName() const override { return "kmsg"; }
  ~DmesgContext() override = default;
};

// Logcat
struct LogcatContext : LoggerContext {
  FILE *openSource(void) override { return popen("/system/bin/logcat", "r"); }
  void closeSource(FILE *fp) override { pclose(fp); }
  std::string getFileName() const override { return "logcat"; }
  ~LogcatContext() override = default;
};

// Filters - AVC
struct AvcFilterContext : LogFilterContext {
  bool filter(const std::string &line) const override {
    return std::regex_search(line, std::regex(R"(avc:\sdenied\s\{\s\w+\s\})"));
  }
  std::string getFilterName(void) const override { return "avc"; }
  ~AvcFilterContext() override = default;
};

int main(void) {
  std::vector<std::thread> threads;
  std::atomic_bool run;

  DmesgContext kDmesgCtx;
  LogcatContext kLogcatCtx;
  AvcFilterContext kDmesgAvcFilter, kLogcatAvcFilter;

  kDmesgCtx.registerLogFilter(&kDmesgAvcFilter);
  kLogcatCtx.registerLogFilter(&kLogcatAvcFilter);

  run = true;
  threads.emplace_back(std::thread([&] { kDmesgCtx.startLogger(&run); }));
  threads.emplace_back(std::thread([&] { kLogcatCtx.startLogger(&run); }));
  WaitForProperty("sys.boot_completed", "1");
  run = false;
  for (auto &i : threads) i.join();
  return 0;
}
