package com.yywspace.simplefilemanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "file_search_histories")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val historyId: Int = 0,
    @ColumnInfo(name = "time")  var historyTime: Calendar = Calendar.getInstance(),
    @ColumnInfo(name = "content") var historyContent: String
)