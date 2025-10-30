package com.cleansea.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailsSheet(
    point: PollutionPoint,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onStatusChange: (PollutionStatus) -> Unit // Функция для админа
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
            // Фото
            AsyncImage(
                model = point.imageUrl ?: "https://picsum.photos/800/600", // Заглушка, если нет фото
                contentDescription = "Фото загрязнения",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))

            // Тип и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = point.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = point.status)
            }
            Spacer(Modifier.height(8.dp))

            // Описание
            if (point.description.isNotBlank()) {
                Text(
                    text = point.description,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
            }

            // Координаты
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

            // --- Панель управления в зависимости от роли ---
            if (isAdmin) {
                AdminControls(
                    currentStatus = point.status,
                    onStatusChange = onStatusChange,
                    onDismiss = onDismiss
                )
            } else {
                // Обычный пользователь видит только кнопку "Закрыть"
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PollutionStatus) {
    val color = when (status) {
        PollutionStatus.DETECTED -> MaterialTheme.colorScheme.errorContainer
        PollutionStatus.IN_PROGRESS -> Color(0xFFFFD180) // Amber A200
        PollutionStatus.CLEARED -> Color(0xFFB9F6CA) // Light Green A200
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
            text = status.name,
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
    onDismiss: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Изменить статус:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Кнопки для смены статуса
            PollutionStatus.values().forEach { status ->
                OutlinedButton(
                    onClick = { onStatusChange(status) },
                    enabled = status != currentStatus
                ) {
                    Text(status.name)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Закрыть")
        }
    }
}