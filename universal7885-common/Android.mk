LOCAL_PATH := $(call my-dir)

ifneq ($(filter a10dd a10 a20 a20e a30 a30s a40, $(TARGET_DEVICE)),)

  subdir_makefiles=$(call first-makefiles-under,$(LOCAL_PATH))
  $(foreach mk,$(subdir_makefiles),$(info including $(mk) ...)$(eval include $(mk)))

include $(CLEAR_VARS)

LIBGLES_MALI_LIBRARY := /vendor/lib/egl/libGLES_mali.so
LIBGLES_MALI64_LIBRARY := /vendor/lib64/egl/libGLES_mali.so

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a10dd a10 a20 a20e))
VULKAN_SYMLINK := $(TARGET_OUT_VENDOR)/lib/hw/vulkan.universal7884B.so
$(VULKAN_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib/hw/vulkan.universal7884B.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI_LIBRARY) $@

ALL_DEFAULT_INSTALLED_MODULES += $(VULKAN_SYMLINK)
endif

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a10 a20 a20e))
VULKAN64_SYMLINK := $(TARGET_OUT_VENDOR)/lib64/hw/vulkan.universal7884B.so
$(VULKAN64_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib64/hw/vulkan.universal7884B.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI64_LIBRARY) $@

ALL_DEFAULT_INSTALLED_MODULES += $(VULKAN64_SYMLINK)
endif

include $(CLEAR_VARS)

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a30 a30s a40))
VULKAN_SYMLINK := $(TARGET_OUT_VENDOR)/lib/hw/vulkan.universal7904.so
$(VULKAN_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib/hw/vulkan.universal7904.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI_LIBRARY) $@

VULKAN64_SYMLINK := $(TARGET_OUT_VENDOR)/lib64/hw/vulkan.universal7904.so
$(VULKAN64_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib64/hw/vulkan.universal7904.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI64_LIBRARY) $@

ALL_DEFAULT_INSTALLED_MODULES += \
	$(VULKAN_SYMLINK) \
	$(VULKAN64_SYMLINK)
endif

include $(CLEAR_VARS)

LIBOPENCL_SYMLINK := $(TARGET_OUT_VENDOR)/lib/libOpenCL.so
$(LIBOPENCL_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib/libOpenCL.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf libOpenCL.so.1 $@

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a10 a20 a20e a30 a30s a40))
LIBOPENCL64_SYMLINK := $(TARGET_OUT_VENDOR)/lib64/libOpenCL.so
$(LIBOPENCL64_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib64/libOpenCL.so symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf libOpenCL.so.1 $@

ALL_DEFAULT_INSTALLED_MODULES += $(LIBOPENCL64_SYMLINK)
endif

LIBOPENCL1_SYMLINK := $(TARGET_OUT_VENDOR)/lib/libOpenCL.so.1
$(LIBOPENCL1_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib/libOpenCL.so.1 symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf libOpenCL.so.1.1 $@

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a10 a20 a20e a30 a30s a40))
LIBOPENCL641_SYMLINK := $(TARGET_OUT_VENDOR)/lib64/libOpenCL.so.1
$(LIBOPENCL641_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib64/libOpenCL.so.1 symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf libOpenCL.so.1.1 $@

ALL_DEFAULT_INSTALLED_MODULES += $(LIBOPENCL641_SYMLINK)
endif

LIBOPENCL11_SYMLINK := $(TARGET_OUT_VENDOR)/lib/libOpenCL.so.1.1
$(LIBOPENCL11_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib/libOpenCL.so.1.1 symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI_LIBRARY) $@

ifeq ($(TARGET_DEVICE), $(filter $(TARGET_DEVICE),a10 a20 a20e a30 a30s a40))
LIBOPENCL6411_SYMLINK := $(TARGET_OUT_VENDOR)/lib64/libOpenCL.so.1.1
$(LIBOPENCL6411_SYMLINK): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating lib64/libOpenCL.so.1.1 symlink: $@"
	@mkdir -p $(dir $@)
	$(hide) ln -sf $(LIBGLES_MALI64_LIBRARY) $@

ALL_DEFAULT_INSTALLED_MODULES += $(LIBOPENCL6411_SYMLINK)
endif

ALL_DEFAULT_INSTALLED_MODULES += \
	$(LIBOPENCL_SYMLINK) \
	$(LIBOPENCL1_SYMLINK) \
	$(LIBOPENCL11_SYMLINK)
endif
