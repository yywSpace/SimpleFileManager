package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_file_list_container.*

class FileListContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list_container, container, false)
    }

    @SuppressLint("SdCardPath")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crumbView.setFragmentTransaction(childFragmentManager)
        crumbView.addFirstFixedCrumbItem(
            R.id.fragment_container,
            "sdcard",
            FileListFragment.newInstance("/sdcard")
        )
    }

    fun addCrumbItem(title: String, fragment: Fragment) {
        crumbView.addCrumbItem(
            R.id.fragment_container,
            title,
            fragment
        )
    }
}
