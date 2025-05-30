package ru.batir8888.beautymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String? = null,
    val lastVisit: Long? = System.currentTimeMillis(),
    val notes: String? = null,
    val portfolioPhotos: List<String> = emptyList() // Cloudinary photo data (URL|publicId)
)

/**
 * TypeConverters для Room Database
 * Конвертирует List<String> в JSON строку и обратно
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson(value, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}