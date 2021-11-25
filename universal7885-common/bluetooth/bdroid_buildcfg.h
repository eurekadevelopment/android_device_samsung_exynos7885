#ifndef _BDROID_BUILDCFG_H
#define _BDROID_BUILDCFG_H

#pragma push_macro("PROPERTY_VALUE_MAX")

#include <cutils/properties.h>
#include <string.h>

static inline const char* BtmGetDefaultName()
{
    char product_device[PROPERTY_VALUE_MAX];
    property_get("ro.product.device", product_device, "");


    if (strstr(product_device, "a20e"))
        return "Galaxy A20e";
    if (strstr(product_device, "a20"))
        return "Galaxy A20";

    // Fallback to Moto SDM Generic
    return "Samsung Galaxy";
}

#define BTM_DEF_LOCAL_NAME BtmGetDefaultName()
#define BTM_ESCO_TRANSPORT_UNIT_SIZE_PCM16
#pragma pop_macro("PROPERTY_VALUE_MAX")
/*A2DP SINK ENABLE*/
#define BTA_AV_SINK_INCLUDED TRUE
#endif
