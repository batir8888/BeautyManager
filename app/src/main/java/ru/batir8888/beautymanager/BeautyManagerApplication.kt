package ru.batir8888.beautymanager

import android.app.Application
import androidx.room.Room
import ru.batir8888.beautymanager.data.AppDatabase

class BeautyManagerApplication : Application() {
    companion object {
        lateinit var database : AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "beauty_manager_db")
            .fallbackToDestructiveMigration()   // при отсутствии ручных миграций
            .build()
    }
}