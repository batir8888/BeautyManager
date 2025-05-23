package ru.batir8888.beautymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reports")
data class DailyReport(
    @PrimaryKey val date: Long,
    val isClosed: Boolean = false,
    val totalIncome: Long? = null,
    val totalExpense: Long? = null
)
