UNIVERSAL="device/samsung/universal7885-common"
FM_PATH="packages/apps/FMRadio"

if [ ! -e .repo/local_manifests/eureka_deps.xml ]; then
	git clone https://github.com/eurekadevelopment/local_manifests .repo/local_manifests
	echo "Run repo sync again"
fi
if test -f ${UNIVERSAL}/vendor_name; then
	rm ${UNIVERSAL}/vendor_name
fi
python3 ${UNIVERSAL}/host-tools/makefile_generator.py
for dev in a10dd a10 a20 a20e a30 a30s a40; do
	echo "Generating ${dev} Makefiles..."
	bash ${UNIVERSAL}/setup.sh "$dev"
done

# Remove multiple declared FMRadio path (we have our own FMRadio and this cause build error)
if [ -d "$FM_PATH" ]; then
	echo "Remove FMRadio from ROM Source"
	rm -Rf $FM_PATH
fi
