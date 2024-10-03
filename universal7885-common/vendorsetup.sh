if [ ! -e .repo/local_manifests/eureka_deps.xml ]; then
	git clone https://github.com/eurekadevelopment/local_manifests .repo/local_manifests
	echo "Run repo sync again"
fi

python3 device/samsung/universal7885-common/generate_product_makefiles.py

if [ -e vendor/*/vars/aosp_target_release ]; then
    # Remove 'local' from outside function
    vendor=$(basename $(dirname $(dirname vendor/*/vars/aosp_target_release)))
    source vendor/$vendor/vars/aosp_target_release

    set +x
    echo "klunch() is available"

    function klunch() {
        # Define 'local' within the function
        local variantStr

        if [ -z "$1" ]; then
            echo "Usage: klunch <device-codename>"
            return
        fi
        read -p "Select build variant: (1. user, 2. userdebug, 3. eng) " variant
        case $variant in
            1)
                variantStr=user;;
            2)
                variantStr=userdebug;;
            3)
                variantStr=eng;;
            *)
                echo "Invalid choice, aborting..."
                return
                ;;
        esac
        lunch "${vendor}_$1_${aosp_target_release}-${variantStr}"
    }
fi

