package com.eurekateam.samsungextras.dolby

import android.media.audiofx.AudioEffect
import java.util.*
import android.content.Context
import android.media.AudioManager
import android.os.IBinder
import android.content.Intent
import android.app.Service

class DolbyCore : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
	if (intent != null){
		if (intent.getBooleanExtra(DAP_ENABLED, false)){
			val PROFILE = intent.getIntExtra(DAP_PROFILE, PROFILE_AUTO)
        		mAudioEffect.setParameter(EFFECT_PARAM_EFF_ENAB, 1)
        		mAudioEffect.setParameter(EFFECT_PARAM_PROFILE, PROFILE)
        		mCurrentProfile = PROFILE
        		mAudioEffect.enabled = true
			(getSystemService(Context.AUDIO_SERVICE) as AudioManager).
				setParameters("${HW_DOLBY_ENABLE}1;${HW_DOLBY_PROFILE}${PROFILE}")
		} else {
			mAudioEffect.setParameter(EFFECT_PARAM_EFF_ENAB, 0)
			mAudioEffect.enabled = false
			(getSystemService(Context.AUDIO_SERVICE) as AudioManager).
                                setParameters("${HW_DOLBY_ENABLE}0;${HW_DOLBY_PROFILE}" +
                                	mCurrentProfile.toString())
		}
	}
	return START_NOT_STICKY
    }
    
    companion object {
        private val EFFECT_TYPE_DAP = UUID.fromString("46d279d9-9be7-453d-9d7c-ef937f675587")

        private const val EFFECT_PARAM_PROFILE = 0
        private const val EFFECT_PARAM_EFF_ENAB = 19
	const val DAP_ENABLED = "dap_enabled"
	const val DAP_PROFILE = "dap_profile"
        const val PROFILE_AUTO = 0
        const val PROFILE_MOVIE = 1
        const val PROFILE_MUSIC = 2
        const val PROFILE_VOICE = 3
	private const val HW_DOLBY_ENABLE = "g_effect_dolby_enable="
	private const val HW_DOLBY_PROFILE = "g_effect_dolby_profile="
	val mAudioEffect = AudioEffect(EFFECT_TYPE_DAP, AudioEffect.EFFECT_TYPE_NULL, 0, 0)
	var mCurrentProfile = PROFILE_AUTO
    }
}
