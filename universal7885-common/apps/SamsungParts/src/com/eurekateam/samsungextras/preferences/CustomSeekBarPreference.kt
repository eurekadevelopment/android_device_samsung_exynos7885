package com.eurekateam.samsungextras.preferences

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.R

open class CustomSeekBarPreference @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = TypedArrayUtils.getAttr(
        context,
        R.attr.preferenceStyle,
        android.R.attr.preferenceStyle
    ), defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), OnSeekBarChangeListener,
    View.OnClickListener, OnLongClickListener {
    private val TAG: String = javaClass.name
    private var mInterval = 1
    private var mShowSign = false
    private var mUnits = ""
    private var mContinuousUpdates = false
    private var mMinValue = 1
    private var mMaxValue = 256
    private var mDefaultValueExists: Boolean
    private var mDefaultValue = 0
    private var mValue = 0
    private var mValueTextView: TextView? = null
    private var mResetImageView: ImageView? = null
    private var mMinusImageView: ImageView? = null
    private var mPlusImageView: ImageView? = null
    private var mSeekBar: SeekBar
    private var mTrackingTouch = false
    private var mTrackingValue = 0
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        try {
            // move our seekbar to the new view we've been given
            val oldContainer = mSeekBar.parent
            val newContainer =
                holder.findViewById(com.eurekateam.samsungextras.R.id.seekbar) as ViewGroup
            if (oldContainer !== newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    (oldContainer as ViewGroup).removeView(mSeekBar)
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews()
                newContainer.addView(
                    mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error binding view: $ex")
        }
        mSeekBar.max = getSeekValue(mMaxValue)
        mSeekBar.progress = getSeekValue(mValue)
        mSeekBar.isEnabled = isEnabled
        mValueTextView = holder.findViewById(com.eurekateam.samsungextras.R.id.value) as TextView
        mResetImageView = holder.findViewById(com.eurekateam.samsungextras.R.id.reset) as ImageView
        mMinusImageView = holder.findViewById(com.eurekateam.samsungextras.R.id.minus) as ImageView
        mPlusImageView = holder.findViewById(com.eurekateam.samsungextras.R.id.plus) as ImageView
        updateValueViews()
        mSeekBar.setOnSeekBarChangeListener(this)
        mResetImageView!!.setOnClickListener(this)
        mMinusImageView!!.setOnClickListener(this)
        mPlusImageView!!.setOnClickListener(this)
        mResetImageView!!.onLongClickListener = this
        mMinusImageView!!.onLongClickListener = this
        mPlusImageView!!.onLongClickListener = this
    }

    private fun getLimitedValue(v: Int): Int {
        return if (v < mMinValue) mMinValue else v.coerceAtMost(mMaxValue)
    }

    private fun getSeekValue(v: Int): Int {
        return -Math.floorDiv(mMinValue - v, mInterval)
    }

    private fun getTextValue(v: Int): String {
        return (if (mShowSign && v > 0) "+" else "") + v + mUnits
    }

    private fun updateValueViews() {
        if (mValueTextView != null) {
            mValueTextView!!.text = context.getString(
                com.eurekateam.samsungextras.R.string.custom_seekbar_value,
                if (!mTrackingTouch || mContinuousUpdates) getTextValue(mValue) +
                        (if (mDefaultValueExists && mValue == mDefaultValue) " (" +
                                context.getString(com.eurekateam.samsungextras.R.string.custom_seekbar_default_value) + ")" else "") else getTextValue(
                    mTrackingValue
                )
            )
        }
        if (mResetImageView != null) {
            if (!mDefaultValueExists || mValue == mDefaultValue || mTrackingTouch) mResetImageView!!.visibility =
                View.INVISIBLE else mResetImageView!!.visibility = View.VISIBLE
        }
        if (mMinusImageView != null) {
            if (mValue == mMinValue || mTrackingTouch) {
                mMinusImageView!!.isClickable = false
                mMinusImageView!!.setColorFilter(
                    context.getColor(com.eurekateam.samsungextras.R.color.disabled_text_color),
                    PorterDuff.Mode.MULTIPLY
                )
            } else {
                mMinusImageView!!.isClickable = true
                mMinusImageView!!.clearColorFilter()
            }
        }
        if (mPlusImageView != null) {
            if (mValue == mMaxValue || mTrackingTouch) {
                mPlusImageView!!.isClickable = false
                mPlusImageView!!.setColorFilter(
                    context.getColor(com.eurekateam.samsungextras.R.color.disabled_text_color),
                    PorterDuff.Mode.MULTIPLY
                )
            } else {
                mPlusImageView!!.isClickable = true
                mPlusImageView!!.clearColorFilter()
            }
        }
    }

    private fun changeValue() {
        // for subclasses
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val newValue = getLimitedValue(mMinValue + progress * mInterval)
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue
            updateValueViews()
        } else if (mValue != newValue) {
            // change rejected, revert to the previous value
            if (!callChangeListener(newValue)) {
                mSeekBar.progress = getSeekValue(mValue)
                return
            }
            // change accepted, store it
            changeValue()
            persistInt(newValue)
            mValue = newValue
            updateValueViews()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mTrackingValue = mValue
        mTrackingTouch = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mTrackingTouch = false
        if (!mContinuousUpdates) onProgressChanged(mSeekBar, getSeekValue(mTrackingValue), false)
        notifyChanged()
    }

    override fun onClick(v: View) {
        when (v.id) {
            com.eurekateam.samsungextras.R.id.reset -> {
                Toast.makeText(
                    context,
                    context.getString(
                        com.eurekateam.samsungextras.R.string.custom_seekbar_default_value_to_set,
                        getTextValue(mDefaultValue)
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
            com.eurekateam.samsungextras.R.id.minus -> {
                setValue(mValue - mInterval, true)
            }
            com.eurekateam.samsungextras.R.id.plus -> {
                setValue(mValue + mInterval, true)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            com.eurekateam.samsungextras.R.id.reset -> {
                setValue(mDefaultValue, true)
            }
            com.eurekateam.samsungextras.R.id.minus -> {
                setValue(
                    if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue < mValue * 2) Math.floorDiv(
                        mMaxValue + mMinValue,
                        2
                    ) else mMinValue, true
                )
            }
            com.eurekateam.samsungextras.R.id.plus -> {
                setValue(
                    if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue > mValue * 2) -1 * Math.floorDiv(
                        -1 * (mMaxValue + mMinValue),
                        2
                    ) else mMaxValue, true
                )
            }
        }
        return true
    }

    // dont need too much shit about initial and default values
    // its all done in constructor already
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (restoreValue) mValue = getPersistedInt(mValue)
    }

    override fun setDefaultValue(defaultValue: Any) {
        if (defaultValue is Int) setDefaultValue(
            defaultValue,
            true
        ) else setDefaultValue(
            defaultValue.toString(), true
        )
    }

    private fun setDefaultValue(newValue: Int, update: Boolean) {
        var mNewValue = newValue
        mNewValue = getLimitedValue(mNewValue)
        if (!mDefaultValueExists || mDefaultValue != mNewValue) {
            mDefaultValueExists = true
            mDefaultValue = mNewValue
            if (update) updateValueViews()
        }
    }

    private fun setDefaultValue(newValue: String?, update: Boolean) {
        if (mDefaultValueExists && (newValue == null || newValue.isEmpty())) {
            mDefaultValueExists = false
            if (update) updateValueViews()
        } else if (newValue != null && newValue.isNotEmpty()) {
            setDefaultValue(newValue.toInt(), update)
        }
    }

    fun setMax(max: Int) {
        mMaxValue = max
        mSeekBar.max = mMaxValue - mMinValue
    }

    fun setMin(min: Int) {
        mMinValue = min
        mSeekBar.max = mMaxValue - mMinValue
    }

    fun setValue(newValue: Int, update: Boolean) {
        var mNewValue = newValue
        mNewValue = getLimitedValue(mNewValue)
        if (mValue != mNewValue) {
            if (update) mSeekBar.progress = getSeekValue(mNewValue) else mValue = mNewValue
        }
    }

    // need some methods here to set/get other attrs at runtime,
    var value: Int
        get() = mValue
        set(newValue) {
            mValue = getLimitedValue(newValue)
            mSeekBar.progress = getSeekValue(mValue)
        }

    // but who really need this ...
    companion object {
        private const val APP_NS = "http://schemas.android.com/apk/res-auto"
        protected const val ANDROIDNS = "http://schemas.android.com/apk/res/android"
    }

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            com.eurekateam.samsungextras.R.styleable.CustomSeekBarPreference
        )
        try {
            mShowSign = a.getBoolean(
                com.eurekateam.samsungextras.R.styleable.CustomSeekBarPreference_showSign,
                mShowSign
            )
            val units =
                a.getString(com.eurekateam.samsungextras.R.styleable.CustomSeekBarPreference_units)
            if (units != null) mUnits = " $units"
            mContinuousUpdates = a.getBoolean(
                com.eurekateam.samsungextras.R.styleable.CustomSeekBarPreference_continuousUpdates,
                mContinuousUpdates
            )
        } finally {
            a.recycle()
        }
        try {
            val newInterval = attrs!!.getAttributeValue(APP_NS, "interval")
            if (newInterval != null) mInterval = newInterval.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Invalid interval value", e)
        }
        mMinValue = attrs!!.getAttributeIntValue(APP_NS, "min", mMinValue)
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", mMaxValue)
        if (mMaxValue < mMinValue) mMaxValue = mMinValue
        val defaultValue = attrs.getAttributeValue(ANDROIDNS, "defaultValue")
        mDefaultValueExists = defaultValue != null && defaultValue.isNotEmpty()
        if (mDefaultValueExists) {
            mDefaultValue = getLimitedValue(defaultValue!!.toInt())
            mValue = mDefaultValue
        } else {
            mValue = mMinValue
        }
        mSeekBar = SeekBar(context, attrs)
        layoutResource = com.eurekateam.samsungextras.R.layout.preference_custom_seekbar
    }
}
