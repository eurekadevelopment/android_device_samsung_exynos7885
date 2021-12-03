rm -rf hardware/samsung
git clone git@github.com:eurekadevelopment/android_hardware_samsung.git hardware/samsung -b NUKE_ALL
git clone git@github.com:eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R-S-private.git kernel/samsung/exynos7885 --depth=1 -b R8.1_rom
git clone https://github.com/lineageos/android_hardware_samsung_nfc hardware/samsung/nfc
git clone https://github.com/lineageos/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal hardware/samsung_slsi/scsc_wifibt/wifi_hal
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib
# Vendors
mkdir -p tmp
git clone git@github.com:eurekadevelopment/android_vendor_samsung_a20_r.git --depth=1 tmp/
git clone git@github.com:eurekadevelopment/proprietary_vendor_samsung_a40_r.git tmp/a40 --depth=1
git clone git@github.com:eurekadevelopment/proprietary_vendor_samsung_a30_r.git tmp/a30 --depth=1
git clone git@github.com:eurekadevelopment/android_vendor_samsung_a10_arm64_R.git tmp/a10 --depth=1
git clone git@github.com:eurekadevelopment/proprietary_vendor_samsung_universal7904-common_r.git tmp/universal7904-common --depth=1
rm -rf vendor/samsung
mv tmp vendor/samsung
if test -f device/samsung/universal7885-common/vendor_name; then
rm device/samsung/universal7885-common/vendor_name
fi
python3 device/samsung/universal7885-common/vendor_detect/main.py -d 0
echo "Generating A10 Makefiles"
./device/samsung/a10/setup.sh
echo "Generating A20 Makefiles"
./device/samsung/a20/setup.sh
echo "Generating A20e Makefiles"
./device/samsung/a20e/setup.sh
echo "Generating A30 Makefiles"
./device/samsung/a30/setup.sh
echo "Generating A40 Makefiles"
./device/samsung/a40/setup.sh

