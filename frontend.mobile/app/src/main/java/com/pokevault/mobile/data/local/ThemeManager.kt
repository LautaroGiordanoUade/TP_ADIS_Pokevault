package com.pokevault.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val DarkModeKey = booleanPreferencesKey("dark_mode_enabled")

    /**
     * Flow that emits the current dark mode preference.
     * Null means "follow system default".
     */
    val isDarkMode: Flow<Boolean?> = context.themeDataStore.data.map { preferences ->
        preferences[DarkModeKey]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DarkModeKey] = enabled
        }
    }

    suspend fun clearDarkMode() {
        context.themeDataStore.edit { preferences ->
            preferences.remove(DarkModeKey)
        }
    }
}
