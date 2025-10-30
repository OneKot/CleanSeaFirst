package com.cleansea.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory // Для кастомных иконок маркеров
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionStatus
import com.cleansea.data.PollutionType
import com.cleansea.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            myLocationButtonEnabled = false // Включим позже, после проверки разрешений
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Карта Чистоты Каспия") },
                navigationIcon = {
                    IconButton(onClick = {
                        // TODO: Открыть боковое меню или профиль
                        scope.launch {
                            snackbarHostState.showSnackbar("Меню пока не реализовано")
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Меню")
                    }
                },
                actions = {
                    // Кнопка для выхода (только если авторизован)
                    if (viewModel.isAuthenticated.value) {
                        Button(onClick = { viewModel.logout(); navController.navigate(Screen.Auth.route) { popUpTo(Screen.Map.route) { inclusive = true } } }) {
                            Text("Выход")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
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
                    navController.navigate(Screen.AddPoint.route)
                }) {
                    Icon(Icons.Filled.Add, "Добавить точку")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings.copy(
                    myLocationButtonEnabled = locationPermissionsState.allPermissionsGranted
                ),
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionsState.allPermissionsGranted
                ),
                onMapClick = { latLng ->
                    viewModel.newPointCoords.value = latLng // Сохраняем координаты для новой точки
                    navController.navigate(Screen.AddPoint.route)
                },
                onMapLongClick = { latLng ->
                    // Можно использовать для другой функции, например, быстрого добавления
                }
            ) {
                viewModel.pollutionPoints.forEach { point ->
                    // Выбираем иконку в зависимости от статуса и типа
                    val markerColor = when (point.status) {
                        PollutionStatus.DETECTED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        PollutionStatus.IN_PROGRESS -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        PollutionStatus.CLEARED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }

                    Marker(
                        state = remember { MarkerState(position = LatLng(point.latitude, point.longitude)) },
                        title = point.title,
                        snippet = point.description,
                        icon = markerColor, // Кастомная иконка по статусу
                        onClick = { marker ->
                            // Навигация к деталям точки (заглушка)
                            scope.launch {
                                snackbarHostState.showSnackbar("Детали точки: ${point.title} (ID: ${point.id})")
                            }
                            // Если бы был экран деталей: navController.navigate("detail/${point.id}")
                            true // Возвращаем true, чтобы обработать клик и не показывать стандартный InfoWindow
                        }
                    )
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
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(8.dp)
                )
            }
        }
    }
}