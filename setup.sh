cd ../..
git clone  git@github.com:eurekadevelopment/android_vendor_samsung_a20_r.git vendor/samsung -b master
#git clone git@github.com:eurekadevelopment/android_device_samsung_a20_r.git device/samsung
git clone git@github.com:eurekadevelopment/android_hardware_samsung.git hardware/samsung -b AOSP-11
rm -rf hardware/custom
git clone git@github.com:eurekadevelopment/Eureka-Kernel-Exynos7885-Q-R.git kernel/samsung/exynos7885 --depth=1 -b R7.5_rom
git clone https://github.com/lineageos/android_hardware_samsung_nfc hardware/samsung/nfc
git clone https://github.com/lineageos/android_hardware_samsung_slsi_libbt hardware/samsung_slsi/libbt
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wifi_hal hardware/samsung_slsi/scsc_wifibt/wifi_hal
git clone https://github.com/lineageos/android_hardware_samsung_slsi_scsc_wifibt_wpa_supplicant_lib hardware/samsung_slsi/scsc_wifibt/wpa_supplicant_lib
git clone https://github.com/lineageos/android_device_samsung_slsi_sepolicy device/samsung_slsi/sepolicy
git clone https://github.com/LineageOS/android_hardware_lineage_interfaces.git hardware/lineage/interfaces
