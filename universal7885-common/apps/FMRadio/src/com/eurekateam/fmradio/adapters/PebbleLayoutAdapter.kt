package com.eurekateam.fmradio.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.res.ResourcesCompat
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.PebbleTextView
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.fragments.MainFragment
import com.eurekateam.fmradio.utils.FileUtilities

class PebbleLayoutAdapter(private val mContext: Context) : BaseAdapter() {
    private val mFavoriteList: MutableList<Int> = emptyList<Int>().toMutableList()
    init {
        for (mItem in MainFragment.mFavStats) {
            if (mItem.value) {
                mFavoriteList.add(mItem.key)
            }
        }
        mFavoriteList.sort()
        var mData = ""
        for (i in mFavoriteList) {
            mData += "$i\n"
        }
        FileUtilities.writeToFile(FileUtilities.mFavouriteChannelFileName, mData, mContext)
    }
    override fun getCount(): Int {
        return mFavoriteList.size
    }

    override fun getItem(p0: Int): Any {
        return mFavoriteList[p0]
    }

    private val mFMInterface = NativeFMInterface()
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(id: Int, mConvertView: View?, parent: ViewGroup?): View {
        val mAnotherConvertView = mConvertView
            ?: (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.favorite_channel_items, parent, false
            )
        mAnotherConvertView.findViewById<PebbleTextView>(R.id.pebble_textview).apply {
            mText = (mFavoriteList[id].toFloat() / 1000).toString()
            mColor = ResourcesCompat.getColor(
                mContext.resources, android.R.color.system_accent1_400,
                mContext.theme
            )
            setOnClickListener {
                mFMInterface.setFMFreq(MainFragment.fd, mFavoriteList[id])
                MainFragment.mFreqCurrent = mFavoriteList[id]
                FileUtilities.writeToFile(
                    FileUtilities.mFMFreqFileName,
                    MainFragment.mFreqCurrent.toString(), mContext
                )
            }
        }
        return mAnotherConvertView
    }
}
