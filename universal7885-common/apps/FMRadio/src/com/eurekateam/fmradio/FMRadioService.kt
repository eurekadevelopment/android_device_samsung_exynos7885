package com.eurekateam.fmradio

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.eurekateam.fmradio.enums.PlayState
import com.eurekateam.fmradio.enums.PowerState
import com.eurekateam.fmradio.utils.Log
import vendor.eureka.hardware.fmradio.SetType
import vendor.eureka.hardware.fmradio.GetType

class FMRadioService : Service() {
    private lateinit var mContext: Context
    private lateinit var mAudioManager: AudioManager
    private val mNativeFMInterface = NativeFMInterface()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("--- FMRadio Background (IN) ---")
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (intent != null) {
            intent.action?.let { Log.i(it) }
            when (intent.action) {
                ACTION_PLAY -> {
                    if (mPlayState == PlayState.STATE_PLAYING) {
                        mPlayState = PlayState.STATE_STOPPED
                        mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    } else if (mPlayState == PlayState.STATE_STOPPED) {
                        mPlayState = PlayState.STATE_PLAYING
                        mAudioManager.setParameters(PowerState.FM_POWER_ON.mAudioParam)
                    }
                }
                ACTION_BEFORE -> {
                    mPlayState = PlayState.STATE_PLAYING
                    mNativeFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_BEFORE_CHANNEL)
                }
                ACTION_NEXT -> {
                    mPlayState = PlayState.STATE_PLAYING
                    mNativeFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_NEXT_CHANNEL)
                }
                ACTION_QUIT -> {
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    Log.i("--- FMRadio Background (OUT) ---")
                    mNativeFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_THREAD, 0)
                    mNativeFMInterface.mDevCtl.close()
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    mMediaSession.release()
                    stopSelf()
                }
                ACTION_OUTPUT -> {
                    var mSpeaker = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("speaker", false)
                    mSpeaker = !mSpeaker
                    changeOutputDevice(mSpeaker)
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("speaker", mSpeaker).apply()
                }
                ACTION_START -> {
                    mMediaSession = MediaSession(this, "FMRadio")
                    setPlaybackState()
                    mMediaSession.isActive = true
                    mContext = this
                }
            }
        }
        sendMetaData("FM ${mNativeFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ).toFloat() / 1000} Mhz")
        startForeground(51, pushNotification())
        Log.i("--- FMRadio Background (OUT) ---")
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
            .putBitmap(
                MediaMetadata.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_radio)
            )
            .build()
        mMediaSession.setMetadata(metadata)
    }

    private fun pushNotification(): Notification {
        val nm = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            mContext.packageName,
            "FM Radio Playing",
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
        builder.setOnlyAlertOnce(true)
        builder.setChannelId(mContext.packageName)
        val togglePlay = PendingIntent.getService(mContext, 0, Intent(ACTION_PLAY), PendingIntent.FLAG_IMMUTABLE)
        val forward = PendingIntent.getService(mContext, 0, Intent(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val rewind = PendingIntent.getService(mContext, 0, Intent(ACTION_BEFORE), PendingIntent.FLAG_IMMUTABLE)
        val close = PendingIntent.getService(mContext, 0, Intent(ACTION_QUIT), PendingIntent.FLAG_IMMUTABLE)
        val output = PendingIntent.getService(mContext, 0, Intent(ACTION_OUTPUT), PendingIntent.FLAG_IMMUTABLE)
        val mOutputAction: Notification.Action = Notification.Action.Builder(
            if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("speaker", false)) {
                Icon.createWithResource(this, R.drawable.ic_volume_up)
            } else {
                Icon.createWithResource(this, R.drawable.ic_headphones)
            },
            "Output Configuration",
            output
        ).build()
        val mPausePlayAction: Notification.Action = Notification.Action.Builder(
            when (mPlayState) {
                PlayState.STATE_PLAYING -> {
                    Icon.createWithResource(this, R.drawable.ic_pause)
                }
                PlayState.STATE_STOPPED -> {
                    Icon.createWithResource(this, R.drawable.ic_play)
                }
            },
            "Start/Stop",
            togglePlay

        ).build()
        val mRewindAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_rewind),
            "Rewind",
            rewind
        ).build()
        val mForwardAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_forward),
            "Forward",
            forward
        ).build()
        val mCloseAction: Notification.Action = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_close),
            "Close",
            close
        ).build()
        builder.addAction(mOutputAction)
        builder.addAction(mRewindAction)
        builder.addAction(mPausePlayAction)
        builder.addAction(mForwardAction)
        builder.addAction(mCloseAction)
        return builder.build()
    }
    companion object {
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        private const val ACTION_START = "$PACKAGENAME.START"
        private const val ACTION_PLAY = "$PACKAGENAME.PLAY_FM"
        private const val ACTION_NEXT = "$PACKAGENAME.NEXT"
        private const val ACTION_BEFORE = "$PACKAGENAME.BEFORE"
        private const val ACTION_QUIT = "$PACKAGENAME.QUIT"
        private const val ACTION_OUTPUT = "$PACKAGENAME.OUTPUT"
        private var mPlayState: PlayState = PlayState.STATE_PLAYING
        lateinit var mMediaSession: MediaSession
    }
    private fun changeOutputDevice(speaker: Boolean) {
        mNativeFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_SPEAKER_ROUTE, if (speaker) 1 else 0)
    }
}
