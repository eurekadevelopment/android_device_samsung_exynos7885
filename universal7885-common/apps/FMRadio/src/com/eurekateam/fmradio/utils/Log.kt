package com.eurekateam.fmradio.utils

import android.util.Log

/**
 * Created by willi on 22.03.18.
 */
object Log {
    private const val TAG = "FMRadio_slsi"
    fun i(message: String) {
        Log.i(TAG, getMessage(message))
    }

    fun e(message: String) {
        Log.e(TAG, getMessage(message))
    }

    fun d(message: String) {
        Log.d(TAG, getMessage(message))
    }
    private fun getMessage(message: String): String {
        val element = Thread.currentThread().stackTrace[4]
        val className = element.className
        return String.format(
            "[%s][%s] %s",
            className.substring(className.lastIndexOf(".") + 1),
            element.methodName,
            message
        )
    }

    fun w(message: String) {
        Log.w(TAG, getMessage(message))
    }
}
