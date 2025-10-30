package com.cleansea.data

import kotlinx.serialization.Serializable

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
enum class PollutionType(val displayName: String) {
    TRASH("Мусор"),
    OIL_SPOT("Нефтяное пятно"),
    INDUSTRIAL_WASTE("Промышленные отходы"),
    OTHER("Другое")
}

@Serializable
enum class PollutionStatus(val displayName: String) {
    DETECTED("Обнаружено"),
    IN_PROGRESS("В работе"),
    CLEARED("Очищено")
}