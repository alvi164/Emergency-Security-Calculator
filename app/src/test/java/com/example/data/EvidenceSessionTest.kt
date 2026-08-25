package com.example.data

import com.example.data.models.EvidenceSession
import com.example.data.models.GpsLocation
import com.example.data.models.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceSessionTest {

    @Test
    fun `test evidence session firestore mapping and formatting`() {
        val location = GpsLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            accuracy = 4.5f,
            altitude = 12.0
        )

        val session = EvidenceSession(
            uid = "user_test123",
            timestamp = 1700000000000L,
            backVideoPath = "/storage/evidence_back.mp4",
            frontVideoPath = "/storage/evidence_front.mp4",
            backVideoUrl = "https://storage.googleapis.com/back.mp4",
            frontVideoUrl = "https://storage.googleapis.com/front.mp4",
            backFileSize = 1024 * 1024 * 5, // 5MB
            frontFileSize = 1024 * 1024 * 3, // 3MB
            durationSeconds = 45,
            location = location,
            deviceModel = "Pixel 8 Pro",
            triggerType = "TRIPLE_SHAKE",
            uploadStatus = UploadStatus.UPLOADED
        )

        val map = session.toFirestoreMap()

        assertEquals("user_test123", map["uid"])
        assertEquals("TRIPLE_SHAKE", map["triggerType"])
        assertEquals("Pixel 8 Pro", map["deviceModel"])
        assertEquals(37.7749, map["latitude"])
        assertEquals(-122.4194, map["longitude"])
        assertEquals(4.5f, map["accuracy"])
        assertEquals("UPLOADED", map["status"])
        assertNotNull(map["recordedAtUtc"])

        assertEquals("8.0 MB", session.formattedSize)
        assertTrue(session.formattedTimestamp.isNotEmpty())
    }
}
