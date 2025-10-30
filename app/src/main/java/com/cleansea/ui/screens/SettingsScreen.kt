package com.cleansea.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val isAdmin = viewModel.isAdmin.value
    var showLanguageDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val csvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { fileUri ->
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                            val csvContent = viewModel.generateCsvContent(context)
                            outputStream.write(csvContent.toByteArray())
                        }
                        Toast.makeText(context, "Отчет успешно сохранен", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Ошибка сохранения отчета", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_dialog_title)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.language_ru),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.changeLanguage(context, "ru"); showLanguageDialog = false
                        }.padding(vertical = 12.dp))
                    Text(text = stringResource(R.string.language_en),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.changeLanguage(context, "en"); showLanguageDialog = false
                        }.padding(vertical = 12.dp))
                    Text(text = stringResource(R.string.language_kk),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.changeLanguage(context, "kk"); showLanguageDialog = false
                        }.padding(vertical = 12.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Text(stringResource(R.string.settings_section_main), style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                        val currentLang = when {
                            currentLangTag.startsWith("ru") -> stringResource(R.string.language_ru)
                            currentLangTag.startsWith("en") -> stringResource(R.string.language_en)
                            currentLangTag.startsWith("kk") -> stringResource(R.string.language_kk)
                            else -> currentLangTag
                        }
                        Text(currentLang)
                        Button(onClick = { showLanguageDialog = true }) {
                            Text(stringResource(R.string.settings_language_change))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_volunteer_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.settings_volunteer_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = viewModel.isVolunteer.value, onCheckedChange = { viewModel.toggleVolunteerStatus() })
                }
            }

            if (isAdmin) {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.settings_section_admin), style = MaterialTheme.typography.titleLarge)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.settings_export_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.settings_export_subtitle), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                csvFileLauncher.launch("cleansea_report.csv")
                            },
                            enabled = !viewModel.isLoading.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Экспорт")
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_export_button))
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
            Spacer(Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}