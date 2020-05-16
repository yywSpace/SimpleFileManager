package com.yywspace.simplefilemanager.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.data.SearchHistory
import kotlinx.android.synthetic.main.item_file_search_history.view.*
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

class SearchHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var onItemClickListener: ((SearchHistory) -> Unit)? = null
    var onDeleteButtonClickListener: ((SearchHistory, View) -> Unit)? = null

    fun bind(history: SearchHistory) {
        with(itemView) {
            setOnClickListener {
                onItemClickListener?.invoke(history)
            }
            deleteHistory.setOnClickListener {
                onDeleteButtonClickListener?.invoke(history, it)
            }
            searchContent.text = history.historyContent
            // 插入搜索历史    // 搜索时切换adapter
            val time = SimpleDateFormat(
                "yy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(history.historyTime.time)
            searchTime.text = time
        }
    }
}