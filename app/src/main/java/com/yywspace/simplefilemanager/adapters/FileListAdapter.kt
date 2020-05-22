package com.yywspace.simplefilemanager.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.R
import com.yywspace.simplefilemanager.data.FileItem
import com.yywspace.simplefilemanager.viewholders.EmptyViewHolder
import com.yywspace.simplefilemanager.viewholders.FileViewHolder

class FileListAdapter : ListAdapter<FileItem, RecyclerView.ViewHolder>(FileItemDiffCallback()) {
    private val TAG = "FileListAdapter"
    var isMultiSelect = false
    var onItemClickListener: ((FileItem, Int) -> Unit)? = null
    var onItemLongClickListener: ((View, Int) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        return when (viewType) {
            NORMAL_ITEM ->
                FileViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_file_list, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClickListener?.invoke(currentList[adapterPosition],adapterPosition)
                    }
                    itemView.setOnLongClickListener {
                        onItemLongClickListener?.invoke(it, adapterPosition)!!
                    }
                }
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
                holder.bind(currentList[position], isMultiSelect)
            }
        }
    }

    companion object {
        const val EMPTY_ITEM = 0
        const val NORMAL_ITEM = 1
    }
}

private class FileItemDiffCallback : DiffUtil.ItemCallback<FileItem>() {
    override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem == newItem
    }
}

