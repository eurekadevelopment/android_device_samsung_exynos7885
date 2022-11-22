package com.eurekateam.fmradio

import android.os.ServiceManager
import com.royna.aidlvintf.AIDLInterface.makeAIDLStr
import vendor.eureka.hardware.fmradio.GetType
import vendor.eureka.hardware.fmradio.IFMDevControl

class NativeFMInterface {
    val mDevCtl: IFMDevControl
    val mSysfsCtl: IFMDevControl
    val mDefaultCtl: IFMDevControl

    fun getInstance(instance: String) = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService(makeAIDLStr("vendor.eureka.hardware.fmradio", "IFMDevControl", instance)))

    init {
        mDevCtl = getInstance("default")
        mSysfsCtl = getInstance("support")
        mDefaultCtl = if (mSysfsCtl.getValue(GetType.GET_TYPE_FM_SYSFS_IF) == 0) mSysfsCtl else mDevCtl
    }
}
