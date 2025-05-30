package ru.batir8888.beautymanager.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionsHelper {
    private const val TAG = "PermissionsHelper"

    /**
     * Проверяет, есть ли разрешение на чтение изображений
     */
    fun hasImagePermission(context: Context): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Проверка разрешения $permission: $hasPermission")

        return hasPermission
    }

    /**
     * Возвращает нужное разрешение в зависимости от версии Android
     */
    fun getRequiredImagePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Проверяет все разрешения приложения
     */
    fun checkAllPermissions(context: Context) {
        Log.d(TAG, "=== ПРОВЕРКА РАЗРЕШЕНИЙ ===")
        Log.d(TAG, "Android версия: ${Build.VERSION.SDK_INT}")

        val permissions = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )

        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "$permission: ${if (granted) "✅ ЕСТЬ" else "❌ НЕТ"}")
        }

        Log.d(TAG, "========================")
    }
}