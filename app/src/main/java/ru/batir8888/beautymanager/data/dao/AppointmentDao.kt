package ru.batir8888.beautymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.batir8888.beautymanager.data.model.Appointment

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE dateStart BETWEEN :from AND :to ORDER BY dateStart")
    fun between(from: Long, to: Long): Flow<List<Appointment>>

    @Insert suspend fun insert(a: Appointment): Long
    @Update suspend fun update(a: Appointment)
    @Delete suspend fun delete(a: Appointment)

    @Query("SELECT EXISTS(SELECT 1 FROM appointments WHERE dateStart < :endTime AND dateEnd > :startTime AND dateStart >= :dayStart AND dateStart <= :dayEnd AND id != :excludeId)")
    suspend fun hasConflicts(startTime: Long, endTime: Long, dayStart: Long, dayEnd: Long, excludeId: Int): Boolean
}
