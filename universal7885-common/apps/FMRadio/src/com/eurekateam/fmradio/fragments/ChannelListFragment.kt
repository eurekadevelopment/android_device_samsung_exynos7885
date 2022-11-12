package com.eurekateam.fmradio.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.eurekateam.fmradio.NativeFMInterface
import com.eurekateam.fmradio.R
import com.eurekateam.fmradio.MainActivity
import com.eurekateam.fmradio.adapters.ListViewAdapter
import com.eurekateam.fmradio.utils.MaterialHelper
import com.eurekateam.fmradio.utils.IWaitUntil
import com.eurekateam.fmradio.utils.WaitUntil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import vendor.eureka.hardware.fmradio.SetType
import vendor.eureka.hardware.fmradio.GetType

class ChannelListFragment :
    Fragment(R.layout.activity_channel_list),
    View.OnClickListener {
    private lateinit var mListView: ListView
    private lateinit var mFloatingActionButton: FloatingActionButton
    private val mNativeIF = NativeFMInterface()

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
        mRootView.findViewById<FrameLayout>(R.id.channel_list_layout).setBackgroundColor(
                MaterialHelper.build {
                     context = requireContext()
                     light = android.R.color.system_accent1_50
                     dark = android.R.color.system_accent1_100
                }.getValue()
        )
        return mRootView
    }

    override fun onClick(v: View?) {
        mNativeIF.mDefaultCtl.setValue(SetType.SET_TYPE_FM_SEARCH_START, 0)
	val loading = ProgressDialog(requireContext())
	loading.setMessage(requireContext().resources.getString(R.string.update_freq))
	loading.setCancelable(false)
	loading.setInverseBackgroundForced(false)
	loading.show()
        WaitUntil.setTimer(
            requireActivity(),
            object : IWaitUntil {
                override fun cond(): Boolean = mNativeIF.mDefaultCtl.getValue(GetType.GET_TYPE_FM_MUTEX_LOCKED) == 0
                override fun todo() {
                    if (isAdded()) {
                        loading.hide()
                        (requireActivity() as MainActivity).getMySupportFragmentManager().apply {
                            beginTransaction().remove(this@ChannelListFragment).commit()
                            executePendingTransactions()
                            beginTransaction().add(R.id.container_view, this@ChannelListFragment).commit()
                        }
                    }
                }
            }
        )
    }
}
