package com.cleansea.data

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import com.cleansea.R

@Serializable // Используем kotlinx.serialization для парсинга JSON
data class PollutionPoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String, // Краткое описание или тип
    val description: String,
    val type: PollutionType,
    val imageUrl: String? = null,
    val status: PollutionStatus = PollutionStatus.DETECTED, // По умолчанию
    val reportedBy: String = "Аноним",
    val timestamp: Long = System.currentTimeMillis() // Время создания
)

@Serializable
enum class PollutionType(@StringRes val displayNameResId: Int) {
    TRASH(R.string.pollution_type_trash),
    OIL_SPOT(R.string.pollution_type_oil_spot),
    INDUSTRIAL_WASTE(R.string.pollution_type_industrial_waste),
    OTHER(R.string.pollution_type_other)
}

@Serializable
enum class PollutionStatus(@StringRes val displayNameResId: Int) {
    DETECTED(R.string.status_detected),
    IN_PROGRESS(R.string.status_in_progress),
    CLEARED(R.string.status_cleared)
}