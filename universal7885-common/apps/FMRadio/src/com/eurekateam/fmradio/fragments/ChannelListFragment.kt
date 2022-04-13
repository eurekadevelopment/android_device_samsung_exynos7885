package com.eurekateam.fmradio.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.eurekateam.fmradio.MainActivity
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.adapters.ListViewAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelListFragment :
    Fragment(R.layout.activity_channel_list),
    View.OnClickListener {
    private lateinit var mListView: ListView
    private lateinit var mFloatingActionButton: FloatingActionButton
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.activity_channel_list, container, false)
        mListView = mRootView.findViewById(R.id.listview)
        mFloatingActionButton = mRootView.findViewById(R.id.refresh_channel_list)
        mFloatingActionButton.setOnClickListener(this)
        mListView.adapter = ListViewAdapter(requireContext())
        var mIsLight = true
        val nightModeFlags = requireContext().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> mIsLight = false
            Configuration.UI_MODE_NIGHT_NO -> mIsLight = true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> mIsLight = true
        }
        mRootView.findViewById<FrameLayout>(R.id.channel_list_layout).apply {
            if (mIsLight)
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_50, requireContext().theme))
            else
                setBackgroundColor(resources.getColor(android.R.color.system_accent1_100, requireContext().theme))
        }
        return mRootView
    }

    override fun onClick(v: View?) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                MainFragment.mRefreshTracks()
            }
            withContext(Dispatchers.Main) {
                (requireActivity() as MainActivity).getMySupportFragmentManager().apply {
                    beginTransaction().remove(this@ChannelListFragment).commit()
                    executePendingTransactions()
                    beginTransaction().add(R.id.container_view, this@ChannelListFragment).commit()
                }
            }
        }
    }
}
