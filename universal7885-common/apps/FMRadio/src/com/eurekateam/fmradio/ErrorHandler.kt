package com.eurekateam.fmradio

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class ErrorHandler: Activity() {
    private lateinit var mErrorSimple: TextView
    private lateinit var mErrorDetail: TextView
    private var mHeadPhonesPresent = false
    private var mRegistered  = false
    private var fd = 0
    private lateinit var mContext: Context
    private lateinit var mAudioManager : AudioManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_screen)
        mContext = this
        fd = intent.getIntExtra("fd", -1)
        mAudioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
        mErrorDetail = findViewById(R.id.error_detailed)
        mErrorSimple = findViewById(R.id.error_simple)
        val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (i in mAudioDeviceInfo.indices){
            if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES){
                mHeadPhonesPresent = true
            }
        }
        when (intent?.action){

            RADIO_DEVICE_IO_FAIL -> {
                mErrorSimple.text = getString(R.string.radio_io_error)
                mErrorDetail.text = getString(R.string.radio_io_error_desc)
            }
            TRACKS_EMPTY -> {
                mErrorSimple.text = getString(R.string.tracks_empty_error)
                mErrorDetail.text = getString(R.string.tracks_empty_error_desc)
            }
            NO_WIRED_HEADPHONES -> {
                mErrorSimple.text = getString(R.string.no_headphones_error)
                mErrorDetail.text = getString(R.string.no_headphones_error_desc)
                mHeadPhonesPresent = false
                val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
                val receiver = mWiredHeadsetReceiver
                mHeadPhonesPresent = false
                registerReceiver(receiver, receiverFilter)
                mRegistered = true
            }
        }
    }
    private val mWiredHeadsetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                mHeadPhonesPresent = false
                val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (i in mAudioDeviceInfo.indices){
                    if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES){
                        mHeadPhonesPresent = true
                    }
                }
                if (mHeadPhonesPresent){
                    Toast.makeText(mContext, "Wired headphones inserted", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(mContext, MainActivity::class.java))
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mRegistered)
            unregisterReceiver(mWiredHeadsetReceiver)
    }

    companion object {
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        // Failures
        private const val RADIO_DEVICE_IO_FAIL = "${PACKAGENAME}.RADIO.IO"
        private const val TRACKS_EMPTY = "${PACKAGENAME}.TRACKS"
        private const val NO_WIRED_HEADPHONES = "${PACKAGENAME}.NO_HEADPHONE"

    }
}