package com.eurekateam.fmradio.utils

import android.content.Context
import java.io.*

object FileUtilities {
    const val mFMFreqFileName = "fm_freq_current"
    const val mFMVolumeFileName = "fm_volume_current"
    const val mFavouriteChannelFileName = "fm_fav_freqs"

    fun writeToFile(fileName: String, data: String, mContext: Context) {
        var os: OutputStream? = null
        try {
            os = FileOutputStream(File(mContext.filesDir.absolutePath + "/" + fileName))
            os.write(data.toByteArray(), 0, data.length)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun readFromFile(fileName: String, mContext: Context): String {
        val mFile = File(mContext.filesDir.absolutePath + "/" + fileName)
        var os: InputStream? = null
        val mByteArray = ByteArray(mFile.length().toInt())
        try {
            os = FileInputStream(mFile)
            os.read(mByteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return String(mByteArray)
    }

    fun checkIfExistFile(fileName: String, mContext: Context): Boolean {
        return File(mContext.filesDir.absolutePath + "/" + fileName).exists()
    }
}
