FM_PATH="packages/apps/FMRadio"
if [ -e ~/.git-credentials ]; then
MODE="https://"
SPERATOR="/"
else
MODE="git@"
SPERATOR=":"
fi
git clone --depth=1 "$MODE"github.com"$SPERATOR"eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S-private.git -b R9.2_rom kernel/samsung/exynos7885
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

# BT Call patch
if ! grep -q ESCO_TRANSPORT_UNIT_SIZE system/bt/device/src/esco_parameters.cc; then
echo "Applying BT call patch";
cd system/bt;
git apply ../../device/samsung/universal7885-common/.patch/BTCalls-On-Samsung.patch;
cd -
fi
# For FM Radio
if grep -q isAudioServerUid\(callingUid\) frameworks/av/services/audioflinger/AudioFlinger.cpp; then
echo "Applying FM routing patch"
sed -i 's/isAudioServerUid(callingUid)/isAudioServerOrSystemServerUid(callingUid)/g' frameworks/av/services/audioflinger/AudioFlinger.cpp
fi

if [ -d external/faceunlock ] && [ ! -d external/faceunlock/prebuilt/libs/arm ]; then
    echo "Adding 32-bit FaceUnlock libs..."
    cd external/faceunlock || exit 0
    git remote add tmp https://github.com/eurekadevelopment/android_external_faceunlock >/dev/null 2>&1
    git fetch tmp >/dev/null 2>&1
    git cherry-pick 951a19b65d2d92a3df8b6ff6c86f51163d113138 >/dev/null 2>&1
    if [ "$?" == "1" ]; then
        git cherry-pick --abort >/dev/null 2>&1
        echo "Failed to add 32-bit libs!"
    else
        echo "Done!"
    fi
    cd - >/dev/null 2>&1
fi
# Remove multiple declared FMRadio path (we have our own FMRadio and this cause build error)
if [ -d "$FM_PATH" ]; then 
echo "Remove FMRadio from ROM Source"
rm -Rf $FM_PATH; 
fi
