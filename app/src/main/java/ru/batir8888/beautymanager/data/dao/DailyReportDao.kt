package ru.batir8888.beautymanager.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.batir8888.beautymanager.data.model.DailyReport

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports WHERE date = :date")
    fun getReport(date: Long): Flow<DailyReport?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: DailyReport)
}