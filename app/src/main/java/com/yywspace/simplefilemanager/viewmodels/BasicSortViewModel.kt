package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.yywspace.simplefilemanager.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

open class BasicSortViewModel(val handle: SavedStateHandle, application: Application) :
    AndroidViewModel(application) {
    private val TAG = "BasicSortViewModel"
    var initialFileItemList: List<FileItem>? = null
    protected val pathListLiveData = MutableLiveData<List<FileItem>>()

    protected val sharedPreferences = application.getSharedPreferences(
        SETTING_PREF_NAME,
        Context.MODE_PRIVATE
    )!!

    fun getLiveData(): LiveData<List<FileItem>> {
        return pathListLiveData
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

    fun deleteSelected() {
        val selectList = getSelectList()
        pathListLiveData.value = getUnSelectList()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                selectList?.filter { it.selected }?.forEach {
                    if (Files.isDirectory(it.path))
                        deleteDir(it.path)
                }
            }
        }

    }

    private fun deleteDir(path: Path) {
        if (Files.isDirectory(path)) {
            for (f in Files.newDirectoryStream(path).toList())
                deleteDir(f)
        }
        Files.deleteIfExists(path)
    }

    fun getSelectList(): List<FileItem>? {
        return pathListLiveData.value?.filter { it.selected }
    }

    fun getUnSelectList(): List<FileItem>? {
        return pathListLiveData.value?.filter { !it.selected }
    }

    fun select(index: Int) {
        Log.d(TAG, "select: $index")
        pathListLiveData.value = pathListLiveData.value?.apply {
            get(index).selected = true
        }
    }

    fun unSelect(index: Int) {
        pathListLiveData.value = pathListLiveData.value?.apply {
            get(index).selected = false
        }
    }


    open fun initData(path: Path) {
        if (initialFileItemList != null)
            return
        initialFileItemList = Files.newDirectoryStream(path).toList().map {
            FileItem(it, false)
        }
    }

    fun sortByName(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value = pathListLiveData.value?.sortedByDescending { it.path.fileName }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { it.path.fileName }
        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "name")
            apply()
        }
    }

    fun sortByModifyTime(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value =
                pathListLiveData.value?.sortedByDescending { Files.getLastModifiedTime(it.path) }
        else
            pathListLiveData.value =
                pathListLiveData.value?.sortedBy { Files.getLastModifiedTime(it.path) }

        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "time")
            apply()
        }
    }

    fun sortBySize(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value =
                pathListLiveData.value?.sortedByDescending { Files.size(it.path) }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { Files.size(it.path) }
        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "size")
            apply()
        }
    }

    fun changeHiddenFileStatus(hasHiddenFile: Boolean) {
        if (hasHiddenFile) {
            val list = initialFileItemList?.toMutableList()
            initFileList(list!!, hasHiddenFile)
            pathListLiveData.value = list
        } else {
            pathListLiveData.value = pathListLiveData.value
                ?.filter { !it.path.fileName.toString().startsWith(".") }
        }
        with(sharedPreferences.edit()) {
            putBoolean(HIDDEN_FILE_STATUS, hasHiddenFile)
            apply()
        }
    }


    fun getCurrentSortType(): String? {
        return sharedPreferences.getString(SORT_TYPE, "name")
    }

    fun getCurrentSortOrder(): Boolean {
        return sharedPreferences.getBoolean(SORT_REVERSE_ORDER, false)
    }

    fun isHasHiddenFile(): Boolean {
        return sharedPreferences.getBoolean(HIDDEN_FILE_STATUS, false)
    }

    protected fun initFileList(pathList: MutableList<FileItem>, hiddenFile: Boolean?) {
        val sortType = getCurrentSortType()
        val sortOrder = getCurrentSortOrder()
        var hasHiddenFile = hiddenFile
        if (hasHiddenFile == null) {
            hasHiddenFile = sharedPreferences.getBoolean(HIDDEN_FILE_STATUS, false)
        }
        if (!hasHiddenFile) {
            pathList.removeIf {
                it.path.fileName.toString().startsWith(".")
            }
        }
        if (sortOrder) {
            when (sortType) {
                "name" -> pathList.sortByDescending { it.path.fileName }
                "size" -> pathList.sortByDescending { Files.size(it.path) }
                "time" -> pathList.sortByDescending {
                    Files.getLastModifiedTime(
                        it.path
                    )
                }
            }
        } else {
            when (sortType) {
                "name" -> pathList.sortBy { it.path.fileName }
                "size" -> pathList.sortBy { Files.size(it.path) }
                "time" -> pathList.sortBy {
                    Files.getLastModifiedTime(
                        it.path
                    )
                }
            }
        }
    }

    companion object {
        const val SORT_TYPE = "SORT_TYPE"
        const val HIDDEN_FILE_STATUS = "HIDDEN_FILE_STATUS"
        const val SORT_REVERSE_ORDER = "SORT_REVERSE_ORDER"
        const val SETTING_PREF_NAME = "SETTING_PREF_NAME"
    }
}