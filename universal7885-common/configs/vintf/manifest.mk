MANIFEST_PATH := device/samsung/universal7885-common/configs/vintf

DEVICE_MANIFEST_FILE := $(MANIFEST_PATH)/manifest.xml
DEVICE_MATRIX_FILE := $(MANIFEST_PATH)/compatibility_matrix.xml
ODM_MANIFEST_SKUS += NFC
ODM_MANIFEST_NFC_FILES := $(MANIFEST_PATH)/manifest_nfc.xml

ifneq ($(filter $(TARGET_DEVICE), a40),)
DEVICE_MANIFEST_FILE += $(MANIFEST_PATH)/manifest_gnss.xml
endif
