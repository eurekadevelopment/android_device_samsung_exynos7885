package com.eurekateam.fmradio.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.utils.MaterialHelper
import com.eurekateam.fmradio.adapters.PebbleLayoutAdapter

class FavoriteFragment : Fragment(R.layout.fragment_fav_list) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.fragment_fav_list, container, false)
        val mGridView = mRootView.findViewById<GridView>(R.id.fav_list_grid)
        mGridView.adapter = PebbleLayoutAdapter(requireContext())
        mRootView.findViewById<GridView>(R.id.fav_list_grid).setBackgroundColor(
                 MaterialHelper.build {
                     context = requireContext()
                     light = android.R.color.system_accent1_100
                     dark = android.R.color.system_accent1_50
                }.getValue()
        )
        return mRootView
    }
}
