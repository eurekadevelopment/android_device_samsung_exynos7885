package com.eurekateam.fmradio.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.eurekateam.fmradio.SharedPreferencesConst
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.utils.Log
import com.eurekateam.fmradio.R
import com.google.android.material.textview.MaterialTextView
import vendor.eureka.hardware.fmradio.GetType
import vendor.eureka.hardware.fmradio.SetType

class ListViewAdapter(private val mContext: Context) : BaseAdapter() {
    private val mFMInterface = NativeFMInterface()
    private val mListChannel = mFMInterface.mDefaultCtl.getFreqsList().apply{ sort() }
    private var mListofViews = HashMap<Int, View>(30)
    private var mInitDoneArray = Array(30){ false }

    override fun getCount(): Int {
        return mListChannel.size
    }

    override fun getItem(p: Int): Any {
        return mListChannel[p]
    }

    override fun getItemId(p: Int): Long {
        return p.toLong()
    }

    override fun getView(id: Int, mConvertView: View?, parent: ViewGroup?): View {
        val mAnotherConvertView = mConvertView
            ?: (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.channel_list_items,
                parent,
                false
            )
        mAnotherConvertView.findViewById<MaterialTextView>(R.id.channel_list_title).text =
            String.format(
                mContext.getString(R.string.fm_radio_freq),
                mListChannel[id].toFloat() / 1000
            )
        mAnotherConvertView.findViewById<MaterialTextView>(R.id.channel_list_title).setOnClickListener {
            mFMInterface.mDefaultCtl.setValue(SetType.SET_TYPE_FM_FREQ, mListChannel[id])
            setCurrentFMChannel(id)
        }
        mAnotherConvertView.findViewById<AppCompatImageView>(R.id.star_button_list).let {
            val mStar = ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_star, mContext.theme)
            val mStarFilled = ResourcesCompat.getDrawable(
                mContext.resources,
                R.drawable.ic_star_filled,
                mContext.theme
            )
            val mIndex = mListChannel[id]
            val mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext)
            val mFav = mSharedPref.getBoolean(SharedPreferencesConst.assembleFavFreq(mIndex), false)
            if (mFav) {
                it.setImageDrawable(mStarFilled)
            } else {
                it.setImageDrawable(mStar)
            }
        }
        if (!mInitDoneArray[id] && mFMInterface.mDefaultCtl.getValue(GetType.GET_TYPE_FM_FREQ) == mListChannel[id]) {
            setCurrentFMChannel(id)
        }
        mInitDoneArray[id] = true
        mListofViews[id] = mAnotherConvertView!!
        return mAnotherConvertView
    }

    private fun setCurrentFMChannel(mPosition: Int) {
        Log.i("Position: $mPosition")
        for (mItem in mListofViews) {
            mItem.value.setBackgroundColor(
                ResourcesCompat.getColor(
                    mContext.resources,
                    android.R.color.system_accent1_50,
                    mContext.theme
                )
            )
        }
        mListofViews[mPosition]?.setBackgroundColor(
            ResourcesCompat.getColor(
                mContext.resources,
                android.R.color.system_accent1_300,
                mContext.theme
            )
        )
    }
}
