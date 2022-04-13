package com.eurekateam.fmradio.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.enums.HeadsetState
import com.eurekateam.fmradio.enums.OutputState
import com.eurekateam.fmradio.enums.PowerState
import com.eurekateam.fmradio.utils.FileUtilities
import com.eurekateam.fmradio.utils.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.fragment_main, container, false)
        super.onCreate(savedInstanceState)
        mStar = ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.ic_star, requireContext().theme
        )!!
        mStarFilled = ResourcesCompat.getDrawable(
            requireContext().resources,
            R.drawable.ic_star_filled, requireContext().theme
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
        var mIsLight = true
        val nightModeFlags = requireContext().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> mIsLight = false
            Configuration.UI_MODE_NIGHT_NO -> mIsLight = true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mIsLight = true
        }
        val mTextViewList = listOf(R.id.app_banner, R.id.fm_freq, R.id.freq_misc)
        for (mResID in mTextViewList) {
            mRootView.findViewById<MaterialTextView>(mResID).apply {
                if (mIsLight)
                    setTextColor(resources.getColor(android.R.color.system_accent2_500, requireContext().theme))
                else
                    setTextColor(resources.getColor(android.R.color.system_accent2_100, requireContext().theme))
            }
        }
        mRootView.findViewById<FrameLayout>(R.id.main_fragment).apply {
            if (mIsLight)
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_100, requireContext().theme))
            else
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_700, requireContext().theme))
        }
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                if (FileUtilities.checkIfExistFile(FileUtilities.mFavouriteChannelFileName, requireContext())) {
                    val mFavData = FileUtilities.readFromFile(
                        FileUtilities.mFavouriteChannelFileName, requireContext()
                    )
                    for (mItem in mFavData.split("\\r?\\n".toRegex())) {
                        if (mItem.isNotBlank())
                            mFavStats[mItem.toInt()] = true
                    }
                }
                var mMute = false
                if (mFreqCurrent == -1) {
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    withContext(Dispatchers.Main) {
                        mUpdateEnableDisable(false, mRootView)
                    }
                    mMute = true
                }

                if (FileUtilities.checkIfExistFile(FileUtilities.mFMFreqFileName, requireContext())) {
                    mFreqCurrent = FileUtilities.readFromFile(
                        FileUtilities.mFMFreqFileName,
                        requireContext()
                    ).toInt()
                    mFMInterface.setFMFreq(fd, mFreqCurrent)
                    withContext(Dispatchers.Main) {
                        mFMFreq.text = mCleanFormat.format(mFreqCurrent.toFloat() / 1000)
                    }
                }
                if (FileUtilities.checkIfExistFile(FileUtilities.mFMVolumeFileName, requireContext())) {
                    mVolume = FileUtilities.readFromFile(
                        FileUtilities.mFMVolumeFileName,
                        requireContext()
                    ).toInt()
                    mFMInterface.setFMVolume(fd, mVolume)
                    withContext(Dispatchers.Main) {
                        mSeekBar.progress = mVolume
                    }
                }
                withContext(Dispatchers.Main) {
                    mSeekBar.min = 1
                    mSeekBar.max = 15
                }
                withContext(Dispatchers.IO) {
                    if (mVolume == -1) {
                        mVolume = 8
                        mFMInterface.setFMVolume(fd, mVolume)
                        withContext(Dispatchers.Main) {
                            mSeekBar.progress = mVolume
                        }
                    }
                }
                if (!mMute)
                    mFMInterface.setFMMute(fd, true)
                mFMInterface.setFMFreq(fd, mFMInterface.getFMLower(fd))
                mRefreshTracks()
                if (mFreqCurrent != -1) {
                    mFMInterface.setFMFreq(fd, mFreqCurrent)
                } else {
                    mFreqCurrent = mFMInterface.getFMLower(fd)
                }
                mFMInterface.setFMBoot(fd)
                mFreqCurrent = mFMInterface.getFMFreq(fd).toInt()
                mFMInterface.setFMFreq(fd, mFreqCurrent)
                withContext(Dispatchers.Main) {
                    mFMFreq.text = mCleanFormat.format(mFreqCurrent.toFloat() / 1000)
                }
                if (!mMute)
                    mFMInterface.setFMMute(fd, false)
                if (mFreqCurrent == -1)
                    mFMInterface.setFMThread(fd, true)
                withContext(Dispatchers.Main) {
                    mFavButton.let {
                        if (mFavStats[mFreqCurrent] == null) {
                            mFavStats.putIfAbsent(mFreqCurrent, false)
                        }
                        if (mFavStats[mFreqCurrent]!!) {
                            it.setImageDrawable(mStarFilled)
                        } else {
                            it.setImageDrawable(mStar)
                        }
                    }
                }
            }
        }
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
            R.id.volume_down, R.id.volume_up,
            R.id.before_channel, R.id.next_channel, R.id.fm_output_btn
        )
        for (i in mTextViewList) {
            if (mEnabled)
                mView.findViewById<View>(i).enable(true)
            else
                mView.findViewById<View>(i).disable(true)
        }
        for (i in mFloatButtonList) {
            if (mEnabled)
                mView.findViewById<View>(i).enable()
            else
                mView.findViewById<View>(i).disable()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            mOutputSwitch.id -> {
                if (mHeadset == OutputState.OUTPUT_HEADSET) {
                    mOutputSwitch.setImageIcon(
                        Icon.createWithResource(
                            requireContext(),
                            R.drawable.ic_volume_up
                        )
                    )
                    val ret = mFMInterface.setAudioRoute(true)
                    Log.i("mFMInterface.setAudioRoute return $ret")
                } else if (mHeadset == OutputState.OUTPUT_SPEAKER) {
                    mOutputSwitch.setImageIcon(
                        Icon.createWithResource(
                            requireContext(),
                            R.drawable.ic_headphones
                        )
                    )
                    val ret = mFMInterface.setAudioRoute(false)
                    Log.i("mFMInterface.setAudioRoute return $ret")
                }
                if (mHeadset == OutputState.OUTPUT_HEADSET) {
                    mHeadset = OutputState.OUTPUT_SPEAKER
                } else if (mHeadset == OutputState.OUTPUT_SPEAKER) {
                    mHeadset = OutputState.OUTPUT_HEADSET
                }
            }
            mVolumeUp.id -> {
                if (mVolume < 15)
                    mVolume += 1
                mFMInterface.setFMVolume(fd, mVolume)
                mSeekBar.progress = mVolume
                Toast.makeText(requireContext(), "Volume set to $mVolume", Toast.LENGTH_SHORT).show()
                FileUtilities.writeToFile(
                    FileUtilities.mFMVolumeFileName,
                    mVolume.toString(), requireContext()
                )
            }
            mVolumeDown.id -> {
                if (mVolume > 0)
                    mVolume -= 1
                mFMInterface.setFMVolume(fd, mVolume)
                mSeekBar.progress = mVolume
                Toast.makeText(requireContext(), "Volume set to $mVolume", Toast.LENGTH_SHORT).show()
                FileUtilities.writeToFile(
                    FileUtilities.mFMVolumeFileName,
                    mVolume.toString(), requireContext()
                )
            }
            mBeforeChannelBtn.id -> {
                mFMInterface.setFMMute(fd, true)
                val mTempFreq = mFMInterface.getBeforeChannel(fd)
                if (mTempFreq > mFMInterface.getFMLower(fd) && mTempFreq < mFMInterface.getFmUpper(
                        fd
                    )
                ) {
                    mFreqCurrent = mTempFreq
                }
                if (!mFMInterface.getSysfsSupport()) {
                    mFMInterface.setFMFreq(fd, mFreqCurrent)
                }
                mFMInterface.setFMMute(fd, false)
                FileUtilities.writeToFile(
                    FileUtilities.mFMFreqFileName,
                    mFreqCurrent.toString(), requireContext()
                )
                mFavButton.let {
                    if (mFavStats[mFreqCurrent] == null) {
                        mFavStats.putIfAbsent(mFreqCurrent, false)
                    }
                    if (mFavStats[mFreqCurrent]!!) {
                        it.setImageDrawable(mStarFilled)
                    } else {
                        it.setImageDrawable(mStar)
                    }
                }
            }
            mPowerBtn.id -> {
                if (mFMPower) {
                    mAudioManager.setParameters(PowerState.FM_POWER_OFF.mAudioParam)
                    mFMFreq.text = getText(R.string.inital_freq)
                } else {
                    mAudioManager.setParameters(PowerState.FM_POWER_ON.mAudioParam)
                    mFMFreq.text = mCleanFormat.format(mFreqCurrent.toFloat() / 1000)
                }
                mUpdateEnableDisable(!mFMPower)
                mFMPower = !mFMPower
            }
            mNextChannelBtn.id -> {
                mFMInterface.setFMMute(fd, true)
                val mTempFreq = mFMInterface.getNextChannel(fd)
                if (mTempFreq > mFMInterface.getFMLower(fd) && mTempFreq < mFMInterface.getFmUpper(
                        fd
                    )
                ) {
                    mFreqCurrent = mTempFreq
                }
                if (!mFMInterface.getSysfsSupport()) {
                    mFMInterface.setFMFreq(fd, mFreqCurrent)
                }
                mFMInterface.setFMMute(fd, false)
                FileUtilities.writeToFile(
                    FileUtilities.mFMFreqFileName,
                    mFreqCurrent.toString(), requireContext()
                )
                mFavButton.let {
                    if (mFavStats[mFreqCurrent] == null) {
                        mFavStats.putIfAbsent(mFreqCurrent, false)
                    }
                    if (mFavStats[mFreqCurrent]!!) {
                        it.setImageDrawable(mStarFilled)
                    } else {
                        it.setImageDrawable(mStar)
                    }
                }
            }
            mFavButton.id -> {
                val mIndex = mFreqCurrent
                if (mFavStats[mIndex] == null) {
                    mFavStats.putIfAbsent(mIndex, false)
                }
                mFavStats[mIndex] = !mFavStats[mIndex]!!
                mFavButton.let {
                    if (mFavStats[mIndex]!!) {
                        it.setImageDrawable(mStarFilled)
                    } else {
                        it.setImageDrawable(mStar)
                    }
                }
                Log.d(
                    "Fav stats for $mIndex changed. " +
                        "Current value ${mFavStats[mIndex]}"
                )
            }
        }
        mFMFreq.text = mCleanFormat.format(mFreqCurrent.toFloat() / 1000)
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        mFMInterface.setFMVolume(fd, p1)
        FileUtilities.writeToFile(FileUtilities.mFMVolumeFileName, p1.toString(), requireContext())
        mVolume = p1
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {
        Toast.makeText(requireContext(), "Volume set to $mVolume", Toast.LENGTH_SHORT).show()
    }
    companion object {
        private var mVolume = -1
        var fd = -1
        var mHeadset = OutputState.OUTPUT_SPEAKER
        var mFreqCurrent = -1
        private var mFMPower = false
        var mHeadSetPlugged: HeadsetState = HeadsetState.HEADSET_STATE_DISCONNECTED
        var mTracks: LongArray = emptyArray<Long>().toLongArray()
        fun mRefreshTracks() {
            NativeFMInterface().setFMFreq(fd, NativeFMInterface().getFMLower(fd))
            mTracks = NativeFMInterface().getFMTracks(fd)
            mTracks = MainFragment().removeZeros(mTracks)
            (mFreqCurrent != -1).let { NativeFMInterface().setFMFreq(fd, mFreqCurrent) }
        }
        fun getIndex(): Int {
            return mTracks.indexOf(mFreqCurrent.toLong())
        }
        val mFavStats = HashMap<Int, Boolean>(30)
    }

    /**
     * Helper function to remove zero values on a [LongArray].
     * Since we don't know the channel count, we initialize it with size 30.
     * But most likely the channel count is less then 30, so remaining values
     * are filled with zero.
     *
     * @param array The target Long array
     * @return Long array with zeros removed
     */
    private fun removeZeros(array: LongArray): LongArray {
        val mArray: MutableList<Long> = emptyList<Long>().toMutableList()
        Log.d("removeZeros: Got array size ${array.size}")
        for (i in array.indices) {
            if (array[i] > 0L) {
                mArray.add(array[i])
            }
        }
        return mArray.toLongArray()
    }
}
