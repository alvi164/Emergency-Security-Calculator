package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.EmergencyApplication
import com.example.MainActivity
import com.example.R
import com.example.camera.DualCameraManager
import com.example.data.location.LocationHelper
import com.example.data.models.EvidenceSession
import com.example.data.models.FakeScreenMode
import com.example.data.preferences.SafetyPreferences
import com.example.data.repository.EvidenceRepository
import com.example.sensor.ShakeDetector
import com.example.ui.fake.FakeScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmergencyRecorderService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val binder = LocalBinder()

    private var wakeLock: PowerManager.WakeLock? = null
    private var shakeDetector: ShakeDetector? = null
    private var dualCameraManager: DualCameraManager? = null
    private lateinit var preferences: SafetyPreferences
    private lateinit var evidenceRepository: EvidenceRepository
    private lateinit var locationHelper: LocationHelper

    private var durationCollectorJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): EmergencyRecorderService = this@EmergencyRecorderService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        preferences = SafetyPreferences(this)
        evidenceRepository = EvidenceRepository.getInstance(this)
        locationHelper = LocationHelper(this)
        dualCameraManager = DualCameraManager(this, evidenceRepository.getEvidenceDirectory())

        acquireWakeLock()
        initShakeDetector()
        observeSettings()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EmergencyRecorder::SafetyMonitorWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(24 * 60 * 60 * 1000L) // 24 hours max safeguard
        }
    }

    private fun initShakeDetector() {
        shakeDetector = ShakeDetector(this, thresholdG = 2.5f) {
            handleShakeTriggered()
        }
        shakeDetector?.startListening()
        _isMonitoring.value = true
    }

    private fun observeSettings() {
        serviceScope.launch {
            preferences.shakeThresholdFlow.collect { threshold ->
                shakeDetector?.updateThreshold(threshold)
            }
        }
    }

    private fun handleShakeTriggered() {
        serviceScope.launch {
            if (_isRecording.value) {
                // If already recording, a triple shake acts as a stop toggle
                stopRecordingSession()
            } else {
                // Start emergency recording session
                startRecordingSession(triggerType = "TRIPLE_SHAKE")
            }
        }
    }

    suspend fun startRecordingSession(triggerType: String = "MANUAL") {
        if (_isRecording.value) return

        val hapticEnabled = preferences.hapticFeedbackFlow.first()
        if (hapticEnabled) {
            triggerSubtleHaptic()
        }

        val recordAudio = preferences.recordAudioFlow.first()
        val timestamp = System.currentTimeMillis()

        // Update foreground service notification to recording state
        updateNotification(isRecording = true)

        // Launch fake discreet screen
        val fakeMode = preferences.fakeScreenModeFlow.first()
        launchFakeScreen(fakeMode)

        val success = dualCameraManager?.startDualRecording(
            recordAudio = recordAudio,
            sessionTimestamp = timestamp
        ) ?: false

        if (success) {
            _isRecording.value = true
            _currentSessionTimestamp.value = timestamp
            _lastTriggerType.value = triggerType

            durationCollectorJob?.cancel()
            durationCollectorJob = serviceScope.launch {
                dualCameraManager?.durationSeconds?.collect { sec ->
                    _currentDurationSeconds.value = sec
                    if (sec % 5L == 0L && sec > 0) {
                        updateNotification(isRecording = true, durationSec = sec)
                    }
                }
            }
        }
    }

    suspend fun stopRecordingSession(): EvidenceSession? {
        if (!_isRecording.value) return null

        val hapticEnabled = preferences.hapticFeedbackFlow.first()
        if (hapticEnabled) {
            triggerSubtleHaptic()
        }

        durationCollectorJob?.cancel()
        durationCollectorJob = null

        val timestamp = _currentSessionTimestamp.value
        val duration = _currentDurationSeconds.value
        val triggerType = _lastTriggerType.value

        val cameraResult = dualCameraManager?.stopDualRecording()
        _isRecording.value = false
        _currentDurationSeconds.value = 0L

        // Fetch location
        val location = locationHelper.getCurrentLocation()
        val uid = preferences.userIdFlow.first()

        val session = evidenceRepository.createEvidenceSession(
            uid = uid,
            timestamp = timestamp,
            backVideoPath = cameraResult?.backVideoFile?.absolutePath,
            frontVideoPath = cameraResult?.frontVideoFile?.absolutePath,
            durationSeconds = duration,
            location = location,
            triggerType = triggerType
        )

        // Automatic Cloud Upload if enabled
        val autoUpload = preferences.autoUploadFlow.first()
        if (autoUpload) {
            evidenceRepository.enqueueUpload(session.id)
        }

        updateNotification(isRecording = false)
        return session
    }

    private fun triggerSubtleHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
                }
            }
        } catch (_: Exception) {}
    }

    private fun launchFakeScreen(mode: FakeScreenMode) {
        try {
            val intent = Intent(this, FakeScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(FakeScreenActivity.EXTRA_MODE, mode.name)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun buildNotification(isRecording: Boolean, durationSec: Long = 0L): Notification {
        // Tapping the notification should ALWAYS go to the Calculator (FakeScreenActivity)
        // to enforce PIN entry.
        val openIntent = Intent(this, FakeScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(FakeScreenActivity.EXTRA_MODE, FakeScreenMode.CALCULATOR.name)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, EmergencyRecorderService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val discreetIntent = Intent(this, FakeScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val discreetPendingIntent = PendingIntent.getActivity(
            this,
            103,
            discreetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, EmergencyApplication.CHANNEL_EMERGENCY_SERVICE)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (isRecording) {
            builder.setSmallIcon(R.drawable.ic_green_dot)
                .setContentTitle("Calculating")
                .setContentText("Background process active") // Neutral text for better camouflage
                .setColor(0xFF4CAF50.toInt()) // Green color

            // Determine if device is locked
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            val isLocked = keyguardManager.isKeyguardLocked

            if (!isLocked) {
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Terminate",
                    stopPendingIntent
                )
            }
            
            builder.setContentIntent(openPendingIntent)
        } else {
            builder.setSmallIcon(R.drawable.ic_emergency_shield)
                .setContentTitle("Safety Monitor Active")
                .setColor(0xFF1E88E5.toInt())
                .setContentIntent(openPendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_camera,
                    "Discreet Screen",
                    discreetPendingIntent
                )
        }

        return builder.build()
    }

    private fun updateNotification(isRecording: Boolean, durationSec: Long = 0L) {
        val notification = buildNotification(isRecording, durationSec)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_RECORDING -> {
                serviceScope.launch {
                    stopRecordingSession()
                }
            }
            ACTION_TRIGGER_RECORDING -> {
                serviceScope.launch {
                    startRecordingSession(triggerType = "NOTIFICATION_ACTION")
                }
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = buildNotification(_isRecording.value, _currentDurationSeconds.value)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        shakeDetector?.stopListening()
        shakeDetector = null
        dualCameraManager?.release()
        dualCameraManager = null

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        _isMonitoring.value = false
        _isRecording.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_MONITORING = "com.example.action.START_MONITORING"
        const val ACTION_TRIGGER_RECORDING = "com.example.action.TRIGGER_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.action.STOP_RECORDING"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"

        private val _isMonitoring = MutableStateFlow(false)
        val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

        private val _isRecording = MutableStateFlow(false)
        val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

        private val _currentDurationSeconds = MutableStateFlow(0L)
        val currentDurationSeconds: StateFlow<Long> = _currentDurationSeconds.asStateFlow()

        private val _currentSessionTimestamp = MutableStateFlow(0L)
        val currentSessionTimestamp: StateFlow<Long> = _currentSessionTimestamp.asStateFlow()

        private val _lastTriggerType = MutableStateFlow("TRIPLE_SHAKE")
        val lastTriggerType: StateFlow<String> = _lastTriggerType.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, EmergencyRecorderService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun triggerRecording(context: Context) {
            val intent = Intent(context, EmergencyRecorderService::class.java).apply {
                action = ACTION_TRIGGER_RECORDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, EmergencyRecorderService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EmergencyRecorderService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
