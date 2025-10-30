package com.cleansea.util // Можете создать папку util для этого файла

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Сохраняем выбор пользователя и просим систему применить его
    fun setLocale(context: Context, languageCode: String) {
        persist(context, languageCode)
        updateSystemLocale(languageCode)
    }

    // Эта функция будет "оборачивать" наш Context в нужную локаль
    fun onAttach(context: Context): ContextWrapper {
        val lang = getPersistedLocale(context)
        return updateResources(context, lang)
    }

    private fun getPersistedLocale(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Возвращаем сохраненный язык или язык системы по умолчанию
        return prefs.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().language
    }

    private fun persist(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    private fun updateSystemLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Главная "магия" происходит здесь
    private fun updateResources(context: Context, language: String): ContextWrapper {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }
}