package com.eurekateam.fmradio

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class FMRadioService : Service() {
    private lateinit var mContext: Context
    private lateinit var mAudioManager: AudioManager
    private var mIsPlaying = true
    private lateinit var mTracks : LongArray
    private var mTitle : String = ""
    private var fd : Int = -1
    private var mIndex = -1
    private lateinit var mNativeFMInterface: NativeFMInterface
    private lateinit var pendingIntent: PendingIntent
    private lateinit var contentView : RemoteViews
    private lateinit var mFilePath : File
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            var mShutDown = false
            Log.i(TAG, "onStartCommand: Received ${intent.action}")
            when (intent.action) {
                BEGIN_BG_SERVICE -> {
                    mContext = this
                    mFilePath = mContext.filesDir
                    contentView = RemoteViews(mContext.packageName, R.layout.notification)
                    mNativeFMInterface = NativeFMInterface()
                    fd = intent.getIntExtra("fd", -1)
                    mTracks = intent.getLongArrayExtra("tracks")!!
                    mIndex = intent.getIntExtra("CurrentIndex", -1)
                    mTitle = "FM ${mTracks[mIndex].toFloat() / 1000} Mhz"
                    contentView.setImageViewResource(
                        R.id.btn_play_pause,
                        R.drawable.stop
                    )
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    setupRemoteView()
                    // Create an explicit intent for an Activity in your app
                    val onClickIntent = Intent(this, FMRadioService::class.java).apply {
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        onClickIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    mIsPlaying = true
                    startForeground(51, pushNotification())
                }
                ACTION_PLAY -> {
                    mIsPlaying = !mIsPlaying
                    mTitle =
                        if (mIsPlaying) {
                            "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                        } else {
                            "FM ${mTracks[mIndex].toFloat() / 1000}Mhz - Stopped"
                        }
                    if (mIsPlaying) {
                        contentView.setImageViewResource(
                            R.id.btn_play_pause,
                            R.drawable.stop
                        )
                        writeToFile("fm_radio_state", "1")
                        mAudioManager.setParameters(FM_RADIO_ON)

                    } else {
                        mAudioManager.setParameters(FM_RADIO_OFF)
                        contentView.setImageViewResource(
                            R.id.btn_play_pause,
                            R.drawable.start
                        )
                        writeToFile("fm_radio_state", "0")
                    }
                }
                ACTION_BEFORE -> {
                    mIsPlaying = true
                    contentView.setImageViewResource(
                        R.id.btn_play_pause,
                        R.drawable.stop
                    )
                    Log.i(TAG, "onStartCommand: mCurrentIndex $mIndex")
                    if (mIndex > 0)
                        mIndex -= 1
                    if (mIndex >= 1)
                        mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    mTitle = if (mIsPlaying) {
                        "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                    } else {
                        "FM ${mTracks[mIndex].toFloat() / 1000}Mhz - Stopped"
                    }
                }
                ACTION_NEXT -> {
                    mIsPlaying = true
                    contentView.setImageViewResource(
                        R.id.btn_play_pause,
                        R.drawable.stop
                    )
                    Log.i(TAG, "onStartCommand: mCurrentIndex $mIndex")
                    if (mIndex < mTracks.size - 1)
                        mIndex += 1
                    if (mIndex < mTracks.size - 1)
                        mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    mTitle = if (mIsPlaying) {
                        "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                    } else {
                        "FM ${mTracks[mIndex].toFloat() / 1000}Mhz - Stopped"
                    }
                }
                ACTION_QUIT -> {
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    mAudioManager.setParameters(FM_RADIO_OFF)
                    stopForeground(true)
                    mShutDown = true
                }
                STOP_BG_SERVICE -> {
                    val mFreqToWrite = mTracks[mIndex].toString()
                    writeToFile(
                        "fm_radio_freq_current",
                        mFreqToWrite
                    )
                }
                else -> {
                    throw IllegalAccessException("Unexpected Intent!")
                }
            }
            Log.i(TAG, "onStartCommand: mTitle=$mTitle")
            if (!mShutDown) {
                contentView.setTextViewText(R.id.txt_title, mTitle)
                startForeground(51,pushNotification())
            }
        }
        return START_STICKY
    }
    private fun writeToFile(fileName : String, data: String){
        var os: OutputStream? = null
        val mData = data.padStart(6,'0')
        try {
            os = FileOutputStream(File(mFilePath.absolutePath + "/" + fileName))
            os.write(mData.toByteArray(), 0, mData.length)
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
    private fun setupRemoteView() {
        val actionTogglePlay = Intent(ACTION_PLAY)
        val actionForward = Intent(ACTION_NEXT)
        val actionRewind = Intent(ACTION_BEFORE)
        val actionClose = Intent(ACTION_QUIT)
        val togglePlay = PendingIntent.getService(mContext, 0, actionTogglePlay, PendingIntent.FLAG_IMMUTABLE)
        val forward = PendingIntent.getService(mContext, 0, actionForward, PendingIntent.FLAG_IMMUTABLE)
        val rewind = PendingIntent.getService(mContext, 0, actionRewind, PendingIntent.FLAG_IMMUTABLE)
        val close = PendingIntent.getService(mContext, 0, actionClose, PendingIntent.FLAG_IMMUTABLE)
        contentView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay)
        contentView.setOnClickPendingIntent(R.id.btn_forward, forward)
        contentView.setOnClickPendingIntent(R.id.btn_rewind, rewind)
        contentView.setOnClickPendingIntent(R.id.btn_close, close)
    }

    private fun pushNotification(): Notification {
        val nm = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            mContext.packageName, "FM Radio Playing",
            NotificationManager.IMPORTANCE_NONE
        )
        nm.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(mContext, mContext.packageName)
        builder.setSmallIcon(R.drawable.radio_text)
        builder.setOngoing(true)
        builder.setCustomBigContentView(contentView)
        builder.setContentIntent(pendingIntent)
        builder.setChannelId(mContext.packageName)
        return builder.build()
    }
    companion object {
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        private const val BEGIN_BG_SERVICE = "$PACKAGENAME.STARTBG"
        private const val ACTION_PLAY = "$PACKAGENAME.PLAY_FM"
        private const val ACTION_NEXT = "$PACKAGENAME.NEXT"
        private const val ACTION_BEFORE = "$PACKAGENAME.BEFORE"
        private const val ACTION_QUIT = "$PACKAGENAME.QUIT"
        private const val FM_RADIO_ON = "l_fmradio_mode=on"
        private const val FM_RADIO_OFF = "l_fmradio_mode=off"
        private const val TAG = "FMRadioService"
        private const val STOP_BG_SERVICE = "${PACKAGENAME}.STOPBG"
    }
}