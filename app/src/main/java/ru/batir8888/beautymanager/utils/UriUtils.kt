package ru.batir8888.beautymanager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object UriUtils {

    /**
     * Создает временную копию файла из URI для надежного доступа
     */
    fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            Log.d("UriUtils", "Processing URI: $uri")

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Log.e("UriUtils", "Cannot open input stream for URI: $uri")
                return null
            }

            // Определяем расширение файла
            val mimeType = context.contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> ".jpg"
                mimeType?.contains("png") == true -> ".png"
                mimeType?.contains("webp") == true -> ".webp"
                else -> ".jpg"
            }

            // Создаем временный файл в cache директории
            val tempFile = File(
                context.cacheDir,
                "upload_${System.currentTimeMillis()}$extension"
            )

            // Копируем данные с принудительной синхронизацией
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
                outputStream.flush()
                outputStream.fd.sync() // Принудительная синхронизация
            }

            inputStream.close()

            // Проверяем что файл создался
            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d("UriUtils", "Created temp file: ${tempFile.absolutePath}, size: ${tempFile.length()}")
                tempFile
            } else {
                Log.e("UriUtils", "Temp file creation failed or file is empty")
                null
            }

        } catch (e: Exception) {
            Log.e("UriUtils", "Error creating temp file from URI: $uri", e)
            null
        }
    }

    /**
     * Сжимает изображение и сохраняет в временный файл
     */
    fun compressImageFromUri(context: Context, uri: Uri, maxSizeKb: Int = 500): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("UriUtils", "Cannot decode bitmap from URI: $uri")
                return null
            }

            // Изменяем размер если нужно
            val resizedBitmap = resizeBitmapIfNeeded(originalBitmap, 1200, 1200)

            // Сжимаем с разным качеством до достижения нужного размера
            var quality = 85
            var compressedFile: File?

            do {
                compressedFile = File.createTempFile(
                    "compressed_${System.currentTimeMillis()}",
                    ".jpg",
                    context.cacheDir
                )

                FileOutputStream(compressedFile).use { outputStream ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }

                val fileSizeKb = compressedFile.length() / 1024
                Log.d("UriUtils", "Compressed with quality $quality: ${fileSizeKb}KB")

                if (fileSizeKb <= maxSizeKb || quality <= 20) {
                    break
                }

                quality -= 10
                compressedFile.delete() // Удаляем предыдущую версию

            } while (quality > 20)

            originalBitmap.recycle()
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }

            Log.d("UriUtils", "Final compressed file: ${compressedFile?.length()?.div(1024)}KB")
            compressedFile

        } catch (e: Exception) {
            Log.e("UriUtils", "Error compressing image from URI: $uri", e)
            null
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Проверяет, доступен ли URI
     */
    fun isUriAccessible(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            Log.e("UriUtils", "URI not accessible: $uri", e)
            false
        }
    }
}