package ru.batir8888.beautymanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.batir8888.beautymanager.data.dao.AppointmentDao
import ru.batir8888.beautymanager.data.dao.ClientDao
import ru.batir8888.beautymanager.data.dao.DailyReportDao
import ru.batir8888.beautymanager.data.dao.ReportDao
import ru.batir8888.beautymanager.data.model.Appointment
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.data.model.Converters
import ru.batir8888.beautymanager.data.model.DailyReport
import ru.batir8888.beautymanager.data.model.Report

@Database(
    entities = [
        Client::class,
        Appointment::class,
        DailyReport::class,
        Report::class],
    version = 10,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun reportDao(): ReportDao
}