package com.eurekateam.fmradio.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.eurekateam.fmradio.SharedPreferencesConst
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.enums.HeadsetState
import com.eurekateam.fmradio.enums.PowerState
import com.eurekateam.fmradio.utils.IWaitUntil
import com.eurekateam.fmradio.utils.Log
import com.eurekateam.fmradio.utils.MaterialHelper
import com.eurekateam.fmradio.utils.WaitUntil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import vendor.eureka.hardware.fmradio.SetType
import vendor.eureka.hardware.fmradio.GetType
import java.text.DecimalFormat

class MainFragment :
    Fragment(R.layout.fragment_main),
    View.OnClickListener,
    SeekBar.OnSeekBarChangeListener {
    private val mFMInterface = NativeFMInterface()
    private lateinit var mVolumeUp: FloatingActionButton
    private lateinit var mVolumeDown: FloatingActionButton
    private lateinit var mSeekBar: AppCompatSeekBar
    private lateinit var mFavButton: FloatingActionButton
    private lateinit var mFMFreq: MaterialTextView
    private val mCleanFormat: DecimalFormat = DecimalFormat("0.#")
    private lateinit var mOutputSwitch: FloatingActionButton
    private lateinit var mBeforeChannelBtn: FloatingActionButton
    private lateinit var mNextChannelBtn: FloatingActionButton
    private lateinit var mPowerBtn: FloatingActionButton
    private lateinit var mAudioManager: AudioManager
    private lateinit var mStar: Drawable
    private lateinit var mStarFilled: Drawable
    private var mFavList : List<Int> = emptyList()
    private lateinit var  mSharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.fragment_main, container, false)
        super.onCreate(savedInstanceState)
        mStar = ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.ic_star,
            requireContext().theme
        )!!
        mStarFilled = ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.ic_star_filled,
            requireContext().theme
        )!!
        mAudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mVolumeUp = mRootView.findViewById(R.id.volume_up)
        mVolumeDown = mRootView.findViewById(R.id.volume_down)
        mSeekBar = mRootView.findViewById(R.id.volume_seekbar)
        mFMFreq = mRootView.findViewById(R.id.fm_freq)
        mBeforeChannelBtn = mRootView.findViewById(R.id.before_channel)
        mPowerBtn = mRootView.findViewById(R.id.fm_power)
        mFavButton = mRootView.findViewById(R.id.fav_button)
        mNextChannelBtn = mRootView.findViewById(R.id.next_channel)
        mOutputSwitch = mRootView.findViewById(R.id.fm_output_btn)
        mVolumeUp.setOnClickListener(this)
        mVolumeDown.setOnClickListener(this)
        mSeekBar.setOnSeekBarChangeListener(this)
        mBeforeChannelBtn.setOnClickListener(this)
        mPowerBtn.setOnClickListener(this)
        mNextChannelBtn.setOnClickListener(this)
        mOutputSwitch.setOnClickListener(this)
        mFavButton.setOnClickListener(this)
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        for (mResID in listOf(R.id.app_banner, R.id.fm_freq, R.id.freq_misc)) {
            mRootView.findViewById<MaterialTextView>(mResID).apply {
                setTextColor(MaterialHelper.build {
                     context = requireContext()
                     light = android.R.color.system_accent2_500
                     dark = android.R.color.system_accent2_100
                }.getValue())
            }
        }
        mRootView.findViewById<FrameLayout>(R.id.main_fragment).apply {
            setBackgroundColor(MaterialHelper.build {
                context = requireContext()
                light = android.R.color.system_accent1_100
                dark = android.R.color.system_accent1_700
            }.getValue())
        }
        val mVolume = mSharedPref.getInt(SharedPreferencesConst.PREF_VOLUME, 8)
        mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, mVolume)
        mSeekBar.min = 1
        mSeekBar.max = 15
        mSeekBar.progress = mVolume
        updateOutputStatus(false)
        if (!mFreqSearchDone) {
           val mRestoreFreq = mSharedPref.getInt(SharedPreferencesConst.PREF_FREQ, mFMInterface.mDevCtl.getValue(GetType.GET_TYPE_FM_LOWER_LIMIT))
           mFMInterface.mDefaultCtl.setValue(SetType.SET_TYPE_FM_FREQ, mRestoreFreq)
           mFMFreq.text = mCleanFormat.format(mRestoreFreq.toFloat() / 1000)
           mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_THREAD, 1)
           val loading = ProgressDialog(requireContext())
	   loading.setMessage(requireContext().resources.getString(R.string.update_freq))
	   loading.setCancelable(false)
	   loading.setInverseBackgroundForced(false)
	   loading.show()
	   mFMInterface.mDefaultCtl.setValue(SetType.SET_TYPE_FM_SEARCH_START, 0)
           WaitUntil.setTimer(
               requireActivity(),
               object : IWaitUntil {
                   override fun cond(): Boolean = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_MUTEX_LOCKED) == 0
                   override fun todo() {
                       val mList = mFMInterface.mDefaultCtl.getFreqsList()
                       for (i in mList) {
                           if (mSharedPref.getBoolean(SharedPreferencesConst.assembleFavFreq(i), false))
                               mFavList += i
                       }
                       if (!mFMPower) mUpdateEnableDisable(false)
                       loading.hide()
                   }
               }
           )
           mFreqSearchDone = true
        } else {
           val mNewFreq = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ)
           mFMFreq.text = mCleanFormat.format(mNewFreq.toFloat() / 1000)
        }
        // Update track list and fav button
        return mRootView
    }

    /**
     * Extension function for [View], for Grayed out, disabled View
     * @param mTextView Whether the target view is touchable [FloatingActionButton] or
     * [MaterialTextView] with useless touch attr
     */
    private fun View.disable(mTextView: Boolean = false) {
        alpha = .7f
        if (!mTextView) isEnabled = false
    }

    /**
     * Extension function for [View], for reverting Grayed out, disabled View
     * @param mTextView Whether the target view is touchable [FloatingActionButton] or
     * [MaterialTextView] with useless touch attr
     */
    private fun View.enable(mTextView: Boolean = false) {
        alpha = 1.0f
        if (!mTextView) isEnabled = true
    }

    /**
     * Uses array of [R] to make the [View] look disabled or enabled
     * @param mEnabled Should we enable the Views or not
     * @param mView The root view we should find views such as [FloatingActionButton].
     * Defaults to [requireView]
     * @see [enable]
     * @see [disable]
     */
    private fun mUpdateEnableDisable(mEnabled: Boolean, mView: View = requireView()) {
        val mTextViewList = listOf(R.id.fm_freq, R.id.freq_misc)
        val mFloatButtonList = listOf(
            R.id.volume_down,
            R.id.volume_up,
            R.id.before_channel,
            R.id.next_channel,
            R.id.fm_output_btn
        )
        for (i in mTextViewList) {
            if (mEnabled) {
                mView.findViewById<View>(i).enable(true)
            } else {
                mView.findViewById<View>(i).disable(true)
            }
        }
        for (i in mFloatButtonList) {
            if (mEnabled) {
                mView.findViewById<View>(i).enable()
            } else {
                mView.findViewById<View>(i).disable()
            }
        }
    }

    private fun updateOutputStatus(mInverted : Boolean) {
        var mCurrent = mSharedPref.getBoolean(SharedPreferencesConst.PREF_AUDIO_OUTPUT, false)
        if (mInverted) mCurrent = !mCurrent
        if (mCurrent) {
            mOutputSwitch.setImageIcon(
                 Icon.createWithResource(
                       requireContext(),
                       R.drawable.ic_volume_up
                 )
            )
            mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_SPEAKER_ROUTE, 1)
        } else {
            mOutputSwitch.setImageIcon(
                Icon.createWithResource(
                     requireContext(),
                     R.drawable.ic_headphones
                )
            )
            mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_SPEAKER_ROUTE, 0)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            mOutputSwitch.id -> {
                val mCurrent = mSharedPref.getBoolean(SharedPreferencesConst.PREF_AUDIO_OUTPUT, false)
                updateOutputStatus(true)
                mSharedPref.edit().putBoolean(SharedPreferencesConst.PREF_AUDIO_OUTPUT, !mCurrent).apply()
            }
            mVolumeUp.id -> {
                var mCurrentVolume = mSharedPref.getInt(SharedPreferencesConst.PREF_VOLUME, 8)
                if (mCurrentVolume < 15) {
                    mCurrentVolume = mCurrentVolume + 1
                }
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, mCurrentVolume)
                mSeekBar.progress = mCurrentVolume
                Toast.makeText(requireContext(), "Volume set to $mCurrentVolume", Toast.LENGTH_SHORT).show()
                mSharedPref.edit().putInt(SharedPreferencesConst.PREF_VOLUME, mCurrentVolume).apply()
            }
            mVolumeDown.id -> {
                var mCurrentVolume = mSharedPref.getInt(SharedPreferencesConst.PREF_VOLUME, 8)
                if (mCurrentVolume > 0) {
                    mCurrentVolume = mCurrentVolume - 1
                }
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, mCurrentVolume)
                mSeekBar.progress = mCurrentVolume
                Toast.makeText(requireContext(), "Volume set to $mCurrentVolume", Toast.LENGTH_SHORT).show()
                mSharedPref.edit().putInt(SharedPreferencesConst.PREF_VOLUME, mCurrentVolume).apply()
            }
            mBeforeChannelBtn.id -> {
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, 1)
                val mNewFreq = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_BEFORE_CHANNEL)
                mFMFreq.text = mCleanFormat.format(mNewFreq.toFloat() / 1000)
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, 0)
                if (mSharedPref.getBoolean(SharedPreferencesConst.assembleFavFreq(mNewFreq), false)) {
                    mFavButton.setImageDrawable(mStarFilled)
                }
                mSharedPref.edit().putInt(SharedPreferencesConst.PREF_FREQ, mNewFreq).apply()
            }
            mPowerBtn.id -> {
                if (mFMPower) {
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    mFMFreq.text = getText(R.string.inital_freq)
                } else {
                    mAudioManager.setParameters(PowerState.FM_POWER_ON.mAudioParam)
                    mFMFreq.text = mCleanFormat.format(mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ).toFloat() / 1000)
                }
                mUpdateEnableDisable(!mFMPower)
                mFMPower = !mFMPower
            }
            mNextChannelBtn.id -> {
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, 1)
                val mNewFreq = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_NEXT_CHANNEL)
                mFMFreq.text = mCleanFormat.format(mNewFreq.toFloat() / 1000)
                mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_MUTE, 0)
                if (mSharedPref.getBoolean(SharedPreferencesConst.assembleFavFreq(mNewFreq), false)) {
                    mFavButton.setImageDrawable(mStarFilled)
                }
                mSharedPref.edit().putInt(SharedPreferencesConst.PREF_FREQ, mNewFreq).apply()
            }
            mFavButton.id -> {
                val mCurrFreq = mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ)
                val mCurr = mSharedPref.getBoolean(SharedPreferencesConst.assembleFavFreq(mCurrFreq), false)
                if (mCurr) { mFavButton.setImageDrawable(mStar) } else { mFavButton.setImageDrawable(mStarFilled) }
                mSharedPref.edit().putBoolean(SharedPreferencesConst.assembleFavFreq(mCurrFreq), !mCurr).apply()
            }
        }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        mFMInterface.mDevCtl.setValue(SetType.SET_TYPE_FM_VOLUME, p1)
        mSharedPref.edit().putInt("volume", p1).apply()
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}

    companion object {
        var mFMPower = false
        var mFreqSearchDone = false
        var mHeadSetPlugged: HeadsetState = HeadsetState.HEADSET_STATE_DISCONNECTED
    }

    /**
     * Helper function to remove zero values on a [IntArray].
     * Since we don't know the channel count, we initialize it with size 30.
     * But most likely the channel count is less then 30, so remaining values
     * are filled with zero.
     *
     * @param array The target Long array
     * @return Long array with zeros removed
     */
    private fun removeZeros(array: IntArray): IntArray {
        val mArray: MutableList<Int> = mutableListOf()
        Log.d("removeZeros: Got array size ${array.size}")
        for (i in array.indices) {
            if (array[i] > 0L) {
                mArray.add(array[i])
            }
        }
        return mArray.toIntArray()
    }
}
