package com.eurekateam.fmradio.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.adapters.PebbleLayoutAdapter

class FavouriteFragment : Fragment(R.layout.fragment_fav_list) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.fragment_fav_list, container, false)
        val mGridView = mRootView.findViewById<GridView>(R.id.fav_list_grid)
        mGridView.adapter = PebbleLayoutAdapter(requireContext())
        var mIsLight = true
        val nightModeFlags = requireContext().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> mIsLight = false
            Configuration.UI_MODE_NIGHT_NO -> mIsLight = true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mIsLight = true
        }
        mRootView.findViewById<GridView>(R.id.fav_list_grid).apply {
            if (mIsLight)
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_50, requireContext().theme))
            else
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_100, requireContext().theme))
        }
        return mRootView
    }
}
