package com.example.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShakeAlgorithmTest {

    private lateinit var algorithm: ShakeAlgorithm

    @Before
    fun setUp() {
        algorithm = ShakeAlgorithm(
            thresholdG = 2.5f,
            windowDurationMs = 1000L,
            requiredSpikeCount = 3,
            debounceDurationMs = 3000L,
            minPeakIntervalMs = 100L
        )
    }

    @Test
    fun `triple shake within 1 second triggers emergency event`() {
        var baseTime = 10000L

        // Spike 1
        val res1 = algorithm.processSample(gForce = 2.8f, nowMs = baseTime)
        assertFalse(res1)
        assertEquals(1, algorithm.getRecentSpikeCount(baseTime))

        // Spike 2 (200ms later)
        baseTime += 200L
        val res2 = algorithm.processSample(gForce = 3.1f, nowMs = baseTime)
        assertFalse(res2)
        assertEquals(2, algorithm.getRecentSpikeCount(baseTime))

        // Spike 3 (200ms later - total 400ms < 1000ms)
        baseTime += 200L
        val res3 = algorithm.processSample(gForce = 2.9f, nowMs = baseTime)
        assertTrue(res3)
    }

    @Test
    fun `spikes below threshold do not trigger`() {
        var baseTime = 10000L

        val res1 = algorithm.processSample(gForce = 1.8f, nowMs = baseTime)
        baseTime += 200L
        val res2 = algorithm.processSample(gForce = 2.1f, nowMs = baseTime)
        baseTime += 200L
        val res3 = algorithm.processSample(gForce = 2.4f, nowMs = baseTime)

        assertFalse(res1)
        assertFalse(res2)
        assertFalse(res3)
        assertEquals(0, algorithm.getRecentSpikeCount(baseTime))
    }

    @Test
    fun `spikes separated by more than 1 second do not trigger`() {
        var baseTime = 10000L

        // Spike 1 at 10.0s
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)

        // Spike 2 at 10.6s
        baseTime += 600L
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)

        // Spike 3 at 11.2s (> 1000ms from Spike 1, Spike 1 has expired)
        baseTime += 600L
        val res3 = algorithm.processSample(gForce = 3.0f, nowMs = baseTime)

        assertFalse(res3)
        // Spikes at 10.6s and 11.2s remain in window
        assertEquals(2, algorithm.getRecentSpikeCount(baseTime))
    }

    @Test
    fun `debounce blocks re-triggering for 3 seconds`() {
        var baseTime = 10000L

        // Initial 3 spikes -> triggers
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        baseTime += 150L
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        baseTime += 150L
        val triggered = algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        assertTrue(triggered)

        // Immediate subsequent spikes within 3000ms debounce
        baseTime += 500L
        val debounceBlocked1 = algorithm.processSample(gForce = 3.5f, nowMs = baseTime)
        assertFalse(debounceBlocked1)

        baseTime += 1000L // 1.5s after trigger
        val debounceBlocked2 = algorithm.processSample(gForce = 3.5f, nowMs = baseTime)
        assertFalse(debounceBlocked2)

        // 3.1s after trigger (debounce expired) -> can trigger again
        baseTime += 1600L // Total 3.1s elapsed
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        baseTime += 150L
        algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        baseTime += 150L
        val retriggered = algorithm.processSample(gForce = 3.0f, nowMs = baseTime)
        assertTrue(retriggered)
    }
}
