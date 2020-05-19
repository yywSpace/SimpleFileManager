package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import java.nio.file.Files
import java.nio.file.Path

class FileListViewModel(handle: SavedStateHandle, application: Application) :
    BasicSortViewModel(handle, application) {
    private val TAG = "FileListViewModel"
    override fun initData(path: Path, refresh: Boolean) {
        currentPath = path
        if (initialFileItemList == null || refresh) {
            super.initData(path, refresh)
            val list = initialFileItemList?.toMutableList()
            initFileList(list!!, null)
            Log.d(TAG, "initData: ${path}")
            pathListLiveData.value = list
            return
        }
    }

    companion object {
        var currentPath: Path? = null
        var isActionModeOn = false
    }
}