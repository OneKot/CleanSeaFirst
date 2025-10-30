package com.cleansea

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansea.data.PollutionPoint
import com.cleansea.data.PollutionStatus
import com.cleansea.data.PollutionType
import com.cleansea.util.LocaleHelper
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.roundToInt

data class PollutionStats(
    val totalPoints: Int = 0,
    val pointsByStatus: Map<PollutionStatus, Int> = emptyMap(),
    val pointsByType: Map<PollutionType, Int> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val pollutionPoints = mutableStateListOf<PollutionPoint>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(auth.currentUser != null)
    val isAdmin = mutableStateOf(false)
    val selectedPoint = mutableStateOf<PollutionPoint?>(null)
    val isVolunteer = mutableStateOf(sharedPrefs.getBoolean("is_volunteer", false))
    val notificationMessage = mutableStateOf<String?>(null)
    val csvExportContent = mutableStateOf<String?>(null)
    val newPointCoords = mutableStateOf<LatLng?>(null)
    val newPointType = mutableStateOf(PollutionType.TRASH)
    val newPointDescription = mutableStateOf("")

    val stats by derivedStateOf {
        val points = pollutionPoints
        PollutionStats(
            totalPoints = points.size,
            pointsByStatus = points.groupingBy { it.status }.eachCount(),
            pointsByType = points.groupingBy { it.type }.eachCount()
        )
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            isAuthenticated.value = user != null
            if (user != null) {
                isAdmin.value = user.email == "admin@example.com"
                fetchPollutionPoints()
            } else {
                isAdmin.value = false
                pollutionPoints.clear()
            }
        }
    }

    fun login(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                errorMessage.value = "Ошибка входа: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                errorMessage.value = "Ошибка регистрации: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun fetchPollutionPoints() {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val snapshot = db.collection("points")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get().await()
                val points = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<PollutionPoint>()?.copy(id = doc.id)
                }
                pollutionPoints.clear()
                pollutionPoints.addAll(points)
            } catch (e: Exception) {
                errorMessage.value = "Ошибка загрузки точек: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addPollutionPoint(context: Context, point: PollutionPoint, imageUri: Uri?) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val imageBase64 = if (imageUri != null) {
                    encodeUriToBase64Compressed(context, imageUri)
                } else {
                    null
                }
                val pointToSave = point.copy(
                    imageUrl = imageBase64,
                    reportedBy = auth.currentUser?.uid ?: "Аноним"
                )
                val documentRef = db.collection("points").add(pointToSave).await()
                pollutionPoints.add(0, pointToSave.copy(id = documentRef.id))
                if (isVolunteer.value) {
                    notificationMessage.value = "Новая точка: ${point.title}"
                }
                newPointCoords.value = null
                newPointDescription.value = ""
                newPointType.value = PollutionType.TRASH
            } catch (e: Exception) {
                errorMessage.value = "Ошибка добавления точки: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updatePollutionPointStatus(pointId: String, status: PollutionStatus) {
        viewModelScope.launch {
            try {
                db.collection("points").document(pointId)
                    .update("status", status).await()
                val index = pollutionPoints.indexOfFirst { it.id == pointId }
                if (index != -1) {
                    pollutionPoints[index] = pollutionPoints[index].copy(status = status)
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка обновления статуса: ${e.message}"
            } finally {
                dismissPointDetails()
            }
        }
    }

    fun deletePoint(pointId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                db.collection("points").document(pointId).delete().await()
                pollutionPoints.removeIf { it.id == pointId }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка удаления точки: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
                dismissPointDetails()
            }
        }
    }

    fun changeLanguage(context: Context, langCode: String) {
        LocaleHelper.setLocale(context, langCode)
    }

    fun exportReports(context: Context) {
        isLoading.value = true
        viewModelScope.launch {
            val csvContent = generateCsvContent(context)
            csvExportContent.value = csvContent
            isLoading.value = false
        }
    }

    fun generateCsvContent(context: Context): String {
        val header = "ID,Тип,Статус,Описание,Широта,Долгота,Фото (Base64)"
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(header)
        pollutionPoints.forEach { point ->
            val description = "\"${point.description.replace("\"", "\"\"")}\""
            val line = listOf(
                point.id,
                context.getString(point.type.displayNameResId),
                context.getString(point.status.displayNameResId),
                description,
                point.latitude.toString(),
                point.longitude.toString(),
                point.imageUrl?.take(20) ?: "N/A"
            ).joinToString(",")
            stringBuilder.appendLine(line)
        }
        return stringBuilder.toString()
    }

    fun onMarkerClick(point: PollutionPoint) { selectedPoint.value = point }
    fun dismissPointDetails() { selectedPoint.value = null }

    fun toggleVolunteerStatus() {
        val newStatus = !isVolunteer.value
        isVolunteer.value = newStatus
        sharedPrefs.edit().putBoolean("is_volunteer", newStatus).apply()
        if (newStatus) {
            Firebase.messaging.subscribeToTopic("new_points")
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) "ПОДПИСКА УСПЕШНА!" else "ОШИБКА ПОДПИСКИ"
                    Log.d("FCM_TOPIC", msg)
                }
        } else {
            Firebase.messaging.unsubscribeFromTopic("new_points")
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) "ОТПИСКА УСПЕШНА!" else "ОШИБКА ОТПИСКИ"
                    Log.d("FCM_TOPIC", msg)
                }
        }
    }

    fun onCsvExported() { csvExportContent.value = null }
    fun clearNotificationMessage() { notificationMessage.value = null }
}

private fun encodeUriToBase64Compressed(context: Context, imageUri: Uri): String? {
    return try {
        val originalBitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
        val maxHeight = 1024.0
        val maxWidth = 1024.0
        val scale = minOf(maxWidth / originalBitmap.width, maxHeight / originalBitmap.height)
        val newWidth = (originalBitmap.width * scale).roundToInt()
        val newHeight = (originalBitmap.height * scale).roundToInt()
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}