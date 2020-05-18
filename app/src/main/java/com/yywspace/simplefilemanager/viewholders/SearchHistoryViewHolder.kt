package com.yywspace.simplefilemanager.viewholders

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yywspace.simplefilemanager.data.SearchHistory
import kotlinx.android.synthetic.main.item_file_search_history.view.*
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

class SearchHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(history: SearchHistory) {
        with(itemView) {
            searchContent.text = history.historyContent
            val time = SimpleDateFormat(
                "yy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(history.historyTime.time)
            searchTime.text = time
        }
    }
}