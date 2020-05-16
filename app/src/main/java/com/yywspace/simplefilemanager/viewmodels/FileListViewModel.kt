package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.nio.file.Files
import java.nio.file.Path

class FileListViewModel(application: Application) : BasicSortViewModel(application) {

    override fun initData(path: Path) {
        super.initData(path)
        val list = initialPathList?.toMutableList()
        initFileList(list!!, null)
        pathListLiveData.value = list
    }

}