package com.pokevault.mobile.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        // Inicializa el idioma por defecto a español si no se ha configurado previamente.
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) {
            setLanguage("es")
        }
    }

    /**
     * Sets the application language dynamically.
     * AppCompatDelegate will automatically store this setting to local storage
     * due to the autoStoreLocales configuration in AndroidManifest.
     */
    fun setLanguage(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Returns the currently configured application language code ("es" or "en").
     * Defaults to "es" (español) if no custom locale is set.
     */
    fun getCurrentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val tag = locales.get(0)?.language
            if (!tag.isNullOrBlank()) {
                return tag
            }
        }
        return "es"
    }
}
