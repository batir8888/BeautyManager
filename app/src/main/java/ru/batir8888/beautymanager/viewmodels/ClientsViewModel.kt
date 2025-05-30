package ru.batir8888.beautymanager.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.batir8888.beautymanager.BeautyManagerApplication
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.services.CloudinaryService
import ru.batir8888.beautymanager.utils.UriUtils

class ClientsViewModel : ViewModel() {
    private val clientDao = BeautyManagerApplication.database.clientDao()
    private val cloudinaryService = CloudinaryService()

    val clients = clientDao.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Состояние загрузки фото
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto = _isUploadingPhoto.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError = _uploadError.asStateFlow()

    // Прогресс загрузки (опционально)
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    fun clientFlow(id: Int) = clientDao.getClientById(id)

    suspend fun insertAndGetId(client: Client): Int =
        clientDao.insertClient(client).toInt()

    suspend fun updateClient(client: Client) =
        clientDao.updateClient(client)

    fun deleteClient(client: Client) = viewModelScope.launch {
        // Удаляем фото из Cloudinary
        client.portfolioPhotos.forEach { photoData ->
            cloudinaryService.deletePhoto(photoData)
        }

        clientDao.deleteClient(client)
    }

    /**
     * Добавляет фото в портфолио клиента через Cloudinary
     */
    fun addPhotoToPortfolio(context: Context, clientId: Int, imageUri: Uri, currentClient: Client) {
        Log.d("ClientsViewModel", "addPhotoToPortfolio called with clientId: $clientId, URI: $imageUri")

        // Проверяем доступность URI
        if (!UriUtils.isUriAccessible(context, imageUri)) {
            _uploadError.value = "Изображение недоступно. Попробуйте выбрать другое."
            return
        }

        viewModelScope.launch {
            _isUploadingPhoto.value = true
            _uploadError.value = null

            try {
                Log.d("ClientsViewModel", "Starting photo upload...")
                val result = cloudinaryService.uploadPhoto(context, clientId, imageUri)

                if (result.isSuccess) {
                    val photoData = result.getOrThrow()
                    Log.d("ClientsViewModel", "Photo uploaded successfully: $photoData")

                    val updatedPhotos = currentClient.portfolioPhotos + photoData
                    val updatedClient = currentClient.copy(portfolioPhotos = updatedPhotos)

                    Log.d("ClientsViewModel", "Updating client with ${updatedPhotos.size} photos")
                    updateClient(updatedClient)

                    _uploadError.value = null
                } else {
                    val error = "Ошибка загрузки: ${result.exceptionOrNull()?.message}"
                    Log.e("ClientsViewModel", error, result.exceptionOrNull())
                    _uploadError.value = error
                }
            } catch (e: Exception) {
                val error = "Ошибка: ${e.message}"
                Log.e("ClientsViewModel", error, e)
                _uploadError.value = error
            } finally {
                _isUploadingPhoto.value = false
                Log.d("ClientsViewModel", "Upload process finished")
            }
        }
    }

    /**
     * Удаляет фото из портфолио
     */
    fun removePhotoFromPortfolio(photoData: String, currentClient: Client) {
        viewModelScope.launch {
            try {
                cloudinaryService.deletePhoto(photoData)

                val updatedPhotos = currentClient.portfolioPhotos.filter { it != photoData }
                val updatedClient = currentClient.copy(portfolioPhotos = updatedPhotos)

                updateClient(updatedClient)
            } catch (e: Exception) {
                _uploadError.value = "Ошибка удаления фото: ${e.message}"
            }
        }
    }

    /**
     * Получает URL для отображения фото
     */
    fun getPhotoUrl(photoData: String): String {
        return cloudinaryService.getPhotoUrl(photoData)
    }

    /**
     * Получает URL для превью (уменьшенное изображение)
     */
    fun getPhotoThumbnailUrl(photoData: String): String {
        return cloudinaryService.getTransformedUrl(photoData, 300, 300)
    }

    fun clearUploadError() {
        _uploadError.value = null
    }
}