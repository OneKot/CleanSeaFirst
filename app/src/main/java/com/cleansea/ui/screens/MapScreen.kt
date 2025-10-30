package com.cleansea.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cleansea.MainViewModel
import com.cleansea.R
import com.cleansea.data.PollutionStatus
import com.cleansea.ui.components.PointDetailsSheet
import com.cleansea.ui.navigation.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private fun bitmapDescriptorWithIcon(
    context: Context,
    @DrawableRes backgroundResId: Int,
    @DrawableRes iconResId: Int,
    color: Int
): BitmapDescriptor? {
    val background: Drawable = ContextCompat.getDrawable(context, backgroundResId)?.mutate() ?: return null
    val icon: Drawable = ContextCompat.getDrawable(context, iconResId)?.mutate() ?: return null
    background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    background.setBounds(0, 0, canvas.width, canvas.height)
    background.draw(canvas)
    val iconSizeRatio = 0.5f
    val iconSize = (canvas.width * iconSizeRatio).toInt()
    val horizontalPadding = (canvas.width - iconSize) / 2
    val verticalPadding = (canvas.height - iconSize) / 2
    val topOffset = (canvas.height * 0.1).toInt()
    icon.setBounds(
        horizontalPadding,
        verticalPadding - topOffset,
        canvas.width - horizontalPadding,
        canvas.height - verticalPadding - topOffset
    )
    icon.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = permissionsToRequest
    )

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    val isLocationPermissionGranted = multiplePermissionsState.permissions.any {
        it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
    }

    val caspianSea = LatLng(41.25, 51.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(caspianSea, 5f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true
        )
    }

    val selectedPoint by viewModel.selectedPoint

    Box(modifier = Modifier.fillMaxSize()) {
        key(viewModel.pollutionPoints.size) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings.copy(
                    myLocationButtonEnabled = isLocationPermissionGranted
                ),
                properties = MapProperties(
                    isMyLocationEnabled = isLocationPermissionGranted
                ),
                onMapClick = { latLng ->
                    viewModel.newPointCoords.value = latLng
                    navController.navigate(Screen.AddPoint.route)
                }
            ) {
                viewModel.pollutionPoints.forEach { point ->
                    val markerColor = when (point.status) {
                        PollutionStatus.DETECTED -> android.graphics.Color.RED
                        PollutionStatus.IN_PROGRESS -> android.graphics.Color.rgb(255, 165, 0) // Orange
                        PollutionStatus.CLEARED -> android.graphics.Color.GREEN
                    }

                    val combinedIcon = bitmapDescriptorWithIcon(
                        context = context,
                        backgroundResId = R.drawable.ic_marker_background,
                        iconResId = point.type.iconResId,
                        color = markerColor
                    )

                    Marker(
                        state = rememberMarkerState(position = LatLng(point.latitude, point.longitude)),
                        title = stringResource(id = point.type.displayNameResId),
                        snippet = point.description,
                        icon = combinedIcon,
                        onClick = {
                            viewModel.onMarkerClick(point)
                            true
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (!isLocationPermissionGranted) {
                ExtendedFloatingActionButton(
                    onClick = { multiplePermissionsState.launchMultiplePermissionRequest() },
                    icon = { Icon(Icons.Filled.LocationOn, "Моё местоположение") },
                    text = { Text("Включить GPS") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddPoint.route)
            }) {
                Icon(Icons.Filled.Add, "Добавить точку")
            }
        }

        if (viewModel.isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

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

    selectedPoint?.let { point ->
        PointDetailsSheet(
            point = point,
            isAdmin = viewModel.isAdmin.value,
            onDismiss = { viewModel.dismissPointDetails() },
            onStatusChange = { newStatus ->
                viewModel.updatePollutionPointStatus(point.id, newStatus)
            },
            onDelete = {
                viewModel.deletePoint(point.id)
            }
        )
    }
}