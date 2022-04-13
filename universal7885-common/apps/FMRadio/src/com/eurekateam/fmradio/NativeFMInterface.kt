package com.eurekateam.fmradio

class NativeFMInterface {
    external fun openFMDevice(): Int
    external fun getFMFreq(fd: Int): Long
    external fun setFMFreq(fd: Int, freq: Int): Int
    external fun setFMVolume(fd: Int, volume: Int /* 1 - 15 */): Int
    external fun setFMMute(fd: Int, mute: Boolean): Int
    external fun getFmUpper(fd: Int): Int
    external fun getFMLower(fd: Int): Int
    external fun getRMSSI(fd: Int): Int
    external fun getFMTracks(fd: Int): LongArray
    external fun setFMStereo(fd: Int): Int
    external fun setFMMono(fd: Int): Int
    external fun setFMThread(fd: Int, run: Boolean): Int
    external fun setFMBoot(fd: Int)
    external fun getNextChannel(fd: Int): Int
    external fun getBeforeChannel(fd: Int): Int
    external fun getAudioChannel(fd: Int): Boolean // Stereo == true, Mono == false
    external fun stopSearching(fd: Int)
    external fun setFMRSSI(fd: Int, rssi: Long): Int
    external fun closeFMDevice(fd: Int)
    external fun getSysfsSupport(): Boolean
    external fun setAudioRoute(speaker: Boolean): Int
}
