package com.cleansea.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionStatus
import com.cleansea.ui.components.PointDetailsSheet
import com.cleansea.ui.navigation.Screen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    // Разрешения для местоположения
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Инициализация состояния камеры (Каспийское море)
    val caspianSea = LatLng(41.25, 51.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(caspianSea, 5f)
    }

    // Настройки UI карты
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false // Включается отдельно через properties
        )
    }

    // Получаем выбранную точку из ViewModel для отображения BottomSheet
    val selectedPoint by viewModel.selectedPoint

    // ОСНОВНОЙ КОНТЕЙНЕР ЭКРАНА. Scaffold был удален.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionsState.allPermissionsGranted
            ),
            onMapClick = { latLng ->
                viewModel.newPointCoords.value = latLng // Сохраняем координаты для новой точки
                navController.navigate(Screen.AddPoint.route)
            }
        ) {
            viewModel.pollutionPoints.forEach { point ->
                // Выбираем иконку в зависимости от статуса
                val markerColor = when (point.status) {
                    PollutionStatus.DETECTED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    PollutionStatus.IN_PROGRESS -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    PollutionStatus.CLEARED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                }

                Marker(
                    state = rememberMarkerState(position = LatLng(point.latitude, point.longitude)),
                    title = point.title,
                    snippet = point.description,
                    icon = markerColor,
                    onClick = {
                        viewModel.onMarkerClick(point) // Показываем BottomSheet
                        true // Возвращаем true, чтобы обработать клик и не показывать стандартный InfoWindow
                    }
                )
            }
        }

        // --- ЭЛЕМЕНТЫ ПОВЕРХ КАРТЫ ---

        // Плавающие кнопки были перенесены сюда из Scaffold
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Кнопка для запроса местоположения
            if (!locationPermissionsState.allPermissionsGranted) {
                ExtendedFloatingActionButton(
                    onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                    icon = { Icon(Icons.Filled.LocationOn, "Моё местоположение") },
                    text = { Text("Включить GPS") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Кнопка для добавления точки
            FloatingActionButton(onClick = {
                // При клике мы просто переходим на экран добавления.
                // Координаты будут добавлены при клике на саму карту.
                // Можно добавить логику, чтобы при нажатии на FAB брались координаты центра карты.
                navController.navigate(Screen.AddPoint.route)
            }) {
                Icon(Icons.Filled.Add, "Добавить точку")
            }
        }

        // Индикатор загрузки
        if (viewModel.isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Отображение ошибок
        viewModel.errorMessage.value?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(8.dp)
            )
        }
    }

    // --- МОДАЛЬНЫЕ ОКНА ---

    // Отображаем BottomSheet, если точка была выбрана.
    // Он находится вне Box, чтобы перекрывать весь экран.
    selectedPoint?.let { point ->
        PointDetailsSheet(
            point = point,
            isAdmin = viewModel.isAdmin.value,
            onDismiss = { viewModel.dismissPointDetails() },
            onStatusChange = { newStatus ->
                viewModel.updatePollutionPointStatus(point.id, newStatus)
            }
        )
    }
}