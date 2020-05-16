package com.yywspace.simplefilemanager.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.nio.file.Files
import java.nio.file.Path

open class BasicSortViewModel(application: Application) : AndroidViewModel(application) {
    var initialPathList: List<Path>? = null
    protected val pathListLiveData = MutableLiveData<List<Path>>()

    protected val sharedPreferences = application.getSharedPreferences(
        SETTING_PREF_NAME,
        Context.MODE_PRIVATE
    )!!

    fun getLiveData(): LiveData<List<Path>> {
        return pathListLiveData
    }

    open fun initData(path: Path) {
        initialPathList = Files.newDirectoryStream(path).toList()
    }

    fun sortByName(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value = pathListLiveData.value?.sortedByDescending { it.fileName }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { it.fileName }
        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "name")
            apply()
        }
    }

    fun sortByModifyTime(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value =
                pathListLiveData.value?.sortedByDescending { Files.getLastModifiedTime(it) }
        else
            pathListLiveData.value =
                pathListLiveData.value?.sortedBy { Files.getLastModifiedTime(it) }

        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "time")
            apply()
        }
    }

    fun sortBySize(reverseOrder: Boolean) {
        if (reverseOrder)
            pathListLiveData.value = pathListLiveData.value?.sortedByDescending { Files.size(it) }
        else
            pathListLiveData.value = pathListLiveData.value?.sortedBy { Files.size(it) }
        with(sharedPreferences.edit()) {
            putBoolean(SORT_REVERSE_ORDER, reverseOrder)
            putString(SORT_TYPE, "size")
            apply()
        }
    }

    fun changeHiddenFileStatus(hasHiddenFile: Boolean) {
        if (hasHiddenFile) {
            val list = initialPathList?.toMutableList()
            initFileList(list!!, hasHiddenFile)
            pathListLiveData.value = list
        } else {
            pathListLiveData.value = pathListLiveData.value
                ?.filter { !it.fileName.toString().startsWith(".") }
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

    protected fun initFileList(pathList: MutableList<Path>, hiddenFile: Boolean?) {
        val sortType = getCurrentSortType()
        val sortOrder = getCurrentSortOrder()
        var hasHiddenFile = hiddenFile
        if (hasHiddenFile == null) {
            hasHiddenFile = sharedPreferences.getBoolean(HIDDEN_FILE_STATUS, false)
        }
        if (!hasHiddenFile) {
            pathList.removeIf {
                it.fileName.toString().startsWith(".")
            }
        }
        if (sortOrder) {
            when (sortType) {
                "name" -> pathList.sortByDescending { it.fileName }
                "size" -> pathList.sortByDescending { Files.size(it) }
                "time" -> pathList.sortByDescending {
                    Files.getLastModifiedTime(
                        it
                    )
                }
            }
        } else {
            when (sortType) {
                "name" -> pathList.sortBy { it.fileName }
                "size" -> pathList.sortBy { Files.size(it) }
                "time" -> pathList.sortBy {
                    Files.getLastModifiedTime(
                        it
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