package com.cleansea.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleansea.MainViewModel
import com.cleansea.data.PollutionStatus
import com.cleansea.data.PollutionType

@Composable
fun StatisticsScreen(viewModel: MainViewModel = viewModel()) {
    val stats = viewModel.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Статистика загрязнений",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        // Карточка с общим количеством точек
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Всего сообщений", style = MaterialTheme.typography.titleLarge)
                Text(
                    stats.totalPoints.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Карточка со статистикой по статусам
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Сводка по статусам", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                StatRow("Обнаружено (не очищено)", stats.pointsByStatus[PollutionStatus.DETECTED] ?: 0)
                Divider()
                StatRow("В работе", stats.pointsByStatus[PollutionStatus.IN_PROGRESS] ?: 0)
                Divider()
                StatRow("Очищено", stats.pointsByStatus[PollutionStatus.CLEARED] ?: 0)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Карточка со статистикой по типам
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Разбивка по типам", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                PollutionType.values().forEach { type ->
                    StatRow(type.name.replace('_', ' '), stats.pointsByType[type] ?: 0)
                }
            }
        }
    }
}

// Вспомогательный компонент для отображения строки статистики
@Composable
fun StatRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}