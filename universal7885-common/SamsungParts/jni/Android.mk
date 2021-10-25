LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# Here we give our module name and source file(s)

LOCAL_SRC_FILES := BatteryBridge.cpp
LOCAL_MODULE    := libnativebridges

include $(BUILD_SHARED_LIBRARY)
