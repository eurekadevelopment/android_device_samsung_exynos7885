package com.eurekateam.samsungextras.utils

import android.util.Log
import com.eurekateam.samsungextras.GlobalConstants
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object SystemProperties {
    /**
     * To read a prop using getprop
     * @param propName the prop name to get
     * @return the prop's value, empty string is not found
     */
    fun read(propName: String): String {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null
        val TAG = GlobalConstants.TAG
        return try {
            val GETPROP_EXECUTABLE_PATH = "/system/bin/getprop"
            process = ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName)
                .redirectErrorStream(true).start()
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            if (line == null) {
                line = "" //prop not set
            }
            Log.i(TAG, "read System Property: $propName=$line")
            line
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read System Property $propName", e)
            ""
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (ignored: IOException) {
                }
            }
            process?.destroy()
        }
    }
}