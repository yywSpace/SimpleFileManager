package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.yywspace.simplefilemanager.data.FileItem
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileListViewModel(handle: SavedStateHandle, application: Application) :
    BasicSortViewModel(handle, application) {
    private val TAG = "FileListViewModel"
    override fun initData(path: Path, refresh: Boolean) {
        if (initialFileItemList == null || refresh) {
            super.initData(path, refresh)
            val list = initialFileItemList?.toMutableList()
            initFileList(list!!, null)
            Log.d(TAG, "initData: ${path}")
            pathListLiveData.value = list
            return
        }
    }

    fun selectAll() {
        pathListLiveData.value = pathListLiveData.value?.apply { forEach { it.selected = true } }
    }

    fun unSelectAll() {
        pathListLiveData.value = pathListLiveData.value?.apply { forEach { it.selected = false } }
    }

    fun isSelected(index: Int): Boolean? {
        return pathListLiveData.value?.get(index)?.selected
    }

    fun reverseSelect() {
        pathListLiveData.value =
            pathListLiveData.value?.apply { forEach { it.selected = !it.selected } }
    }

    fun createFolder(newFile: Path): Boolean {
        if (Files.exists(newFile)) {
            return false
        } else {
            val path = Files.createDirectory(newFile)
            pathListLiveData.value =
                pathListLiveData.value?.toMutableList()?.apply {
                    add(FileItem.fromLocalFile(path))
                }
            initialFileItemList?.add(FileItem.fromLocalFile(path))
            return true
        }
    }

    fun select(index: Int): FileItem? {
        var fileItem: FileItem? = null
        Log.d(TAG, "select: $index")
        pathListLiveData.value = pathListLiveData.value?.apply {
            fileItem = get(index)
            get(index).selected = true
        }
        return fileItem
    }

    fun unSelect(index: Int): FileItem? {
        var fileItem: FileItem? = null
        pathListLiveData.value = pathListLiveData.value?.apply {
            fileItem = get(index)
            get(index).selected = false
        }
        return fileItem
    }
}