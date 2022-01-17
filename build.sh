#!/bin/bash
# Logic here:
# Target devices: a10, a20, a20e, a30, a40, or all (LOWERCASE)
# Target : e.g. bacon, otapackage
# Usage ./build.sh a10 a20e bacon

# Export Telegram variables
CHAT_ID=-1001207057155
BOT_TOKEN=5068662666:AAH4nY3MIcx_kBCZKnAVDsLstylvt50nlHA

VENDOR=$(cat device/samsung/universal7885-common/vendor_name)
if [ ! $VENDOR ]; then
VENDOR=aosp;
fi
# Telegram functions.
function tg_sendText() {
	curl -s "https://api.telegram.org/bot$BOT_TOKEN/sendMessage" \
	-d "parse_mode=html" \
	-d text="${@}" \
	-d chat_id=$CHAT_ID \
	-d "disable_web_page_preview=true"
}
function tg_sendFile() {
	curl -s "https://api.telegram.org/bot$BOT_TOKEN/sendDocument" \
	-F parse_mode=markdown \
	-F chat_id=$CHAT_ID \
	-F document=@${1} \
	-F "caption=$POST_CAPTION"
}

if [ "$#" -lt 2 ]; then
    tg_sendText "Illegal number of parameters ($#)"
    exit 1
fi
. build/envsetup.sh
TARGET=${*%${!#}}
if [ "$1" == "all" ]; then
TARGET="a10 a20 a20e a30 a40"
fi
tg_sendText "[Vendor = $VENDOR], [Devices = $TARGET], [Build Target=${@:$#}]"
for i in $TARGET; do 
start=`date +%s`
tg_sendText "[$VENDOR][$i] lunch...";
if lunch ${VENDOR}_${i}-user; then
tg_sendText "[$VENDOR][$i] lunch success";
else
tg_sendText "[$VENDOR][$i] lunch failed";
exit 1;
fi
tg_sendText "[$VENDOR][$i] start build...";
if ! m ${@:$#}; then
end=`date +%s`
secs=$((end-start)) 
tg_sendText "[$VENDOR][$i] fail in $((secs/3600)) Hours $((secs%3600/60)) Minutes $((secs%60)) Seconds";
if [ -e out/error.log ]; then
tg_sendFile "out/error.log"
fi
exit 1;
fi
end=`date +%s`
secs=$((end-start))
tg_sendText "[$VENDOR][$i] success in $((secs/3600)) Hours $((secs%3600/60)) Minutes $((secs%60)) Seconds"
done
tg_sendText "All done"
