VENDOR=$(cat device/samsung/universal7885-common/vendor_name)
if [ ! $VENDOR ]; then
VENDOR=aosp;
fi
# Generate AndroidProducts.mk
echo "# Auto-Generated by device/samsung/a30s/setup.sh" > device/samsung/a30s/AndroidProducts.mk
echo "PRODUCT_MAKEFILES += \$(LOCAL_DIR)/"$VENDOR"_a30s.mk" >>  device/samsung/a30s/AndroidProducts.mk

# Generate <vendor>_a30s.mk
echo "# Auto-Generated by device/samsung/a30s/setup.sh" > device/samsung/a30s/"$VENDOR"_a30s.mk
echo "\$(call inherit-product, device/samsung/a30s/full_a30s.mk)" >> device/samsung/a30s/"$VENDOR"_a30s.mk
if test -f vendor/"$VENDOR"/config/common_full_phone.mk && echo "common_full_phone"; then
	echo "\$(call inherit-product, vendor/"$VENDOR"/config/common_full_phone.mk)" >> device/samsung/a30s/"$VENDOR"_a30s.mk
elif test -f vendor/"$VENDOR"/config/common.mk && echo "common"; then
	echo "\$(call inherit-product, vendor/"$VENDOR"/config/common.mk)" >> device/samsung/a30s/"$VENDOR"_a30s.mk
fi
echo "PRODUCT_NAME := "$VENDOR"_a30s" >> device/samsung/a30s/"$VENDOR"_a30s.mk
echo "" >>  device/samsung/a30s/"$VENDOR"_a30s.mk
echo "# Additional Props" >>  device/samsung/a30s/"$VENDOR"_a30s.mk
echo "TARGET_FACE_UNLOCK_SUPPORTED := true" >>  device/samsung/a30s/"$VENDOR"_a30s.mk
echo "TARGET_BOOT_ANIMATION_RES := 1080" >> device/samsung/a30s/"$VENDOR"_a30s.mk

