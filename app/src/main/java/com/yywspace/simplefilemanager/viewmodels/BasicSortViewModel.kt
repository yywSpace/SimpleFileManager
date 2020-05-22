package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.yywspace.simplefilemanager.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

open class BasicSortViewModel(val handle: SavedStateHandle, application: Application) :
    AndroidViewModel(application) {
    private val TAG = "BasicSortViewModel"
    var path: Path? = null
    var initialFileItemList: MutableList<FileItem>? = null
    protected val pathListLiveData = MutableLiveData<List<FileItem>>()

    protected val sharedPreferences = application.getSharedPreferences(
        SETTING_PREF_NAME,
        Context.MODE_PRIVATE
    )!!

    fun getLiveData(): LiveData<List<FileItem>> {
        return pathListLiveData
    }


    open fun initData(path: Path, refresh: Boolean = false) {
        this.path = path
        initialFileItemList = Files.newDirectoryStream(path).toList().map {
            FileItem.fromLocalFile(it)
        }.toMutableList()
    }

    fun sortByName(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value = pathListLiveData.value?.sortedByDescending { it.name }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { it.name }
        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "name")
            apply()
        }
    }

    fun sortByModifyTime(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value =
                pathListLiveData.value?.sortedByDescending { it.lastModified }
        else
            pathListLiveData.value =
                pathListLiveData.value?.sortedBy { it.lastModified }

        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "time")
            apply()
        }
    }

    fun sortBySize(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value =
                pathListLiveData.value?.sortedByDescending { it.length }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { it.length }
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
                ?.filter { !it.name.startsWith(".") }
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
                it.name.startsWith(".")
            }
        }
        if (sortOrder) {
            when (sortType) {
                "name" -> pathList.sortByDescending { it.name }
                "size" -> pathList.sortByDescending { it.length }
                "time" -> pathList.sortByDescending { it.lastModified }
            }
        } else {
            when (sortType) {
                "name" -> pathList.sortBy { it.name }
                "size" -> pathList.sortBy { it.length }
                "time" -> pathList.sortBy { it.lastModified }
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