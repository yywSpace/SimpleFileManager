package com.yywspace.simplefilemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.R
import com.yywspace.simplefilemanager.data.SearchHistory
import com.yywspace.simplefilemanager.viewholders.EmptyViewHolder
import com.yywspace.simplefilemanager.viewholders.SearchHistoryViewHolder
import kotlinx.android.synthetic.main.item_file_search_history_clear.view.*


class SearchHistoryAdapter :
    ListAdapter<SearchHistory, RecyclerView.ViewHolder>(SearchHistoryDiffCallback()) {
    var onItemClickListener: ((SearchHistory) -> Unit)? = null
    var onClearButtonClickListener: (() -> Unit)? = null
    var onDeleteButtonClickListener: ((SearchHistory, View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NORMAL_ITEM ->
                SearchHistoryViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_file_search_history, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onItemClickListener?.invoke(currentList[adapterPosition])
                    }
                }
            DELETE_ITEM -> ClearHistoryViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file_search_history_clear, parent, false)
            )
            // empty holder
            else ->
                EmptyViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_history_empty, parent, false)
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchHistoryViewHolder -> {
                holder.bind(currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        if (currentList.size == 0)
            return 1
        return currentList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (currentList.size == 0)
            return EMPTY_ITEM
        if (position == currentList.size)
            return DELETE_ITEM
        return NORMAL_ITEM
    }

    companion object {
        const val EMPTY_ITEM = 0
        const val NORMAL_ITEM = 1
        const val DELETE_ITEM = 2
    }

    inner class ClearHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            with(itemView) {
                clearHistoriesButton.setOnClickListener {
                    onClearButtonClickListener?.invoke()
                }
            }
        }
    }
}

private class SearchHistoryDiffCallback : DiffUtil.ItemCallback<SearchHistory>() {
    override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
        return oldItem.historyId == newItem.historyId
    }

    override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
        return oldItem == newItem
    }
}