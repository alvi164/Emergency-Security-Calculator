package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

class BackCameraRecorder(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val lifecycleOwner = ServiceLifecycleOwner()
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun startRecording(
        outputFile: File,
        recordAudio: Boolean,
        onFinalized: (File, Long, String?) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            lifecycleOwner.start()

            val provider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider = provider
            provider.unbindAll()

            val qualitySelector = QualitySelector.from(
                Quality.FHD,
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .setExecutor(cameraExecutor)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            if (!provider.hasCamera(cameraSelector)) {
                onFinalized(outputFile, 0L, "Back camera not available")
                return@withContext false
            }

            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                videoCapture
            )

            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            var pendingRecording = videoCapture!!.output.prepareRecording(context, outputOptions)

            if (recordAudio && hasAudioPermission()) {
                @SuppressLint("MissingPermission")
                pendingRecording = pendingRecording.withAudioEnabled()
            }

            activeRecording = pendingRecording.start(
                ContextCompat.getMainExecutor(context)
            ) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            onFinalized(outputFile, outputFile.length(), "Finalize error: ${event.error}")
                        } else {
                            onFinalized(outputFile, outputFile.length(), null)
                        }
                    }
                    else -> Unit
                }
            }

            true
        } catch (e: Exception) {
            onFinalized(outputFile, 0L, e.localizedMessage)
            false
        }
    }

    fun stopRecording() {
        try {
            activeRecording?.stop()
            activeRecording?.close()
            activeRecording = null
            cameraProvider?.unbindAll()
            lifecycleOwner.stop()
        } catch (_: Exception) {
            // Ignore stop errors
        }
    }

    fun release() {
        stopRecording()
        cameraExecutor.shutdown()
    }
}
