FM_PATH="packages/apps/FMRadio"
if [ -e ~/.git-credentials ]; then
MODE="https://"
SPERATOR="/"
else
MODE="git@"
SPERATOR=":"
fi
git clone --depth=1 "$MODE"github.com"$SPERATOR"eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S-private.git -b R10.1_rom kernel/samsung/exynos7885
git clone https://github.com/lineageos/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt
git clone https://github.com/eurekadevelopment/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal.git hardware/samsung_slsi/scsc_wifibt/wifi_hal
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib
mv hardware/samsung/nfc .
git clone https://github.com/eurekadevelopment/android_hardware_samsung hardware/samsung
mv nfc hardware/samsung
git clone --depth=1 https://github.com/eurekadevelopment/android_vendor_samsung_exynos7885.git -b master vendor/samsung
if test -f device/samsung/universal7885-common/vendor_name; then
rm device/samsung/universal7885-common/vendor_name
fi
python3 device/samsung/universal7885-common/vendor_detect/main.py
echo "Generating A10 Makefiles"
./device/samsung/a10/setup.sh
echo "Generating A20 Makefiles"
./device/samsung/a20/setup.sh
echo "Generating A20e Makefiles"
./device/samsung/a20e/setup.sh
echo "Generating A30 Makefiles"
./device/samsung/a30/setup.sh
echo "Generating A30s Makefiles"
./device/samsung/a30s/setup.sh
echo "Generating A40 Makefiles"
./device/samsung/a40/setup.sh

# For FM Radio
if grep -q isAudioServerUid\(callingUid\) frameworks/av/services/audioflinger/AudioFlinger.cpp; then
echo "Applying FM routing patch"
sed -i 's/isAudioServerUid(callingUid)/isAudioServerOrSystemServerUid(callingUid)/g' frameworks/av/services/audioflinger/AudioFlinger.cpp
fi
# Remove multiple declared FMRadio path (we have our own FMRadio and this cause build error)
if [ -d "$FM_PATH" ]; then 
echo "Remove FMRadio from ROM Source"
rm -Rf $FM_PATH; 
fi
