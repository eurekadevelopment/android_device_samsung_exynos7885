UNIVERSAL="device/samsung/universal7885-common"
FM_PATH="packages/apps/FMRadio"
if [ -e ~/.git-credentials ]; then
	MODE="https://"
	SPERATOR="/"
else
	MODE="git@"
	SPERATOR=":"
fi
git clone --depth=1 "$MODE"github.com"$SPERATOR"eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S-private.git -b R10.1-T_rom kernel/samsung/exynos7885
git clone https://github.com/lineageos/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt -b lineage-18.1
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal.git hardware/samsung_slsi/scsc_wifibt/wifi_hal -b lineage-18.1
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib -b lineage-18.1
git clone https://github.com/lineageos/android_hardware_samsung hardware/samsung -b lineage-18.1
git clone --depth=1 https://github.com/eurekadevelopment/android_vendor_samsung_exynos7885.git -b master vendor/samsung
git clone https://github.com/LineageOS/android_hardware_samsung_nfc hardware/samsung/nfc -b lineage-18.1
if test -f ${UNIVERSAL}/vendor_name; then
	rm ${UNIVERSAL}/vendor_name
fi
python3 ${UNIVERSAL}/vendor_detect/main.py
for dev in a10dd a10 a20 a20e a30 a30s a40; do
	echo "Generating ${dev} Makefiles..."
	bash ${UNIVERSAL}/setup.sh "$dev"
done

# For FM Radio
if grep -q isAudioServerUid\(callingUid\) frameworks/av/services/audioflinger/AudioFlinger.cpp; then
	echo "Applying FM routing patch"
	sed -i 's/isAudioServerUid(callingUid)/isAudioServerOrSystemServerUid(callingUid)/g' frameworks/av/services/audioflinger/AudioFlinger.cpp
fi
# Remove multiple declared FMRadio path (we have our own FMRadio and this cause build error)
if [ -d "$FM_PATH" ]; then
	echo "Remove FMRadio from ROM Source"
	rm -Rf $FM_PATH
fi

