package com.example.ui.fake

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.MainActivity
import com.example.data.models.FakeScreenMode
import com.example.data.preferences.SafetyPreferences
import com.example.service.EmergencyRecorderService
import com.example.ui.theme.EmergencyRecorderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FakeScreenActivity : ComponentActivity() {

    private lateinit var preferences: SafetyPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        preferences = SafetyPreferences(this)
        val initialModeStr = intent.getStringExtra(EXTRA_MODE)
        val defaultMode = initialModeStr?.let {
            try {
                FakeScreenMode.valueOf(it)
            } catch (_: Exception) {
                null
            }
        } ?: runBlocking { preferences.fakeScreenModeFlow.first() }

        setContent {
            EmergencyRecorderTheme {
                val isRecording by EmergencyRecorderService.isRecording.collectAsState()
                val currentMode by preferences.fakeScreenModeFlow.collectAsState(initial = defaultMode)
                val calculatorPin by preferences.calculatorPinFlow.collectAsState(initial = "1234")

                if (currentMode == FakeScreenMode.BLACK_SCREEN) {
                    FakeBlackScreen(
                        isRecording = isRecording,
                        onStopRecording = {
                            EmergencyRecorderService.stopRecording(this@FakeScreenActivity)
                        },
                        onExitToMain = {
                            val mainIntent = Intent(this@FakeScreenActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(mainIntent)
                            finish()
                        }
                    )
                } else {
                    FakeCalculatorScreen(
                        isRecording = isRecording,
                        calculatorPin = calculatorPin,
                        onStopRecording = {
                            EmergencyRecorderService.stopRecording(this@FakeScreenActivity)
                        },
                        onExitToMain = {
                            val mainIntent = Intent(this@FakeScreenActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(mainIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_MODE = "extra_fake_screen_mode"
    }
}
