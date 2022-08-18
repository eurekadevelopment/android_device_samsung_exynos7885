#!/sbin/sh
mount /vendor
mount -o rw,remount /vendor
rm -rf /vendor/recovery-from-boot*
umount /vendor
rm -f /tmp/eureka_install.sh
exit 0
