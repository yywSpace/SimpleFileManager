package com.yywspace.simplefilemanager.viewmodels

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.yywspace.simplefilemanager.FileListFragment
import com.yywspace.simplefilemanager.data.FileItem
import com.yywspace.simplefilemanager.utils.LocalFileUtil


class GlobalViewModel : ViewModel() {
    var isActionModeOn = false
    var currentSourceType: FileSourceType? = null
        set(value) {
            Log.d("TAG", "currentSourceType: $value")
            field = value
        }
    var currentPath: String? = null
        set(value) {
            Log.d("TAG", "currentPath: $value")
            field = value
        }
    val currentSelectList: MutableList<FileItem> = mutableListOf()
    var currentFragment: Fragment? = null
    fun deleteSelected(onDelete: (() -> Unit)?) {
        when (currentSourceType) {
            FileSourceType.LOCAL -> LocalFileUtil.delete(currentSelectList)
            FileSourceType.SAMBA -> LocalFileUtil.delete(currentSelectList)
        }
        onDelete?.invoke()
    }


    fun copySelectedTo(onCopy: (() -> Unit)?) {
        when (currentSourceType) {
            FileSourceType.LOCAL -> {
                Log.d("TAG", "copy: $currentSelectList \n $currentPath")
                LocalFileUtil.copy(currentSelectList, currentPath!!)
            }
            FileSourceType.SAMBA -> LocalFileUtil.copy(currentSelectList, currentPath!!)
        }
        onCopy?.invoke()
    }

    fun cutSelectedTo(onCut: (() -> Unit)?) {
        when (currentSourceType) {
            FileSourceType.LOCAL -> LocalFileUtil.cut(currentSelectList, currentPath!!)
            FileSourceType.SAMBA -> LocalFileUtil.cut(currentSelectList, currentPath!!)
        }
        onCut?.invoke()
    }


    companion object {
        @Volatile
        private var instance: GlobalViewModel? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: GlobalViewModel().also {
                    instance = it
                }
            }
    }

}

enum class FileSourceType {
    LOCAL,
    SAMBA
}