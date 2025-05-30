package ru.batir8888.beautymanager.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ru.batir8888.beautymanager.data.model.Client
import ru.batir8888.beautymanager.utils.UriUtils
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PortfolioSection(
    client: Client,
    onAddPhoto: (Uri) -> Unit,
    onDeletePhoto: (String) -> Unit,
    getPhotoUrl: (String) -> String,
    getThumbnailUrl: (String) -> String,
    isUploading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Разрешения для разных версий Android
    val readImagesPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Новый Photo Picker для Android 13+ (рекомендуется)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        Log.d("PortfolioSection", "Photo picker result: $uri")
        uri?.let { selectedUri ->
            // Немедленно копируем файл синхронно
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                if (inputStream != null) {
                    val fileName = "upload_${System.currentTimeMillis()}.jpg"
                    val tempFile = File(context.cacheDir, fileName)

                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                    }
                    inputStream.close()

                    if (tempFile.exists() && tempFile.length() > 0) {
                        val fileUri = Uri.fromFile(tempFile)
                        Log.d("PortfolioSection", "File copied successfully: ${tempFile.absolutePath}")
                        onAddPhoto(fileUri)
                    } else {
                        Log.e("PortfolioSection", "Failed to copy file")
                    }
                } else {
                    Log.e("PortfolioSection", "Cannot open input stream")
                }
            } catch (e: Exception) {
                Log.e("PortfolioSection", "Error copying file", e)
            }
        }
    }

    // Старый способ для Android 12 и ниже
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("PortfolioSection", "Image picked (legacy): $uri")
        uri?.let { selectedUri ->
            // Сразу создаем постоянную копию файла
            val tempFile = UriUtils.createTempFileFromUri(context, selectedUri)
            if (tempFile != null) {
                val permanentUri = Uri.fromFile(tempFile)
                Log.d("PortfolioSection", "Created permanent file: ${tempFile.absolutePath}")
                onAddPhoto(permanentUri)
            } else {
                Log.e("PortfolioSection", "Failed to create temp file from URI: $selectedUri")
            }
        } ?: Log.d("PortfolioSection", "No image selected")
    }

    // Launcher для запроса разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("PortfolioSection", "Permissions result: $permissions")
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d("PortfolioSection", "All permissions granted, launching image picker")
            imagePickerLauncher.launch("image/*")
        } else {
            Log.d("PortfolioSection", "Permissions denied")
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Портфолио (${client.portfolioPhotos.size})",
                style = MaterialTheme.typography.titleMedium
            )

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item(key = "add_button") {
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(enabled = !isUploading) {
                            Log.d("PortfolioSection", "Add photo button clicked")

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Log.d("PortfolioSection", "Using new Photo Picker for Android 13+")
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } else {
                                Log.d("PortfolioSection", "Using legacy picker for Android 12-")
                                if (readImagesPermission.status.isGranted) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    Log.d("PortfolioSection", "Requesting permission")
                                    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        android.Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                    permissionLauncher.launch(arrayOf(permission))
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUploading)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Добавить фото",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Добавить",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Существующие фото
            items(
                items = client.portfolioPhotos,
                key = { photoData -> photoData.hashCode() }
            ) { photoData ->
                PhotoItem(
                    photoData = photoData,
                    photoUrl = getThumbnailUrl(photoData),
                    onDelete = { onDeletePhoto(photoData) },
                    modifier = Modifier.size(360.dp)
                )
            }
        }

        if (client.portfolioPhotos.isEmpty() && !isUploading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавьте фотографии работ для клиента",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoItem(
    photoData: String,
    photoUrl: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить фото?") },
            text = { Text("Действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .combinedClickable(
                onLongClick = { showDeleteDialog = true },
                onClick = { /* TODO: открыть в полном размере */ }
            ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Фото портфолио",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Иконка удаления в углу
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить фото",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}