package com.eurekateam.fmradio.utils

import android.app.Activity

import java.util.Timer
import java.util.TimerTask

object WaitUntil {
    fun setTimer(act: Activity, todo: IWaitUntil, initdelay: Long = 5000, delay: Long = 500) {
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
            initdelay, delay
        )
    }
}
