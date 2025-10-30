package com.cleansea

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionStatus
import com.cleansea.data.PollutionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat

data class PollutionStats(
    val totalPoints: Int = 0,
    val pointsByStatus: Map<PollutionStatus, Int> = emptyMap(),
    val pointsByType: Map<PollutionType, Int> = emptyMap()
)
class ApiService {
    suspend fun fetchPollutionPoints(): List<PollutionPoint> {
        delay(1000) // Имитация задержки сети
        return listOf(
            PollutionPoint(
                id = UUID.randomUUID().toString(),
                latitude = 42.0, longitude = 52.0,
                title = "Куча мусора",
                description = "Большое скопление бытовых отходов",
                type = PollutionType.TRASH,
                status = PollutionStatus.DETECTED,
                imageUrl = "https://picsum.photos/200/300?random=1"
            ),
            PollutionPoint(
                id = UUID.randomUUID().toString(),
                latitude = 41.5, longitude = 51.0,
                title = "Нефтяное пятно",
                description = "Небольшое нефтяное пятно у берега",
                type = PollutionType.OIL_SPOT,
                status = PollutionStatus.IN_PROGRESS,
                imageUrl = "https://picsum.photos/200/300?random=2"
            )
        )
    }

    suspend fun addPollutionPoint(point: PollutionPoint): PollutionPoint {
        delay(500) // Имитация задержки сети
        return point.copy(id = UUID.randomUUID().toString(), status = PollutionStatus.DETECTED)
    }

    suspend fun updatePollutionPointStatus(pointId: String, status: PollutionStatus): PollutionPoint {
        delay(300)

        return PollutionPoint(id = pointId, latitude = 0.0, longitude = 0.0, title = "", description = "", type = PollutionType.OTHER, status = status)
    }
}


class MainViewModel : ViewModel() {
    private val apiService = ApiService()

    val pollutionPoints = mutableStateListOf<PollutionPoint>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(false) // Статус авторизации
    val isAdmin = mutableStateOf(false) // Статус админа
    val selectedPoint = mutableStateOf<PollutionPoint?>(null)
    val isVolunteer = mutableStateOf(false)
    val notificationMessage = mutableStateOf<String?>(null)
    val csvExportContent = mutableStateOf<String?>(null)

    val newPointCoords = mutableStateOf<LatLng?>(null)
    val newPointType = mutableStateOf(PollutionType.TRASH)
    val newPointDescription = mutableStateOf("")
    val newPointImageUrl = mutableStateOf<String?>(null)
    fun toggleVolunteerStatus() {
        isVolunteer.value = !isVolunteer.value
    }
    fun onCsvExported() {
        csvExportContent.value = null
    }

    fun clearNotificationMessage() {
        notificationMessage.value = null
    }

    init {
        // Загружаем точки при инициализации ViewModel
        fetchPollutionPoints()
    }
    fun onMarkerClick(point: PollutionPoint) {
        selectedPoint.value = point
    }
    fun dismissPointDetails() {
        selectedPoint.value = null
    }

    fun fetchPollutionPoints() {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                pollutionPoints.clear()
                pollutionPoints.addAll(apiService.fetchPollutionPoints())
            } catch (e: Exception) {
                errorMessage.value = "Ошибка загрузки точек: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addPollutionPoint(point: PollutionPoint) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val addedPoint = apiService.addPollutionPoint(point)
                if (isVolunteer.value) {
                    notificationMessage.value = "Новая точка: ${addedPoint.title}"
                }
                // Сброс полей для новой точки
                newPointCoords.value = null

                pollutionPoints.add(addedPoint)
                // Сброс полей для новой точки
                newPointCoords.value = null
                newPointType.value = PollutionType.TRASH
                newPointDescription.value = ""
                newPointImageUrl.value = null
            } catch (e: Exception) {
                errorMessage.value = "Ошибка добавления точки: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updatePollutionPointStatus(pointId: String, status: PollutionStatus) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                // Имитируем обновление статуса на бэкенде
                apiService.updatePollutionPointStatus(pointId, status)
                val index = pollutionPoints.indexOfFirst { it.id == pointId }
                if (index != -1) {
                    pollutionPoints[index] = pollutionPoints[index].copy(status = status)
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка обновления статуса: ${e.message}"
            } finally {
                isLoading.value = false
                dismissPointDetails() // <-- ЗАКРЫВАЕМ ОКНО ПОСЛЕ ОБНОВЛЕНИЯ
            }
        }
    }

    // Заглушка для регистрации/авторизации
    fun login(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            delay(1000)
            if (email == "admin" && password == "admin") {
                isAuthenticated.value = true
                isAdmin.value = true
            } else if (email == "user" && password == "user") {
                isAuthenticated.value = true
                isAdmin.value = false
            }else if (email == "a" && password == "a") {
                isAuthenticated.value = true
                isAdmin.value = true
            } else {
                errorMessage.value = "Неверный логин или пароль"
            }
            isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            delay(1000)
            if (email == "admin@example.com" || email == "user@example.com") {
                errorMessage.value = "Пользователь с таким email уже существует"
            } else {
                isAuthenticated.value = true // Автоматический вход после регистрации
                isAdmin.value = false // Новые пользователи не админы по умолчанию
            }
            isLoading.value = false
        }
    }

    fun logout() {
        isAuthenticated.value = false
        isAdmin.value = false
    }
    val stats by derivedStateOf {
        val points = pollutionPoints
        PollutionStats(
            totalPoints = points.size,
            pointsByStatus = points.groupingBy { it.status }.eachCount(),
            pointsByType = points.groupingBy { it.type }.eachCount()
        )
    }
    fun changeLanguage(context: Context, langCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun exportReports(context: Context) {
        isLoading.value = true
        viewModelScope.launch {
            // Генерируем контент в фоновом потоке
            val csvContent = generateCsvContent(context)
            // Передаем готовый контент в наше состояние
            csvExportContent.value = csvContent
            isLoading.value = false
        }
    }
    private fun generateCsvContent(context: Context): String {
        val header = "ID,Тип,Статус,Описание,Широта,Долгота,URL Фото"
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(header)

        pollutionPoints.forEach { point ->
            val description = "\"${point.description.replace("\"", "\"\"")}\"" // Экранируем кавычки в описании
            val line = listOf(
                point.id,
                context.getString(point.type.displayNameResId),
                context.getString(point.status.displayNameResId),
                description,
                point.latitude.toString(),
                point.longitude.toString(),
                point.imageUrl ?: "N/A"
            ).joinToString(",")
            stringBuilder.appendLine(line)
        }
        return stringBuilder.toString()
    }

}