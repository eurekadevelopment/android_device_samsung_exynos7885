package com.eurekateam.fmradio

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import androidx.core.content.res.ResourcesCompat
import android.graphics.BitmapFactory
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.Log
import java.io.File


class FMRadioService : Service() {
    private lateinit var mContext: Context
    private lateinit var mAudioManager: AudioManager
    private lateinit var mTracks : LongArray
    private var mTitle : String = ""
    private var fd : Int = -1
    private var mIndex = -1
    private lateinit var mNativeFMInterface: NativeFMInterface
    private lateinit var mFilePath : File
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fd = MainFragment.fd
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mTracks = MainFragment.mTracks
        mIndex = MainFragment.getIndex()
        if (mTracks.isEmpty() || mIndex == -1)
        {
            return super.onStartCommand(intent, flags, startId)
        }
        if (intent != null) {
            intent.action?.let { Log.i(it) }
            when (intent.action) {
                BEGIN_BG_SERVICE -> {
                    mContext = this
                    mFilePath = mContext.filesDir
                    mNativeFMInterface = NativeFMInterface()
                    mTitle = "FM ${mTracks[mIndex].toFloat() / 1000} Mhz"
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    mIsPlaying = true
                }
                ACTION_PLAY -> {
                    mIsPlaying = !mIsPlaying
                    mTitle = "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                    if (mIsPlaying) {
                        mAudioManager.setParameters(FM_RADIO_ON)
                    } else {
                        mAudioManager.setParameters(FM_RADIO_OFF)
                    }
                }
                ACTION_BEFORE -> {
                    mIsPlaying = true
                    Log.i("onStartCommand: mCurrentIndex $mIndex")
                    if (mIndex > 0)
                        mIndex -= 1
                    if (mIndex >= 1)
                        mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    mTitle = "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                }
                ACTION_NEXT -> {
                    mIsPlaying = true
                    Log.i("onStartCommand: mCurrentIndex $mIndex")
                    if (mIndex < mTracks.size - 1)
                        mIndex += 1
                    if (mIndex < mTracks.size - 1)
                        mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    mTitle = "FM ${mTracks[mIndex].toFloat() / 1000}Mhz"
                }
                ACTION_QUIT -> {
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    mAudioManager.setParameters(FM_RADIO_OFF)
                    mMediaSession.release()
                    stopSelf()
                }
                else -> {
                    throw IllegalAccessException("Unexpected Intent!")
                }
            }
        }
        mContext = this
        mFilePath = mContext.filesDir
        mNativeFMInterface = NativeFMInterface()
        mTitle = "FM ${mTracks[mIndex].toFloat() / 1000} Mhz"
        Log.i("onStartCommand: mTitle=$mTitle")
        mMediaSession = MediaSession(this, "FMRadio")
        setPlaybackState()
        sendMetaData("FM ${mTracks[mIndex].toFloat() / 1000}Mhz")
        mMediaSession.isActive = true
        startForeground(51,pushNotification())
        return START_STICKY
    }
    private fun setPlaybackState() {
        val state = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SKIP_TO_NEXT
                        or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackState.ACTION_STOP or PlaybackState.ACTION_PLAY_PAUSE
            )
            .build()
        mMediaSession.setPlaybackState(state)
    }
    private fun sendMetaData(mTitle: String) {
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, mTitle)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, resources.getString(R.string.app_name))
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_radio))
            .build()
        mMediaSession.setMetadata(metadata)
    }

    private fun pushNotification(): Notification {
        val nm = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            mContext.packageName, "FM Radio Playing",
            NotificationManager.IMPORTANCE_HIGH
        )
        nm.createNotificationChannel(channel)
        val builder = Notification.Builder(mContext, mContext.packageName)
        // Create a MediaStyle object and supply your media session token to it.
        val mediaStyle = Notification.MediaStyle().setMediaSession(mMediaSession.sessionToken)
        builder.setSmallIcon(R.drawable.ic_radio)
        builder.setOngoing(true)
        builder.setColor(resources.getColor(android.R.color.system_accent1_200, mContext.theme))
        builder.setColorized(true)
        builder.style = mediaStyle
        builder.setChannelId(mContext.packageName)
        val togglePlay = PendingIntent.getService(mContext, 0, Intent(ACTION_PLAY), PendingIntent.FLAG_IMMUTABLE)
        val forward = PendingIntent.getService(mContext, 0, Intent(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val rewind = PendingIntent.getService(mContext, 0, Intent(ACTION_BEFORE), PendingIntent.FLAG_IMMUTABLE)
        val close = PendingIntent.getService(mContext, 0, Intent(ACTION_QUIT), PendingIntent.FLAG_IMMUTABLE)
        val mPausePlayAction: Notification.Action = Notification.Action.Builder(
            if (mIsPlaying) {
                Icon.createWithResource(this, R.drawable.ic_pause)
            }else{
                Icon.createWithResource(this, R.drawable.ic_play)
            }, "Start/Stop", togglePlay

        ).build()
        val mRewindAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_rewind),
            "Rewind", rewind
        ).build()
        val mForwardAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_forward),
            "Forward", forward
        ).build()
        val mCloseAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_close),
            "Close", close
        ).build()
        builder.addAction(mRewindAction)
        builder.addAction(mPausePlayAction)
        builder.addAction(mForwardAction)
        builder.addAction(mCloseAction)
        return builder.build()
    }
    companion object {
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        private const val BEGIN_BG_SERVICE = "$PACKAGENAME.STARTBG"
        private const val ACTION_PLAY = "$PACKAGENAME.PLAY_FM"
        private const val ACTION_NEXT = "$PACKAGENAME.NEXT"
        private var mIsPlaying = true
        private const val ACTION_BEFORE = "$PACKAGENAME.BEFORE"
        private const val ACTION_QUIT = "$PACKAGENAME.QUIT"
        private const val FM_RADIO_ON = "l_fmradio_mode=on"
        private const val FM_RADIO_OFF = "l_fmradio_mode=off"
        private const val TAG = "FMRadioService"
        lateinit var mMediaSession : MediaSession
    }
}
