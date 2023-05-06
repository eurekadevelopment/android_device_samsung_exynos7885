UNIVERSAL="device/samsung/universal7885-common"
FM_PATH="packages/apps/FMRadio"

git clone --depth=1 https://github.com/eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S -b R11.5_rom kernel/samsung/exynos7885
git clone https://github.com/eurekadevelopment/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt
git clone https://github.com/eurekadevelopment/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal.git hardware/samsung_slsi/scsc_wifibt/wifi_hal
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib
mv hardware/samsung/nfc .
git clone https://github.com/eurekadevelopment/android_hardware_samsung hardware/samsung -b AOSP-13
mv nfc hardware/samsung
git clone --depth=1 https://github.com/eurekadevelopment/android_vendor_samsung_exynos7885.git -b android-13 vendor/samsung
if test -f ${UNIVERSAL}/vendor_name; then
	rm ${UNIVERSAL}/vendor_name
fi
python3 ${UNIVERSAL}/host-tools/makefile_generator.py
for dev in a10dd a10 a20 a20e a30 a30s a40; do
	echo "Generating ${dev} Makefiles..."
	bash ${UNIVERSAL}/setup.sh "$dev"
done

# Remove multiple declared FMRadio path (we have our own FMRadio and this cause build error)
if [ -d "$FM_PATH" ]; then
	echo "Remove FMRadio from ROM Source"
	rm -Rf $FM_PATH
fi
