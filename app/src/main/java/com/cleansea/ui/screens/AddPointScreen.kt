package com.cleansea.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Выбор изображения из галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            viewModel.newPointImageUrl.value = uris.firstOrNull()?.toString()
        }
    }

    var description by remember { mutableStateOf("") }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Тип загрязнения",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            PollutionTypeSelector(
                selectedType = viewModel.newPointType.value,
                onTypeSelected = { viewModel.newPointType.value = it }
            )

            Spacer(Modifier.height(16.dp))
            Text(text = "Добавьте фото загрязнения", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (selectedImageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImageUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable {
                                    selectedImageUris = selectedImageUris - uri
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Фото загрязнения",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить фото",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Выбрать фото")
                Spacer(Modifier.width(8.dp))
                Text("Добавить фото")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание загрязнения") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (viewModel.newPointType.value == null || description.isBlank()) {
                        Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    } else {
                        val point = PollutionPoint(
                            id = null,
                            type = viewModel.newPointType.value!!,
                            description = description,
                            imageUrl = viewModel.newPointImageUrl.value,
                            status = "новая"
                        )
                        viewModel.addPollutionPoint(point)
                        Toast.makeText(context, "Точка отправлена", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отправить")
            }
        }
    }
}

@Composable
fun PollutionTypeSelector(
    selectedType: PollutionType?,
    onTypeSelected: (PollutionType) -> Unit
) {
    val pollutionTypes = PollutionType.values()
    Column(Modifier.fillMaxWidth()) {
        pollutionTypes.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
                Spacer(Modifier.width(8.dp))
                Text(type.displayName)
            }
        }
    }
}
