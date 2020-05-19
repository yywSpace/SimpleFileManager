package com.yywspace.simplefilemanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_file_list_container.*

class FileListContainerFragment : Fragment() {
    private val TAG = "FileListContainerFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list_container, container, false)
    }

    @SuppressLint("SdCardPath")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)
        fragment = FileListFragment.newInstance("/sdcard")
        crumbView.setFragmentTransaction(childFragmentManager)
        crumbView.addFirstFixedCrumbItem(
            R.id.fragment_container,
            "sdcard",
            fragment
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

    companion object{
        private lateinit var fragment: Fragment

        fun getFirstFragment(): Fragment {
            return fragment
        }
    }

}
