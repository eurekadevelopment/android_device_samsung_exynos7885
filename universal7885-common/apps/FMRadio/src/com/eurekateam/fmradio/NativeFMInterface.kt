package com.eurekateam.fmradio

import android.os.ServiceManager
import vendor.eureka.hardware.fmradio.GetType
import vendor.eureka.hardware.fmradio.IFMDevControl
import vendor.eureka.hardware.fmradio.SetType

class NativeFMInterface {
    private val mDevCtl: IFMDevControl
    private val mSysfsCtl: IFMDevControl
    private val mDefaultCtl: IFMDevControl

    init {
        mDevCtl = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMDevControl/default"))
        mSysfsCtl = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMDevControl/support"))
        mDefaultCtl = if (mSysfsCtl.getValue(GetType.GET_TYPE_FM_SYSFS_IF) == 0) mSysfsCtl else mDevCtl
    }

    fun openFMDevice(): Int {
        mDevCtl.open()
        return 1
    }

    fun getFMFreq(a: Int): Long = mDevCtl.getValue(GetType.GET_TYPE_FM_FREQ).toLong()
    fun setFMFreq(a: Int, freq: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_FREQ, freq)
    fun setFMVolume(a: Int, volume: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, volume)
    fun setFMMute(a: Int, mute: Boolean) = mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, if (mute) 1 else 0)
    fun getFmUpper(a: Int): Int = mDevCtl.getValue(GetType.GET_TYPE_FM_UPPER_LIMIT)
    fun getFMLower(a: Int): Int = mDevCtl.getValue(GetType.GET_TYPE_FM_LOWER_LIMIT)
    fun getRMSSI(a: Int): Int = mDevCtl.getValue(GetType.GET_TYPE_FM_RMSSI)
    fun getFMTracks(fd: Int): IntArray = mDefaultCtl.getFreqsList()
    fun setFMThread(a: Int, run: Boolean) = mDevCtl.setValue(SetType.SET_TYPE_FM_THREAD, if (run) 1 else 0)
    fun getNextChannel(a: Int): Int = mDefaultCtl.getValue(GetType.GET_TYPE_FM_NEXT_CHANNEL)
    fun getBeforeChannel(a: Int): Int = mDefaultCtl.getValue(GetType.GET_TYPE_FM_BEFORE_CHANNEL)
    fun stopSearching(a: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_SEARCH_CANCEL, 0)
    fun setFMRSSI(a: Int, rssi: Long) = mDevCtl.setValue(SetType.SET_TYPE_FM_RMSSI, rssi.toInt())
    fun closeFMDevice(fd: Int) = mDevCtl.close()
    fun getSysfsSupport(): Boolean = mSysfsCtl.getValue(GetType.GET_TYPE_FM_SYSFS_IF) == 0
    fun setAudioRoute(speaker: Boolean) = mDevCtl.setValue(SetType.SET_TYPE_FM_SPEAKER_ROUTE, if (speaker) 1 else 0)
}
