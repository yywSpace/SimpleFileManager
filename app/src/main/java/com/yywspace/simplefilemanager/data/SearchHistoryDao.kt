package com.yywspace.simplefilemanager.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM file_search_histories order by time desc limit :limit")
    fun getHistories(limit: Int): LiveData<List<SearchHistory>>

    @Insert
    suspend fun insertHistory(history: SearchHistory)

    @Query("DELETE FROM file_search_histories")
    suspend fun deleteAll()

    @Update
    suspend fun updateHistory(history: SearchHistory)

    @Delete
    suspend fun deleteHistory(history: SearchHistory)

    @Query("SELECT * FROM file_search_histories where content = :content")
    suspend fun getHistoryByContent(content: String): SearchHistory?
}