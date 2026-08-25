package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FrontCameraRecorder(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var isRecording = false

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("FrontCameraBackground").apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (_: Exception) {}
    }

    private fun getFrontCameraId(): String? {
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return id
                }
            }
        } catch (_: Exception) {}
        return null
    }

    @SuppressLint("MissingPermission")
    suspend fun startRecording(
        outputFile: File,
        onFinalized: (File, Long, String?) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val frontId = getFrontCameraId()
        if (frontId == null) {
            onFinalized(outputFile, 0L, "Front camera not found")
            return@withContext false
        }

        startBackgroundThread()

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder = recorder

            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            recorder.setVideoSize(1280, 720)
            recorder.setVideoFrameRate(30)
            recorder.setVideoEncodingBitRate(6_000_000)
            recorder.setOutputFile(outputFile.absolutePath)
            // Rotate -90 degrees (equivalent to 270 clockwise)
            recorder.setOrientationHint(270)

            recorder.prepare()
            val recorderSurface = recorder.surface

            cameraManager.openCamera(frontId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    try {
                        val surfaces = listOf(recorderSurface)

                        @Suppress("DEPRECATION")
                        camera.createCaptureSession(
                            surfaces,
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    captureSession = session
                                    try {
                                        val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                                        builder.addTarget(recorderSurface)
                                        session.setRepeatingRequest(
                                            builder.build(),
                                            null,
                                            backgroundHandler
                                        )

                                        mediaRecorder?.start()
                                        isRecording = true
                                    } catch (e: Exception) {
                                        onFinalized(outputFile, 0L, "Session start error: ${e.message}")
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    onFinalized(outputFile, 0L, "Front camera session configuration failed")
                                }
                            },
                            backgroundHandler
                        )
                    } catch (e: Exception) {
                        onFinalized(outputFile, 0L, "Camera session error: ${e.message}")
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    onFinalized(outputFile, 0L, "Camera device error: $error")
                }
            }, backgroundHandler)

            true
        } catch (e: Exception) {
            onFinalized(outputFile, 0L, e.localizedMessage)
            false
        }
    }

    fun stopRecording(outputFile: File, onFinalized: (File, Long, String?) -> Unit) {
        try {
            if (isRecording) {
                try {
                    captureSession?.stopRepeating()
                    captureSession?.close()
                } catch (_: Exception) {}

                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.reset()
                    mediaRecorder?.release()
                } catch (_: Exception) {}

                try {
                    cameraDevice?.close()
                } catch (_: Exception) {}

                cameraDevice = null
                captureSession = null
                mediaRecorder = null
                isRecording = false

                val length = if (outputFile.exists()) outputFile.length() else 0L
                onFinalized(outputFile, length, null)
            }
        } catch (e: Exception) {
            onFinalized(outputFile, outputFile.length(), e.localizedMessage)
        } finally {
            stopBackgroundThread()
        }
    }

    fun release() {
        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
        } catch (_: Exception) {}
        try {
            mediaRecorder?.release()
            cameraDevice?.close()
        } catch (_: Exception) {}
        stopBackgroundThread()
    }
}
