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
                    add(FileItem(path, false))
                }
            initialFileItemList?.add(FileItem(path, false))
            return true
        }
    }

    fun deleteSelected(onDelete: (() -> Unit)?) {
        val selectList = getSelectList()
        Log.d(TAG, "deleteSelected11: ${selectList}")
        Log.d(TAG, "deleteSelected22: ${selectList}")
        Log.d(TAG, "deleteSelected33: ${selectList}")
        Log.d(TAG, "deleteSelected44: ${selectList}")
        selectList?.forEach {
            deleteDir(it.path)
        }
        onDelete?.invoke()
    }

    private fun copyFolder(src: Path, dest: Path) {
        if (Files.isDirectory(src)) {
            Log.d(TAG, "src: ${src}")
            Log.d(TAG, "dest: ${dest}")
            if (!Files.exists(dest)) {
                Files.createDirectory(dest)
            }
            Files.newDirectoryStream(src).toList().forEach {
                val srcFile = Paths.get(
                    src.toAbsolutePath().toString() + File.separator + it.fileName.toString()
                )
                val destFile = Paths.get(
                    dest.toAbsolutePath().toString() + File.separator + it.fileName.toString()
                )
                // 递归复制
                copyFolder(srcFile, destFile)
            }
        } else {
            Log.d(TAG, "copyFolder: ${dest}")
            Files.copy(
                src,
                dest
            )
        }
    }

    fun copySelectedTo(path: Path, onCopy: (() -> Unit)?) {
        Log.d(TAG, "cutSelectedTo: ${pathListLiveData.value}")
        val selectList = getSelectList()
        selectList?.forEach {
            Log.d(TAG, "getSelectList forEach: ${it}")
            val newFile = Paths.get(
                path.toAbsolutePath()
                    .toString() + File.separator + it.path.fileName.toString()
            )
            copyFolder(it.path, newFile)
        }
        onCopy?.invoke()
    }

    fun cutSelectedTo(path: Path, onCut: (() -> Unit)?) {
        val selectList = getSelectList()
        selectList?.forEach {
            val newFile = Paths.get(
                path.toAbsolutePath()
                    .toString() + File.separator + it.path.fileName.toString()
            )
            copyFolder(it.path, newFile)
        }
        selectList?.forEach {
            deleteDir(it.path)
        }
        onCut?.invoke()
    }

    private fun deleteDir(path: Path) {
        Log.d(TAG, "deleteDir: ${path}")
        Log.d(TAG, "exists: ${Files.exists(path)}")
        if (Files.isDirectory(path)) {
            for (f in Files.newDirectoryStream(path).toList())
                deleteDir(f)
            Files.delete(path)
        } else {

            Files.delete(path)
        }
    }

    fun getSelectList(): List<FileItem>? {
        Log.d(TAG, "getSelectList: ${pathListLiveData.value}")
        return pathListLiveData.value?.filter {
            it.selected
        }
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


    open fun initData(path: Path, refresh: Boolean = false) {
        this.path = path
        initialFileItemList = Files.newDirectoryStream(path).toList().map {
            FileItem(it, false)
        }.toMutableList()
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