package com.example.camera

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class DualRecordingResult(
    val timestamp: Long,
    val backVideoFile: File?,
    val frontVideoFile: File?,
    val durationSeconds: Long,
    val backSuccess: Boolean,
    val frontSuccess: Boolean,
    val errorMessages: List<String>
)

class DualCameraManager(
    private val context: Context,
    private val storageDir: File
) {
    private val backRecorder = BackCameraRecorder(context)
    private val frontRecorder = FrontCameraRecorder(context)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private var durationJob: Job? = null
    private var currentSessionTimestamp: Long = 0L
    private var currentBackFile: File? = null
    private var currentFrontFile: File? = null

    private var backFinalizedLength: Long = 0L
    private var frontFinalizedLength: Long = 0L
    private var backError: String? = null
    private var frontError: String? = null

    suspend fun startDualRecording(
        recordAudio: Boolean,
        sessionTimestamp: Long = System.currentTimeMillis()
    ): Boolean {
        if (_isRecording.value) return true

        currentSessionTimestamp = sessionTimestamp
        backFinalizedLength = 0L
        frontFinalizedLength = 0L
        backError = null
        frontError = null
        _durationSeconds.value = 0L

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val backFile = File(storageDir, "evidence_${sessionTimestamp}_back.mp4")
        val frontFile = File(storageDir, "evidence_${sessionTimestamp}_front.mp4")
        currentBackFile = backFile
        currentFrontFile = frontFile

        val backStarted = backRecorder.startRecording(backFile, recordAudio) { file, len, err ->
            backFinalizedLength = len
            backError = err
        }

        val frontStarted = frontRecorder.startRecording(frontFile) { file, len, err ->
            frontFinalizedLength = len
            frontError = err
        }

        val started = backStarted || frontStarted
        _isRecording.value = started

        if (started) {
            startDurationTimer()
        }

        return started
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            while (isActive && _isRecording.value) {
                delay(500L)
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000L
                _durationSeconds.value = elapsedSec
            }
        }
    }

    suspend fun stopDualRecording(): DualRecordingResult {
        durationJob?.cancel()
        durationJob = null

        val finalDuration = _durationSeconds.value
        _isRecording.value = false

        // Stop back camera
        backRecorder.stopRecording()

        // Stop front camera
        currentFrontFile?.let { frontFile ->
            frontRecorder.stopRecording(frontFile) { _, len, err ->
                frontFinalizedLength = len
                frontError = err
            }
        }

        // Small delay to allow file flushes
        delay(400L)

        val backFile = currentBackFile?.takeIf { it.exists() && it.length() > 0 }
        val frontFile = currentFrontFile?.takeIf { it.exists() && it.length() > 0 }

        val errors = mutableListOf<String>()
        backError?.let { errors.add("Back camera: $it") }
        frontError?.let { errors.add("Front camera: $it") }

        val result = DualRecordingResult(
            timestamp = currentSessionTimestamp,
            backVideoFile = backFile,
            frontVideoFile = frontFile,
            durationSeconds = finalDuration,
            backSuccess = backFile != null,
            frontSuccess = frontFile != null,
            errorMessages = errors
        )

        currentBackFile = null
        currentFrontFile = null

        return result
    }

    fun release() {
        durationJob?.cancel()
        backRecorder.release()
        frontRecorder.release()
    }
}
