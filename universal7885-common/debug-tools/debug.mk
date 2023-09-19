## logger: Dump tool that automatically dumps logcat/kmsg while booting.
# It is common to miss boot time logs, since there is a lot of logs there on boot.
# So this dumps logs while boot, starts at post-fs-data, stops at sys.boot_completed=1
#
# This is selinux-safe (Enforcing OK) neverallow-safe (Passes neverallow)
# And user-build safe (Works)
#

PRODUCT_PACKAGES += logger

## dlopener: Tool for command-line to test dlopen, aka opening shared object files (.so)
# Provides dlerror string if failed to open (resolve dependencies, symbols etc)
# Else returns success.
#
# Has vendor varient and system varient as from Android R, namespace is sperated with
# /vendor and /system so /system/bin/dlopener cant open vendor libs and vice versa

PRODUCT_PACKAGES += dlopener dlopener.vendor

# System calls - ptrace

PRODUCT_PACKAGES += strace
