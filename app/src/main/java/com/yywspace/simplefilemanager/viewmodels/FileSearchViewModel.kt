package com.yywspace.simplefilemanager.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yywspace.simplefilemanager.data.FileItem
import com.yywspace.simplefilemanager.data.SearchHistory
import com.yywspace.simplefilemanager.data.SearchHistoryRepository
import kotlinx.coroutines.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.ConcurrentLinkedQueue

class FileSearchViewModel(handle: SavedStateHandle, application: Application) :
    BasicSortViewModel(handle, application) {
    private val TAG = "FileSearchViewModel"
    private var searchJob: Job? = null
    private val historyRepository = SearchHistoryRepository.getInstance(application)
    var searchStatus = MutableLiveData<SearchStatus>()

    fun deleteHistory(history: SearchHistory) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyRepository.deleteHistory(history)
            }
        }
    }

    fun insertOrUpdate(history: SearchHistory) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyRepository.insertOrUpdate(history)
            }
        }
    }

    fun setSearchMode(searchMode: SearchMode) {
        sharedPreferences.edit().apply {
            putInt(SEARCH_MODE, searchMode.ordinal)
            apply()
        }
    }

    fun getSearchMode(): SearchMode {
        return when (sharedPreferences.getInt(SEARCH_MODE, 0)) {
            0 -> SearchMode.LOCAL
            else -> SearchMode.GLOBAL
        }
    }

    fun isRecursiveSearch(): Boolean {
        return sharedPreferences.getBoolean(RECURSIVE_SEARCH, false)
    }

    fun setRecursiveSearch(isRecursive: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(RECURSIVE_SEARCH, isRecursive)
            apply()
        }
    }

    fun hasUnknownFile(): Boolean {
        return sharedPreferences.getBoolean(UNKNOWN_FILE, false)
    }

    fun setUnknownFile(hasUnknownFile: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(UNKNOWN_FILE, hasUnknownFile)
            apply()
        }
    }

    fun getHistory(): LiveData<List<SearchHistory>> {
        return historyRepository.getHistories(5)
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyRepository.clearHistories()
            }
        }
    }

    override fun initData(path: Path, refresh: Boolean) {
        super.initData(path,refresh)
        this.path = path
    }

    fun searchInCurrentPath(query: String) {
        searchStatus.value = SearchStatus.SEARCHING
        if (isRecursiveSearch()) {
            Log.d(TAG, "searchInCurrentPath: ${isRecursiveSearch()}")
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                var list: MutableList<FileItem>? = null
                withContext(Dispatchers.IO) {
                    list = searchRecursive(path!!, query)
                }
                pathListLiveData.value = list
                searchStatus.value = SearchStatus.FINISH
                Log.d(TAG, "isRecursiveSearch: ${list?.size}")
            }
        } else {
            val searchedList =
                initialFileItemList?.filter {
                    it.name.contains(query)
                }
            Log.d(TAG, "searchInCurrentPath: ${searchedList?.size}")
            pathListLiveData.value = searchedList?.toList()
            searchStatus.value = SearchStatus.FINISH
        }
    }

    @SuppressLint("SdCardPath")
    fun searchInRootPath(query: String) {
        searchStatus.value = SearchStatus.SEARCHING
        if (isRecursiveSearch()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                var list: MutableList<FileItem>? = null
                withContext(Dispatchers.IO) {
                    list = searchRecursive(Paths.get("/sdcard"), query)
                }
                pathListLiveData.value = list
                searchStatus.value = SearchStatus.FINISH
            }
        } else {
            val searchedList =
                Files.newDirectoryStream(Paths.get("/sdcard")).toList().filter {
                    it.fileName.toString().contains(query)
                }
            pathListLiveData.value = searchedList.toList().map { FileItem.fromLocalFile(it) }
            searchStatus.value = SearchStatus.FINISH
        }
    }

    private fun searchRecursive(path: Path, query: String): MutableList<FileItem> {
        val searchedList: ConcurrentLinkedQueue<FileItem> = ConcurrentLinkedQueue()
        Files.newDirectoryStream(path).toList().parallelStream().forEach {
            Files.walkFileTree(it, object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path?,
                    attrs: BasicFileAttributes?
                ): FileVisitResult {
                    file?.apply {
                        if (fileName.toString().contains(query)) {
                            // 如果不包含未知文件时，判断此文件的拓展名
                            if (!hasUnknownFile())
                                if (toFile().extension == "")
                                    return FileVisitResult.CONTINUE
                            searchedList.add(FileItem.fromLocalFile(this))
                            if (searchedList.size % MAX_LIST_UPDATE_SIZE == 0)
                                pathListLiveData.postValue(searchedList.toList())
                            if (searchedList.size >= MAX_FILE_SIZE)
                                return FileVisitResult.TERMINATE
                        }
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun preVisitDirectory(
                    dir: Path?,
                    attrs: BasicFileAttributes?
                ): FileVisitResult {
                    dir?.apply {
                        if (fileName.toString().contains(query)) {
                            // Log.d(TAG, "dir: $fileName")
                            searchedList.add(FileItem.fromLocalFile(this))
                            if (searchedList.size % MAX_LIST_UPDATE_SIZE == 0)
                                pathListLiveData.postValue(searchedList.toList())
                        }
                        if (searchedList.size >= MAX_FILE_SIZE)
                            return FileVisitResult.TERMINATE
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        }
        return searchedList.toMutableList()
    }

    companion object {
        const val SEARCH_MODE = "SEARCH_MODE"
        const val RECURSIVE_SEARCH = "RECURSIVE_SEARCH"
        const val UNKNOWN_FILE = "UNKNOWN_FILE"
        const val MAX_FILE_SIZE = 500
        const val MAX_LIST_UPDATE_SIZE = 50
    }
}

enum class SearchMode {
    LOCAL,
    GLOBAL
}

enum class SearchStatus {
    SEARCHING,
    FINISH
}
