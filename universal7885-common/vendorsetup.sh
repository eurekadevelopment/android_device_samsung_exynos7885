git clone  git@github.com:eurekadevelopment/android_vendor_samsung_a20_r.git vendor/samsung -b master
#git clone git@github.com:eurekadevelopment/android_device_samsung_a20_r.git device/samsung
rm -rf hardware/samsung
git clone git@github.com:eurekadevelopment/android_hardware_samsung.git hardware/samsung -b AOSP-11
git clone git@github.com:eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S-private.git kernel/samsung/exynos7885 --depth=1 -b R8.0_rom
git clone https://github.com/lineageos/android_hardware_samsung_nfc hardware/samsung/nfc
git clone https://github.com/lineageos/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal hardware/samsung_slsi/scsc_wifibt/wifi_hal
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib
if ! test -f vendor_detect; then
#clang++ device/samsung/universal7885-common/vendor_detect/main.cpp -o vendor_detect -std=c++17
cp device/samsung/universal7885-common/vendor_detect/prebuilt vendor_detect
fi
if ! test -f device/samsung/universal7885-common/vendor_name; then
touch device/samsung/universal7885-common/vendor_name
fi
./vendor_detect
echo "Generating A20 Makefiles"
./device/samsung/a20/setup.sh
echo "Generating A20e Makefiles"
./device/samsung/a20e/setup.sh

