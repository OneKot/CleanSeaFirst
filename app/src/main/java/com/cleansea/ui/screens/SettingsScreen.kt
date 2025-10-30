package com.cleansea.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleansea.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val isAdmin = viewModel.isAdmin.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Секция общих настроек ---
        Text("Основные", style = MaterialTheme.typography.titleLarge)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Язык приложения", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Русский")
                    Button(onClick = {
                        viewModel.changeLanguage("ru")
                        Toast.makeText(context, "Функция смены языка в разработке", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Изменить")
                    }
                }
            }
        }

        if (isAdmin) {
            Spacer(Modifier.height(24.dp))
            Text("Администрирование", style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Экспорт данных", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Вы можете выгрузить все данные о точках загрязнения в виде отчета.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.exportReports()
                            Toast.makeText(context, "Экспорт отчета запущен...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Экспорт")
                        Spacer(Modifier.width(8.dp))
                        Text("Экспортировать отчеты")
                    }
                }
            }
        }
    }
}