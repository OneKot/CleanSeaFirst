package com.cleansea

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.cleansea.util.LocaleHelper

class CleanSeaApplication : Application() {

    // Этот метод вызывается самым первым при запуске приложения
    override fun attachBaseContext(base: Context) {
        // Мы перехватываем базовый контекст и оборачиваем его с помощью нашего хелпера
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    // --- ДОБАВЬТЕ ЭТОТ МЕТОД ---
    // Этот метод вызывается при изменении конфигурации (включая смену языка)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Мы снова применяем нашу логику локализации
        LocaleHelper.onAttach(this)
    }
}