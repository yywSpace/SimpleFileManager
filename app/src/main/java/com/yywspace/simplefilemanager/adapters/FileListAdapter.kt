package com.yywspace.simplefilemanager.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.R
import com.yywspace.simplefilemanager.viewholders.EmptyViewHolder
import com.yywspace.simplefilemanager.viewholders.FileViewHolder
import java.nio.file.Path

class FileListAdapter : ListAdapter<Path, RecyclerView.ViewHolder>(FileItemDiffCallback()) {
    private val TAG = "FileListAdapter"
    var onItemClickListener: ((Path) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        return when (viewType) {
            NORMAL_ITEM ->
                FileViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_file_list, parent, false)
                )
            // empty holder
            else ->
                EmptyViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_file_list_empty, parent, false)
                )
        }

    }

    override fun getItemCount(): Int {
        if (currentList.size == 0)
            return 1
        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (currentList.size == 0)
            return EMPTY_ITEM
        return NORMAL_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
        when (holder) {
            is FileViewHolder -> {
                holder.bind(currentList[position])
                holder.onItemClickListener = onItemClickListener
            }
        }
    }

    companion object {
        const val EMPTY_ITEM = 0
        const val NORMAL_ITEM = 1
    }
}

private class FileItemDiffCallback : DiffUtil.ItemCallback<Path>() {
    override fun areItemsTheSame(oldItem: Path, newItem: Path): Boolean {
        return oldItem.fileName == newItem.fileName
    }

    override fun areContentsTheSame(oldItem: Path, newItem: Path): Boolean {
        return oldItem == newItem
    }
}

