package com.example.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class UploadStatus {
    RECORDED_LOCAL,
    UPLOADING,
    UPLOADED,
    QUEUED_OFFLINE,
    FAILED
}

enum class FakeScreenMode {
    CALCULATOR,
    BLACK_SCREEN
}

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double = 0.0
) {
    fun toFormattedString(): String {
        return "%.5f, %.5f (±%.1fm)".format(Locale.US, latitude, longitude, accuracy)
    }
}

data class EvidenceSession(
    val id: String = UUID.randomUUID().toString(),
    val uid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val backVideoPath: String? = null,
    val frontVideoPath: String? = null,
    val backVideoUrl: String? = null,
    val frontVideoUrl: String? = null,
    val backFileSize: Long = 0L,
    val frontFileSize: Long = 0L,
    val durationSeconds: Long = 0L,
    val location: GpsLocation? = null,
    val deviceModel: String = "",
    val triggerType: String = "TRIPLE_SHAKE", // "TRIPLE_SHAKE" | "MANUAL" | "TEST"
    val uploadStatus: UploadStatus = UploadStatus.RECORDED_LOCAL,
    val errorMessage: String? = null
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val totalFileSizeBytes: Long
        get() = backFileSize + frontFileSize

    val formattedSize: String
        get() {
            val kb = totalFileSizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) "%.1f MB".format(Locale.US, mb) else "%.0f KB".format(Locale.US, kb)
        }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "uid" to uid,
            "timestamp" to timestamp,
            "formattedTime" to formattedTimestamp,
            "deviceModel" to deviceModel,
            "triggerType" to triggerType,
            "durationSeconds" to durationSeconds,
            "backVideoUrl" to backVideoUrl,
            "frontVideoUrl" to frontVideoUrl,
            "totalSizeBytes" to totalFileSizeBytes,
            "latitude" to location?.latitude,
            "longitude" to location?.longitude,
            "accuracy" to location?.accuracy,
            "altitude" to location?.altitude,
            "status" to uploadStatus.name,
            "recordedAtUtc" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(Date(timestamp))
        )
    }
}
