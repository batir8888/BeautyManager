package ru.batir8888.beautymanager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.batir8888.beautymanager.data.dao.AppointmentDao
import ru.batir8888.beautymanager.data.dao.ClientDao
import ru.batir8888.beautymanager.data.dao.DailyReportDao
import ru.batir8888.beautymanager.data.dao.ReportDao
import ru.batir8888.beautymanager.data.model.Appointment
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.data.model.DailyReport
import ru.batir8888.beautymanager.data.model.Report

@Database(
    entities = [
        Client::class,
        Appointment::class,
        DailyReport::class,
        Report::class],
    version = 6,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun reportDao(): ReportDao
}