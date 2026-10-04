package com.myledger.app.security

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("myledger_settings")

class AppSettings(private val context: Context) {
    private val pinKey = stringPreferencesKey("pin")
    private val onboardingKey = booleanPreferencesKey("onboarding_complete")
    private val darkKey = booleanPreferencesKey("dark_mode")

    val darkMode = context.settingsDataStore.data.map { it[darkKey] ?: false }

    suspend fun setDark(value:Boolean) { context.settingsDataStore.edit{it[darkKey]=value} }
    suspend fun setOnboardingComplete() { context.settingsDataStore.edit{it[onboardingKey]=true} }
    suspend fun onboardingComplete() = context.settingsDataStore.data.first()[onboardingKey] ?: false
    suspend fun setPin(pin:String) { context.settingsDataStore.edit{it[pinKey]=pin} }
    suspend fun verifyPin(pin:String) = context.settingsDataStore.data.first()[pinKey] == pin
    suspend fun hasPin() = !context.settingsDataStore.data.first()[pinKey].isNullOrBlank()
}
