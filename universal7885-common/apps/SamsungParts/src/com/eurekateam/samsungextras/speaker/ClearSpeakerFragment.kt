/*
 * Copyright (C) 2020 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eurekateam.samsungextras.speaker

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.eurekateam.samsungextras.R
import java.io.IOException

class ClearSpeakerFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private var mAudioManager: AudioManager? = null
    private var mHandler: Handler? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mClearSpeakerPref: SwitchPreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.clear_speaker_settings)
        mClearSpeakerPref = findPreference(PREF_CLEAR_SPEAKER)
        mClearSpeakerPref!!.onPreferenceChangeListener = this
        mHandler = Handler(Looper.getMainLooper())
        mAudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference === mClearSpeakerPref) {
            val value = newValue as Boolean
            if (value) {
                if (startPlaying()) {
                    mHandler!!.removeCallbacksAndMessages(null)
                    mHandler!!.postDelayed({ stopPlaying() }, 30000)
                    return true
                }
            }
        }
        return false
    }

    override fun onStop() {
        stopPlaying()
        super.onStop()
    }

    /**
     * Start playing speaker-clearing audio
     * @return true on success, else false
     */
    private fun startPlaying(): Boolean {
        mAudioManager!!.setParameters("status_earpiece_clean=on")
        mMediaPlayer = MediaPlayer()
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer!!.isLooping = true
        try {
            resources.openRawResourceFd(R.raw.clear_speaker_sound).use { file ->
                mMediaPlayer!!.setDataSource(
                    file.fileDescriptor,
                    file.startOffset,
                    file.length
                )
            }
            mClearSpeakerPref!!.isEnabled = false
            mMediaPlayer!!.setVolume(1.0f, 1.0f)
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()
        } catch (ioe: IOException) {
            Log.e(TAG, "Failed to play speaker clean sound!", ioe)
            return false
        }
        return true
    }

    /**
     * Stops and invalidates
     */
    private fun stopPlaying() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
        }
        mAudioManager!!.setParameters("status_earpiece_clean=off")
        mClearSpeakerPref!!.isEnabled = true
        mClearSpeakerPref!!.isChecked = false
    }

    companion object {
        private val TAG = ClearSpeakerFragment::class.java.simpleName
        private const val PREF_CLEAR_SPEAKER = "clear_speaker_pref"
    }
}