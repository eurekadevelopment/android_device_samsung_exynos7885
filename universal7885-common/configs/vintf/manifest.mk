MANIFEST_PATH := device/samsung/universal7885-common/configs/vintf

DEVICE_MANIFEST_FILE := $(MANIFEST_PATH)/manifest.xml
DEVICE_MATRIX_FILE := $(MANIFEST_PATH)/compatibility_matrix.xml
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE := \
    $(MANIFEST_PATH)/device_framework_matrix.xml

LINEAGE_MANIFEST := $(wildcard vendor/*/config/device_framework_matrix.xml)
ifneq ($(LINEAGE_MANIFEST),)
ifeq ($(TARGET_DEVICE),a30s)
DEVICE_MANIFEST_FILE += $(MANIFEST_PATH)/lineage_manifest_a30s.xml
else
DEVICE_MANIFEST_FILE += $(MANIFEST_PATH)/lineage_manifest.xml
endif
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += $(LINEAGE_MANIFEST)
endif

ODM_MANIFEST_SKUS += NFC
ODM_MANIFEST_NFC_FILES := $(MANIFEST_PATH)/manifest_nfc.xml
