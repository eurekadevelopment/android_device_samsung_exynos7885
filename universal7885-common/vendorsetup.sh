UNIVERSAL="device/samsung/universal7885-common"

if [ ! -e .repo/local_manifests/eureka_deps.xml ]; then
	git clone https://github.com/eurekadevelopment/local_manifests .repo/local_manifests
	echo "Run repo sync again"
fi
python3 ${UNIVERSAL}/generate_product_makefiles.py
for dev in a10dd a10 a20 a20e a30 a30s a40; do
	echo "Generating ${dev} Makefiles..."
	bash ${UNIVERSAL}/setup.sh "$dev"
done
