package com.cleansea.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionType
import java.io.File
import java.util.*

private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "camera_photo_",
        ".jpg",
        context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageVersion by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            cameraImageUri = null
            selectedImageUris = uris
            imageVersion++
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUris = emptyList()
            imageVersion++
        } else {
            cameraImageUri = null
        }
    }

    val imageToDisplay = cameraImageUri ?: selectedImageUris.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сообщить о загрязнении") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.newPointCoords.value != null) {
                // ... ваш код для отображения координат и выбора типа ...
                Text(
                    text = "Координаты: ${"%.4f".format(viewModel.newPointCoords.value?.latitude)}, ${"%.4f".format(viewModel.newPointCoords.value?.longitude)}",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(text = "Выберите тип загрязнения", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                PollutionTypeSelector(
                    selectedType = viewModel.newPointType.value,
                    onTypeSelected = { viewModel.newPointType.value = it }
                )
                Spacer(Modifier.height(24.dp))


                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (imageToDisplay != null) {
                            key(imageVersion) { // <-- ИСПОЛЬЗУЕМ KEY ЗДЕСЬ
                                Image(
                                    painter = rememberAsyncImagePainter(imageToDisplay), // <-- ПРОСТОЙ ВЫЗОВ
                                    contentDescription = "Выбранное фото",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedImageUris = emptyList()
                                    cameraImageUri = null
                                    imageVersion++
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить фото", tint = Color.White)
                            }
                        } else {
                            Text("Добавьте фото", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // ... ваш код для кнопок, поля описания и кнопки "Сообщить" ...
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Галерея")
                        Spacer(Modifier.width(8.dp))
                        Text("Галерея")
                    }
                    Button(onClick = {
                        val newUri = createImageUri(context)
                        cameraImageUri = newUri
                        cameraLauncher.launch(newUri)
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Камера")
                        Spacer(Modifier.width(8.dp))
                        Text("Камера")
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = viewModel.newPointDescription.value, onValueChange = { viewModel.newPointDescription.value = it }, label = { Text("Описание (необязательно)") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 4)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val finalImageUri = cameraImageUri ?: selectedImageUris.firstOrNull()
                        val newPoint = PollutionPoint(
                            id = UUID.randomUUID().toString(),
                            latitude = viewModel.newPointCoords.value!!.latitude,
                            longitude = viewModel.newPointCoords.value!!.longitude,
                            title = viewModel.newPointType.value.displayName,
                            description = viewModel.newPointDescription.value,
                            type = viewModel.newPointType.value,
                            imageUrl = finalImageUri?.toString()
                        )
                        viewModel.addPollutionPoint(newPoint)
                        Toast.makeText(context, "Точка добавлена!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    enabled = !viewModel.isLoading.value && imageToDisplay != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Сообщить")
                }
            } else {
                Text(
                    text = "Координаты не выбраны. Вернитесь на карту и тапните на место загрязнения.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun PollutionTypeSelector(selectedType: PollutionType, onTypeSelected: (PollutionType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PollutionType.values().forEach { type ->
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(text = type.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}