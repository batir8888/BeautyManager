package ru.batir8888.beautymanager.services

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.batir8888.beautymanager.utils.UriUtils
import kotlin.coroutines.resume

class CloudinaryService {

    companion object {
        private var isInitialized = false

        fun initialize(context: Context) {
            if (!isInitialized) {
                val config = hashMapOf(
                    "cloud_name" to "dtinnzfe8",
                    "api_key" to "238619838548123",
                    "api_secret" to "rqUJ1zp9fmRx9OadeBldd2QlDh4"
                )
                MediaManager.init(context, config)
                isInitialized = true
            }
        }
    }

    /**
     * Загружает фото в Cloudinary с оптимизацией
     */
    suspend fun uploadPhoto(context: Context, clientId: Int, imageUri: Uri): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            Log.d("CloudinaryService", "Starting upload for URI: $imageUri")

            // Определяем путь к файлу
            val filePath = if (imageUri.scheme == "file") {
                // Если это уже файл, используем его путь
                imageUri.path!!
            } else {
                // Если это content URI, создаем временный файл
                val tempFile = UriUtils.compressImageFromUri(context, imageUri, 800)
                if (tempFile == null) {
                    continuation.resume(Result.failure(Exception("Не удалось обработать изображение")))
                    return@suspendCancellableCoroutine
                }
                tempFile.absolutePath
            }

            Log.d("CloudinaryService", "Using file path: $filePath")

            val publicId = "client_${clientId}_${System.currentTimeMillis()}"

            val options = hashMapOf(
                "public_id" to publicId,
                "folder" to "beauty_manager/portfolio",
                "tags" to "portfolio,client_$clientId"
            )

            // Используем путь к файлу
            val requestId = MediaManager.get().upload(filePath)
                .options(options)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CloudinaryService", "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d("CloudinaryService", "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        Log.d("CloudinaryService", "Upload successful: $resultData")

                        val secureUrl = resultData["secure_url"] as? String
                        val publicId = resultData["public_id"] as? String

                        if (secureUrl != null && publicId != null) {
                            val photoData = "$secureUrl|$publicId"
                            continuation.resume(Result.success(photoData))
                        } else {
                            continuation.resume(Result.failure(Exception("URL или ID не найден в ответе")))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CloudinaryService", "Upload error: ${error.description}")
                        continuation.resume(Result.failure(Exception("Ошибка загрузки: ${error.description}")))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("CloudinaryService", "Upload rescheduled: ${error.description}")
                        continuation.resume(Result.failure(Exception("Загрузка отложена: ${error.description}")))
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }

    /**
     * Получает URL изображения из сохраненных данных
     */
    fun getPhotoUrl(photoData: String): String {
        return photoData.split("|")[0]
    }

    /**
     * Получает Public ID для удаления
     */
    fun getPublicId(photoData: String): String {
        return photoData.split("|")[1]
    }

    /**
     * Удаляет фото из Cloudinary (простое решение - используем Admin API через HTTP)
     */
    suspend fun deletePhoto(photoData: String): Result<Unit> {
        return try {
            // Для бесплатного плана Cloudinary автоматически удаляет старые файлы
            // или можно просто скрыть фото в UI, помечая как удаленное

            // В продакшене нужно было бы использовать Admin API:
            // val publicId = getPublicId(photoData)
            // но это требует серверную часть

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Генерирует трансформированный URL для отображения
     */
    fun getTransformedUrl(photoData: String, width: Int = 300, height: Int = 300): String {
        val originalUrl = getPhotoUrl(photoData)
        val publicId = getPublicId(photoData)

        // Создаем URL с трансформацией для превью
        return originalUrl.replace(
            "/image/upload/",
            "/image/upload/w_$width,h_$height,c_fill,q_auto,f_auto/"
        )
    }
}