#!/bin/bash
#
# Copyright (C) 2017-2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

set -e

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

LINEAGE_ROOT="${MY_DIR}"/../../..

HELPER="${LINEAGE_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

function blob_fixup {
    case "$1" in
        vendor/lib/libstagefright_omx_utils.so)
            "$PATCHELF" --add-needed "libshim_stagefright_foundation.so" "$2"
            ;;
        vendor/lib*/libhifills.so)
            "$PATCHELF" --add-needed "libunwindstack.so" "$2"
            ;;
        vendor/lib*/libsec-ril.so)
            ;&
        vendor/lib*/libsec-ril-dsds.so)
            "$PATCHELF" --replace-needed "libcutils.so" "libcutils-v29.so" "$2"
            ;;
        vendor/lib*/hw/camera.exynos7904.so)
            "$PATCHELF" --replace-needed "libcamera_client.so" "libcamera_metadata_helper.so" "$2"
            "$PATCHELF" --replace-needed "libgui.so" "libgui_vendor.so" "$2"
            ;;
        vendor/lib*/libexynoscamera.so)
            ;&
        vendor/lib*/libexynoscamera3.so)
            "$PATCHELF" --remove-needed "libcamera_client.so" "$2"
            "$PATCHELF" --remove-needed "libgui.so" "$2"
            ;;
    esac
}

# Default to sanitizing the vendor folder before extraction
CLEAN_VENDOR=true

SECTION=
KANG=

while [ "${#}" -gt 0 ]; do
    case "${1}" in
        -n | --no-cleanup )
                CLEAN_VENDOR=false
                ;;
        -k | --kang )
                KANG="--kang"
                ;;
        -s | --section )
                SECTION="${2}"; shift
                CLEAN_VENDOR=false
                ;;
        * )
                SRC="${1}"
                ;;
    esac
    shift
done

if [ -z "${SRC}" ]; then
    SRC="adb"
fi

# Initialize the helper for common device
setup_vendor "${DEVICE_COMMON}" "${VENDOR}" "${LINEAGE_ROOT}" true "${CLEAN_VENDOR}"

extract "${MY_DIR}/proprietary-files.txt" "${SRC}" \
        "${KANG}" --section "${SECTION}"

if [ -s "${MY_DIR}/../${DEVICE}/proprietary-files.txt" ]; then
    # Reinitialize the helper for device
    source "${MY_DIR}/../${DEVICE}/extract-files.sh"
    setup_vendor "${DEVICE}" "${VENDOR}" "${LINEAGE_ROOT}" false "${CLEAN_VENDOR}"

    extract "${MY_DIR}/../${DEVICE}/proprietary-files.txt" "${SRC}" \
            "${KANG}" --section "${SECTION}"
fi

"${MY_DIR}/setup-makefiles.sh"
