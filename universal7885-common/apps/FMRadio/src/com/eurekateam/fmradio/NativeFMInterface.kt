package com.eurekateam.fmradio

import android.os.ServiceManager
import vendor.eureka.hardware.fmradio.GetType
import vendor.eureka.hardware.fmradio.IFMDevControl

class NativeFMInterface {
    val mDevCtl: IFMDevControl
    val mSysfsCtl: IFMDevControl
    val mDefaultCtl: IFMDevControl

    init {
        mDevCtl = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMDevControl/default"))
        mSysfsCtl = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMDevControl/support"))
        mDefaultCtl = if (mSysfsCtl.getValue(GetType.GET_TYPE_FM_SYSFS_IF) == 0) mSysfsCtl else mDevCtl
    }
}
