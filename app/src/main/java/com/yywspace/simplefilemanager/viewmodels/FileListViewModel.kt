package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import java.nio.file.Files
import java.nio.file.Path

class FileListViewModel(handle: SavedStateHandle, application: Application) :
    BasicSortViewModel(handle, application) {
    var currentPath: Path? = null
    private val TAG = "FileListViewModel"
    override fun initData(path: Path) {
        super.initData(path)
        currentPath = path
        val list = initialFileItemList?.toMutableList()
        initFileList(list!!, null)
        Log.d(TAG, "initData: ${path}")
        pathListLiveData.value = list
    }
}