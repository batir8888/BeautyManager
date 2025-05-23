package ru.batir8888.beautymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.batir8888.beautymanager.data.ReportType

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monthStart: Long,            // millis на 1-е число месяца 00:00
    val type: ReportType,
    val label: String,               // «Аренда», «Накладные» …
    val amount: Long                 // в копейках
)
