package com.eurekateam.fmradio

import vendor.eureka.hardware.fmradio.IFMDevControl
import vendor.eureka.hardware.fmradio.IFMSysfsSupport
import vendor.eureka.hardware.fmradio.GetType
import vendor.eureka.hardware.fmradio.SetType
import vendor.eureka.hardware.fmradio.Direction

import android.os.ServiceManager

class NativeFMInterface {
    private val mDevCtl : IFMDevControl
    private val mSysfsCtl : IFMSysfsSupport
    private var mSysfs = false

    init {
        mDevCtl = IFMDevControl.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMDevControl/default"))
        mSysfsCtl = IFMSysfsSupport.Stub.asInterface(ServiceManager.waitForDeclaredService("vendor.eureka.hardware.fmradio.IFMSysfsSupport/default"))
        mSysfs = mSysfsCtl.isAvailable() 
    }

    fun openFMDevice(): Int {
        mDevCtl.open()
        return 1
    }

    fun getFMFreq(a: Int): Long = mDevCtl.getValue(GetType.GET_TYPE_FM_FREQ).toLong()
    fun setFMFreq(a: Int, freq: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_FREQ, freq)
    fun setFMVolume(a: Int, volume: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, volume)
    fun setFMMute(a: Int, mute: Boolean) = mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, if (mute) 1 else 0)
    fun getFmUpper(a: Int) : Int = mDevCtl.getValue(GetType.GET_TYPE_FM_UPPER_LIMIT)
    fun getFMLower(a: Int): Int = mDevCtl.getValue(GetType.GET_TYPE_FM_LOWER_LIMIT)
    fun getRMSSI(a: Int): Int = mDevCtl.getValue(GetType.GET_TYPE_FM_RMSSI)
    fun getFMTracks(fd: Int): IntArray = if (mSysfs) {
         var mRes = intArrayOf()
         for (a in 1..30) {
            mSysfsCtl.adjustFreqByStep(Direction.UP)
            var freq = mSysfsCtl.getFreqFromSysfs()
            if (mRes.contains(freq)) continue else mRes += freq
         }
         mRes 
    } else mDevCtl.getFreqsList()
    fun setFMThread(a: Int, run: Boolean) = mDevCtl.setValue(SetType.SET_TYPE_FM_THREAD, if (run) 1 else 0)
    fun getNextChannel(a: Int): Int = if (mSysfs) {
        mSysfsCtl.adjustFreqByStep(Direction.UP)
        mSysfsCtl.getFreqFromSysfs()
    } else mDevCtl.getValue(GetType.GET_TYPE_FM_NEXT_CHANNEL)
    fun getBeforeChannel(a: Int): Int = if (mSysfs) {
        mSysfsCtl.adjustFreqByStep(Direction.DOWN)
        mSysfsCtl.getFreqFromSysfs()
    } else mDevCtl.getValue(GetType.GET_TYPE_FM_BEFORE_CHANNEL)
    fun stopSearching(a: Int) = mDevCtl.setValue(SetType.SET_TYPE_FM_SEARCH_CANCEL, 0)
    fun setFMRSSI(a: Int, rssi: Long) = mDevCtl.setValue(SetType.SET_TYPE_FM_RMSSI, rssi.toInt())
    fun closeFMDevice(fd: Int) = mDevCtl.close()
    fun getSysfsSupport(): Boolean = mSysfs
    external fun setAudioRoute(speaker: Boolean): Int 
}
