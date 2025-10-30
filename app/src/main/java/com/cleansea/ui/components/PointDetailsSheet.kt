package com.cleansea.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cleansea.R
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailsSheet(
    point: PollutionPoint,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onStatusChange: (PollutionStatus) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageBitmap = remember(point.imageUrl) {
                if (point.imageUrl == null) {
                    null
                } else {
                    try {
                        val decodedBytes = Base64.decode(point.imageUrl, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Фото загрязнения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = "https://picsum.photos/800/600",
                    contentDescription = "Фото загрязнения",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = point.type.displayNameResId),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = point.status)
            }
            Spacer(Modifier.height(8.dp))

            if (point.description.isNotBlank()) {
                Text(
                    text = point.description,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = "Координаты")
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${"%.4f".format(point.latitude)}, ${"%.4f".format(point.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(24.dp))

            if (isAdmin) {
                AdminControls(
                    currentStatus = point.status,
                    onStatusChange = onStatusChange,
                    onDelete = onDelete,
                    onDismiss = onDismiss
                )
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PollutionStatus) {
    val color = when (status) {
        PollutionStatus.DETECTED -> MaterialTheme.colorScheme.errorContainer
        PollutionStatus.IN_PROGRESS -> Color(0xFFFFD180)
        PollutionStatus.CLEARED -> Color(0xFFB9F6CA)
    }
    val textColor = when (status) {
        PollutionStatus.DETECTED -> MaterialTheme.colorScheme.onErrorContainer
        else -> Color.Black
    }
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text = stringResource(id = status.displayNameResId),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun AdminControls(
    currentStatus: PollutionStatus,
    onStatusChange: (PollutionStatus) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Подтверждение") },
            text = { Text("Вы уверены, что хотите удалить эту точку? Это действие необратимо.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Изменить статус:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PollutionStatus.values().forEach { status ->
                OutlinedButton(
                    onClick = { onStatusChange(status) },
                    enabled = status != currentStatus
                ) {
                    Text(stringResource(id = status.displayNameResId))
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
            Spacer(Modifier.width(8.dp))
            Text("Удалить точку")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.close))
        }
    }
}