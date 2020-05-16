package com.yywspace.simplefilemanager.data

import android.content.Context

class SearchHistoryRepository private constructor(var historyDao: SearchHistoryDao) {

    fun getHistories(limit: Int) = historyDao.getHistories(limit)

    suspend fun insertOrUpdate(history: SearchHistory) {
        val oldHistory = historyDao.getHistoryByContent(history.historyContent)
        if (oldHistory == null) {
            historyDao.insertHistory(history)
        } else {
            oldHistory.historyTime = history.historyTime
            historyDao.updateHistory(oldHistory)
        }
    }

    suspend fun deleteHistory(history: SearchHistory) = historyDao.deleteHistory(history)

    suspend fun clearHistories() = historyDao.deleteAll()

    companion object {
        @Volatile
        private var instance: SearchHistoryRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                val historyDao =
                    AppDatabase.getInstance(context.applicationContext).getSearchHistoryDao()
                instance ?: SearchHistoryRepository(historyDao).also {
                    instance = it
                }
            }
    }
}