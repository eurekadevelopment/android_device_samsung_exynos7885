if [ ! -e .repo/local_manifests/eureka_deps.xml ]; then
	git clone https://github.com/eurekadevelopment/local_manifests .repo/local_manifests
	echo "Run repo sync again"
fi

python3 device/samsung/universal7885-common/generate_product_makefiles.py
