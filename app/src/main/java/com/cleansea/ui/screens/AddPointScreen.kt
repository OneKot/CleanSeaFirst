package com.cleansea.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            viewModel.newPointImageUrl.value = uris.firstOrNull()?.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Фото сделано (функционал сохранения в разработке)", Toast.LENGTH_SHORT).show()
        }
    }

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
            if (viewModel.newPointCoords.value == null) {
                Text(
                    text = "Координаты не выбраны. Вернитесь на карту и тапните на место загрязнения.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(32.dp)
                )
            } else {
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // 1. ИСПРАВЛЕНА ОШИБКА С ИМЕНЕМ ПЕРЕМЕННОЙ
                        if (selectedImageUris.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = selectedImageUris.first()), // Отображаем первое фото
                                contentDescription = "Выбранное фото",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    // 1. ИСПРАВЛЕНА ЛОГИКА ОЧИСТКИ
                                    selectedImageUris = emptyList()
                                    viewModel.newPointImageUrl.value = null
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Галерея")
                        Spacer(Modifier.width(8.dp))
                        Text("Галерея")
                    }
                    Button(
                        onClick = { cameraLauncher.launch(null) }, // Теперь работает
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Камера")
                        Spacer(Modifier.width(8.dp))
                        Text("Камера")
                    }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.newPointDescription.value,
                    onValueChange = { viewModel.newPointDescription.value = it },
                    label = { Text("Описание (необязательно)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val newPoint = PollutionPoint(
                            id = UUID.randomUUID().toString(),
                            latitude = viewModel.newPointCoords.value!!.latitude,
                            longitude = viewModel.newPointCoords.value!!.longitude,
                            title = viewModel.newPointType.value.name,
                            description = viewModel.newPointDescription.value,
                            type = viewModel.newPointType.value,
                            imageUrl = viewModel.newPointImageUrl.value
                        )
                        viewModel.addPollutionPoint(newPoint)
                        Toast.makeText(context, "Точка добавлена!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    enabled = !viewModel.isLoading.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Сообщить")
                }
            }
        }
    }
}

@Composable
fun PollutionTypeSelector(
    selectedType: PollutionType,
    onTypeSelected: (PollutionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        PollutionType.values().forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                // capitalize() устарел, используем более современный вариант
                label = { Text(type.name.replace('_', ' ').replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}