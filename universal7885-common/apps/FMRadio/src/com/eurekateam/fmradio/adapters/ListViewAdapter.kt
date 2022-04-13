package com.eurekateam.fmradio.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.eurekateam.fmradio.*
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.FileUtilities
import com.eurekateam.fmradio.utils.Log
import com.google.android.material.textview.MaterialTextView

class ListViewAdapter(private val mContext: Context) : BaseAdapter() {
    private val mFMInterface = NativeFMInterface()
    private var mListofViews = HashMap<Int, View>(30)
    override fun getCount(): Int {
        return MainFragment.mTracks.size
    }

    override fun getItem(p0: Int): Any {
        return MainFragment.mTracks[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(id: Int, mConvertView: View?, parent: ViewGroup?): View {
        val mAnotherConvertView = mConvertView
            ?: (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.channel_list_items, parent, false
            )
        mAnotherConvertView.findViewById<MaterialTextView>(R.id.channel_list_title).text =
            String.format(
                mContext.getString(R.string.fm_radio_freq),
                MainFragment.mTracks[id].toFloat() / 1000
            )
        mAnotherConvertView.findViewById<MaterialTextView>(R.id.channel_list_title).setOnClickListener {
            mFMInterface.setFMFreq(MainFragment.fd, MainFragment.mTracks[id].toInt())
            MainFragment.mFreqCurrent = MainFragment.mTracks[id].toInt()
            FileUtilities.writeToFile(
                FileUtilities.mFMFreqFileName,
                MainFragment.mFreqCurrent.toString(), mContext
            )
            setCurrentFMChannel(id)
        }
        mAnotherConvertView.findViewById<AppCompatImageView>(R.id.star_button_list).let {
            val mStar = ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_star, mContext.theme)
            val mStarFilled = ResourcesCompat.getDrawable(
                mContext.resources,
                R.drawable.ic_star_filled, mContext.theme
            )
            val mIndex = MainFragment.mTracks[id].toInt()
            if (MainFragment.mFavStats[mIndex] == null) {
                MainFragment.mFavStats.putIfAbsent(mIndex, false)
            }
            if (MainFragment.mFavStats[mIndex]!!) {
                it.setImageDrawable(mStarFilled)
            } else {
                it.setImageDrawable(mStar)
            }
        }
        if (MainFragment.mFreqCurrent == MainFragment.mTracks[id].toInt()) {
            setCurrentFMChannel(id)
        }
        mListofViews[id] = mAnotherConvertView!!
        return mAnotherConvertView
    }

    private fun setCurrentFMChannel(mPosition: Int) {
        Log.i("Position: $mPosition")
        for (mItem in mListofViews) {
            mItem.value.setBackgroundColor(
                ResourcesCompat.getColor(
                    mContext.resources,
                    android.R.color.system_accent2_100, mContext.theme
                )
            )
        }
        mListofViews[mPosition]?.setBackgroundColor(
            ResourcesCompat.getColor(
                mContext.resources,
                android.R.color.system_accent3_400, mContext.theme
            )
        )
    }
}
