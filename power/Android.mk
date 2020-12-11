LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := android.hardware.power@1.0-service.exynos.rc
LOCAL_MODULE_TAGS  := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/init
LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)

LOCAL_PATH := hardware/samsung/hidl/power

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
    Power.cpp \
    service.cpp

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/include \
    hardware/samsung/hidl/light/include

LOCAL_SHARED_LIBRARIES := \
    libbase \
    libbinder \
    libhidlbase \
    libutils \
    android.hardware.power@1.0 \
    vendor.lineage.power@1.0

LOCAL_STATIC_LIBRARIES := libc++fs

LOCAL_MODULE := android.hardware.power@1.0-service.universal7904
LOCAL_MODULE_STEM := android.hardware.power@1.0-service.exynos
LOCAL_REQUIRED_MODULES := $(LOCAL_MODULE_STEM).rc
LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := samsung

include $(BUILD_EXECUTABLE)
