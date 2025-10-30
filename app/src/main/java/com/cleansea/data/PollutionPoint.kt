package com.cleansea.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cleansea.R
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PollutionPoint(
    var id: String = "", // ID теперь будет присваиваться Firestore, делаем его var
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var title: String = "", // Сделаем var, если захотим менять
    val description: String = "",
    val type: PollutionType = PollutionType.OTHER,
    val status: PollutionStatus = PollutionStatus.DETECTED,
    val imageUrl: String? = null,
    val reportedBy: String = "Аноним", // В будущем сюда можно подставлять ID пользователя

    // Firestore будет автоматически подставлять время создания на сервере
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Пустой конструктор, необходимый для Firestore для преобразования данных
    constructor() : this("", 0.0, 0.0, "", "", PollutionType.OTHER, PollutionStatus.DETECTED, null)
}


enum class PollutionType(
    @StringRes val displayNameResId: Int,
    @DrawableRes val iconResId: Int
) {
    TRASH(R.string.pollution_type_trash, R.drawable.ic_trash),
    OIL_SPOT(R.string.pollution_type_oil_spot, R.drawable.ic_oil),
    INDUSTRIAL_WASTE(R.string.pollution_type_industrial_waste, R.drawable.ic_factory),
    OTHER(R.string.pollution_type_other, R.drawable.ic_other)
}


enum class PollutionStatus(@StringRes val displayNameResId: Int) {
    DETECTED(R.string.status_detected),
    IN_PROGRESS(R.string.status_in_progress),
    CLEARED(R.string.status_cleared)
}