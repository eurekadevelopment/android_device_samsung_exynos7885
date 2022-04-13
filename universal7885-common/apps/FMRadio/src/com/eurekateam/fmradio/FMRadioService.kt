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
import com.eurekateam.fmradio.enums.OutputState
import com.eurekateam.fmradio.enums.PlayState
import com.eurekateam.fmradio.enums.PowerState
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.Log

class FMRadioService : Service() {
    private lateinit var mContext: Context
    private lateinit var mAudioManager: AudioManager
    private lateinit var mTracks: LongArray
    private lateinit var mNativeFMInterface: NativeFMInterface
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("--- FMRadio Background (IN) ---")
        fd = MainFragment.fd
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mTracks = MainFragment.mTracks
        mIndex = MainFragment.getIndex()
        if (mTracks.isEmpty() || mIndex == -1) {
            return super.onStartCommand(intent, flags, startId)
        }
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
                    Log.i("mCurrentIndex $mIndex")
                    if (mIndex > 0)
                        mIndex -= 1
                    mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    MainFragment.mFreqCurrent = mTracks[mIndex].toInt()
                }
                ACTION_NEXT -> {
                    mPlayState = PlayState.STATE_PLAYING
                    Log.i("mCurrentIndex $mIndex")
                    if (mIndex < mTracks.size - 1)
                        mIndex += 1
                    mNativeFMInterface.setFMFreq(fd, mTracks[mIndex].toInt())
                    MainFragment.mFreqCurrent = mTracks[mIndex].toInt()
                }
                ACTION_QUIT -> {
                    mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                    Log.i("--- FMRadio Background (OUT) ---")
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    mMediaSession.release()
                    stopSelf()
                }
                ACTION_OUTPUT -> {
                    changeOutputDevice(mOutput)
                    if (mOutput == OutputState.OUTPUT_HEADSET) {
                        mOutput = OutputState.OUTPUT_SPEAKER
                    } else if (mOutput == OutputState.OUTPUT_SPEAKER) {
                        mOutput = OutputState.OUTPUT_HEADSET
                    }
                }
                ACTION_START -> {
                    mMediaSession = MediaSession(this, "FMRadio")
                    mNativeFMInterface = NativeFMInterface()
                    mContext = this
                    setPlaybackState()
                    mMediaSession.isActive = true
                }
            }
        }
        sendMetaData("FM ${mTracks[mIndex].toFloat() / 1000} Mhz")
        startForeground(51, pushNotification())
        if (mOutput == OutputState.OUTPUT_SPEAKER) {
            changeOutputDevice(OutputState.OUTPUT_HEADSET)
        }
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
        builder.setOnlyAlertOnce(true)
        builder.setChannelId(mContext.packageName)
        val togglePlay = PendingIntent.getService(mContext, 0, Intent(ACTION_PLAY), PendingIntent.FLAG_IMMUTABLE)
        val forward = PendingIntent.getService(mContext, 0, Intent(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val rewind = PendingIntent.getService(mContext, 0, Intent(ACTION_BEFORE), PendingIntent.FLAG_IMMUTABLE)
        val close = PendingIntent.getService(mContext, 0, Intent(ACTION_QUIT), PendingIntent.FLAG_IMMUTABLE)
        val output = PendingIntent.getService(mContext, 0, Intent(ACTION_OUTPUT), PendingIntent.FLAG_IMMUTABLE)
        val mOutputAction: Notification.Action = Notification.Action.Builder(
            when (mOutput) {
                OutputState.OUTPUT_HEADSET -> {
                    Icon.createWithResource(this, R.drawable.ic_volume_up)
                }
                OutputState.OUTPUT_SPEAKER -> {
                    Icon.createWithResource(this, R.drawable.ic_headphones)
                }
            },
            "Output Configuration", output
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
            "Start/Stop", togglePlay

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
        private var mOutput: OutputState = MainFragment.mHeadset
        private var fd: Int = -1
        private var mIndex = -1
        lateinit var mMediaSession: MediaSession
    }
    private fun changeOutputDevice(output: OutputState) {
        if (output == OutputState.OUTPUT_SPEAKER) {
            mNativeFMInterface.setAudioRoute(false)
        } else if (output == OutputState.OUTPUT_HEADSET) {
            mNativeFMInterface.setAudioRoute(true)
        }
    }
}
