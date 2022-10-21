package com.eurekateam.fmradio.utils

import android.app.Activity

import java.util.Timer
import java.util.TimerTask

object WaitUntil {
    fun setTimer(act: Activity, todo: IWaitUntil, timeout: Long = 15000) {
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    act.runOnUiThread({
                        if (todo.cond()) {
                            todo.todo()
                            cancel()
                        }
                    })
                }
            },
            timeout
        )
    }
}
