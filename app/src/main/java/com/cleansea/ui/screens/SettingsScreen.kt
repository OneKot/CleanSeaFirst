package com.cleansea.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleansea.MainViewModel
import com.cleansea.R

@Composable
fun SettingsScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val isAdmin = viewModel.isAdmin.value
    var showLanguageDialog by remember { mutableStateOf(false) }

    // --- Диалоговое окно для выбора языка ---
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_dialog_title)) },
            text = {
                Column {
                    // Русский
                    Text(
                        text = stringResource(R.string.language_ru),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeLanguage(context, "ru")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    // Английский
                    Text(
                        text = stringResource(R.string.language_en),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeLanguage(context, "en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    // Казахский
                    Text(
                        text = stringResource(R.string.language_kk),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeLanguage(context, "kk")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // --- Основной контент экрана ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Секция общих настроек ---
        Text(stringResource(R.string.settings_section_main), style = MaterialTheme.typography.titleLarge)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Карточка смены языка
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Показываем текущий язык системы
                    val currentLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                    val currentLang = when {
                        currentLangTag.startsWith("ru") -> stringResource(R.string.language_ru)
                        currentLangTag.startsWith("en") -> stringResource(R.string.language_en)
                        currentLangTag.startsWith("kk") -> stringResource(R.string.language_kk)
                        else -> "Unknown"
                    }
                    Text(currentLang)
                    Button(onClick = { showLanguageDialog = true }) {
                        Text(stringResource(R.string.settings_language_change))
                    }
                }
            }
        }

        // Карточка статуса волонтера
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_volunteer_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.settings_volunteer_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = viewModel.isVolunteer.value,
                    onCheckedChange = { viewModel.toggleVolunteerStatus() }
                )
            }
        }

        // --- Секция только для администраторов ---
        if (isAdmin) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_section_admin), style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_export_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.settings_export_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.exportReports(context) }, // <-- ПРАВИЛЬНЫЙ ВЫЗОВ
                        enabled = !viewModel.isLoading.value,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_export_generating))
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "Экспорт")
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_export_button))
                        }
                    }
                }
            }
        }
    }
}