package ru.batir8888.beautymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.batir8888.beautymanager.data.model.Report

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE monthStart = :monthStart ORDER BY id DESC")
    fun getByMonth(monthStart: Long): Flow<List<Report>>

    @Insert
    suspend fun insert(report: Report): Long
    @Update
    suspend fun update(report: Report)
    @Delete
    suspend fun delete(report: Report)
}
