package ru.batir8888.beautymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val dateStart: Long,
    val dateEnd: Long,
    val income: Long? = null,
    val expense: Long? = null,
    val note: String? = null
)
