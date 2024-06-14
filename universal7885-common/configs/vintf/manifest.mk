MANIFEST_PATH := device/samsung/universal7885-common/configs/vintf

DEVICE_MANIFEST_FILE := $(MANIFEST_PATH)/manifest.xml

ifeq ($(TARGET_DEVICE),a30s)
DEVICE_MANIFEST_FILE += $(MANIFEST_PATH)/lineage_manifest_a30s.xml
else
DEVICE_MANIFEST_FILE += $(MANIFEST_PATH)/lineage_manifest.xml
endif
DEVICE_MATRIX_FILE := $(MANIFEST_PATH)/compatibility_matrix.xml
DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE := \
    $(MANIFEST_PATH)/device_framework_matrix.xml \
    vendor/lineage/config/device_framework_matrix.xml

ODM_MANIFEST_SKUS += NFC
ODM_MANIFEST_NFC_FILES := $(MANIFEST_PATH)/manifest_nfc.xml
