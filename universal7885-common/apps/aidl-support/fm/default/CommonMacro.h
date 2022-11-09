#pragma once

#include <android-base/logging.h>

#define RETURN_IF_FAILED_LOCK                                                  \
  ({                                                                           \
    if (!lock.try_lock_for(std::chrono::milliseconds(100)))                    \
      return ::ndk::ScopedAStatus::fromServiceSpecificError(-ETIME);           \
  })

#define NOT_SUPPORTED                                                          \
  ({                                                                           \
    LOG(ERROR) << __func__ << ": Attempted to invoke unsupported operation";   \
    return ::ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);  \
  })
