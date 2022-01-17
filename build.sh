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
TARGET=${*%${!#}}
tg_sendText "VENDOR = $VENDOR, Devices = ${@:$#}, Build Target=$TARGET"
for i in TARGET; do 
tg_sendText "$VENDOR $i lunch...";
if lunch $VENDOR_$i-user; then
tg_sendText "$VENDOR $i lunch success";
else
tg_sendText "$VENDOR $i lunch fail";
exit 1;
fi
tg_sendText "$VENDOR $i build...";
if m $TARGET; then
tg_sendText "$VENDOR $i finish";
else
tg_sendText "$VENDOR $i failed";
exit 1;
fi
done

