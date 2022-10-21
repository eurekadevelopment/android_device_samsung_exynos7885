package com.eurekateam.fmradio.utils

interface IWaitUntil {
    fun cond(): Boolean
    fun todo()
}
