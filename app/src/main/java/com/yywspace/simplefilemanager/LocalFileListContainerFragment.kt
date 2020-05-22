package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yywspace.simplefilemanager.viewmodels.FileSourceType
import com.yywspace.simplefilemanager.viewmodels.GlobalViewModel
import kotlinx.android.synthetic.main.fragment_local_file_list_container.*

class LocalFileListContainerFragment : Fragment() {
    private val TAG = "FileListContainerFragment"
    private val globalViewModel = GlobalViewModel.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        globalViewModel.currentSourceType = FileSourceType.LOCAL
        Log.d(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_local_file_list_container, container, false)
    }

    @SuppressLint("SdCardPath")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)
        globalViewModel.currentFragment = FileListFragment.newInstance("/sdcard")
        crumbView.setFragmentTransaction(childFragmentManager)
        crumbView.addFirstFixedCrumbItem(
            R.id.fragment_container,
            "sdcard",
            globalViewModel.currentFragment!!
        )
    }

    fun addCrumbItem(title: String, fragment: Fragment, tag: String) {
        crumbView.addCrumbItem(
            R.id.fragment_container,
            title,
            fragment,
            tag
        )
    }
}
