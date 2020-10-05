#!/sbin/sh
echo "$(grep -v libgui.so system/system/system_ext/apex/com.android.vndk.v29/etc/vndkprivate.libraries.29.txt)" > system/system/system_ext/apex/com.android.vndk.v29/etc/vndkprivate.libraries.29.txt
echo libcamera_client.so >> system/system/system_ext/apex/com.android.vndk.v29/etc/vndkcore.libraries.29.txt
