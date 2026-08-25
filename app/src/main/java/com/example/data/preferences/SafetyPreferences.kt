package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.models.FakeScreenMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.safetyDataStore: DataStore<Preferences> by preferencesDataStore(name = "emergency_safety_prefs")

class SafetyPreferences(private val context: Context) {

    companion object {
        private val KEY_SHAKE_THRESHOLD = floatPreferencesKey("shake_threshold_g")
        private val KEY_FAKE_SCREEN_MODE = stringPreferencesKey("fake_screen_mode")
        private val KEY_AUTO_UPLOAD = booleanPreferencesKey("auto_upload_enabled")
        private val KEY_RECORD_AUDIO = booleanPreferencesKey("record_audio_enabled")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        private val KEY_HIGH_RES_VIDEO = booleanPreferencesKey("high_res_video")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        private val KEY_CALCULATOR_PIN = stringPreferencesKey("calculator_pin")
    }

    val shakeThresholdFlow: Flow<Float> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_SHAKE_THRESHOLD] ?: 2.5f
    }

    val fakeScreenModeFlow: Flow<FakeScreenMode> = context.safetyDataStore.data.map { prefs ->
        val raw = prefs[KEY_FAKE_SCREEN_MODE] ?: FakeScreenMode.CALCULATOR.name
        try {
            FakeScreenMode.valueOf(raw)
        } catch (_: Exception) {
            FakeScreenMode.CALCULATOR
        }
    }

    val autoUploadFlow: Flow<Boolean> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_AUTO_UPLOAD] ?: true
    }

    val recordAudioFlow: Flow<Boolean> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_RECORD_AUDIO] ?: true
    }

    val userIdFlow: Flow<String> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: "user_${UUID.randomUUID().toString().take(8)}"
    }

    val disclaimerAcceptedFlow: Flow<Boolean> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_DISCLAIMER_ACCEPTED] ?: false
    }

    val highResVideoFlow: Flow<Boolean> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_HIGH_RES_VIDEO] ?: true
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_FEEDBACK] ?: true
    }

    val calculatorPinFlow: Flow<String> = context.safetyDataStore.data.map { prefs ->
        prefs[KEY_CALCULATOR_PIN] ?: "1234"
    }

    suspend fun setShakeThreshold(threshold: Float) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_SHAKE_THRESHOLD] = threshold
        }
    }

    suspend fun setFakeScreenMode(mode: FakeScreenMode) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_FAKE_SCREEN_MODE] = mode.name
        }
    }

    suspend fun setAutoUpload(enabled: Boolean) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_AUTO_UPLOAD] = enabled
        }
    }

    suspend fun setRecordAudio(enabled: Boolean) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_RECORD_AUDIO] = enabled
        }
    }

    suspend fun setUserId(uid: String) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = uid
        }
    }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_DISCLAIMER_ACCEPTED] = accepted
        }
    }

    suspend fun setHighResVideo(enabled: Boolean) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_HIGH_RES_VIDEO] = enabled
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setCalculatorPin(pin: String) {
        context.safetyDataStore.edit { prefs ->
            prefs[KEY_CALCULATOR_PIN] = pin
        }
    }
}
