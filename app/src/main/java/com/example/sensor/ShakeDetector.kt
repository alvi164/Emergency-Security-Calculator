package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Pure algorithm for Triple-Shake detection to enable deterministic unit testing and runtime execution.
 *
 * Requirements:
 * - Detects 3 high-G spikes > threshold (default 2.5g) within 1 second window.
 * - Minimum peak separation (e.g., 100ms) to ensure true separate shake cycles rather than single continuous impulse.
 * - 3-second debounce lockout after a successful trigger.
 */
class ShakeAlgorithm(
    var thresholdG: Float = 2.5f,
    var windowDurationMs: Long = 1000L,
    var requiredSpikeCount: Int = 3,
    var debounceDurationMs: Long = 3000L,
    var minPeakIntervalMs: Long = 100L
) {
    private val spikeTimestamps = mutableListOf<Long>()
    private var lastTriggerTimeMs: Long = 0L
    private var lastSpikeTimeMs: Long = 0L

    /**
     * Process an acceleration sample with timestamp in milliseconds.
     * Returns true if a Triple-Shake event is triggered.
     */
    fun processSample(gForce: Float, nowMs: Long): Boolean {
        // 1. Debounce check
        if (nowMs - lastTriggerTimeMs < debounceDurationMs) {
            return false
        }

        // 2. High-G threshold check
        if (gForce >= thresholdG) {
            // Check minimum interval between distinct spikes
            if (nowMs - lastSpikeTimeMs >= minPeakIntervalMs) {
                lastSpikeTimeMs = nowMs
                spikeTimestamps.add(nowMs)
            }
        }

        // 3. Purge spikes outside the sliding window
        val windowCutoff = nowMs - windowDurationMs
        spikeTimestamps.removeAll { it < windowCutoff }

        // 4. Trigger condition
        if (spikeTimestamps.size >= requiredSpikeCount) {
            lastTriggerTimeMs = nowMs
            spikeTimestamps.clear()
            return true
        }

        return false
    }

    fun getRecentSpikeCount(nowMs: Long): Int {
        val windowCutoff = nowMs - windowDurationMs
        spikeTimestamps.removeAll { it < windowCutoff }
        return spikeTimestamps.size
    }

    fun reset() {
        spikeTimestamps.clear()
        lastSpikeTimeMs = 0L
        lastTriggerTimeMs = 0L
    }
}

/**
 * Android SensorManager wrapper integrating ShakeAlgorithm with accelerometer hardware events.
 */
class ShakeDetector(
    context: Context,
    thresholdG: Float = 2.5f,
    private val onShakeTriggered: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val algorithm = ShakeAlgorithm(thresholdG = thresholdG)

    private val _currentGForce = MutableStateFlow(1.0f)
    val currentGForce: StateFlow<Float> = _currentGForce.asStateFlow()

    private val _recentSpikes = MutableStateFlow(0)
    val recentSpikes: StateFlow<Int> = _recentSpikes.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening || accelerometer == null) return
        algorithm.reset()
        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        algorithm.reset()
        _recentSpikes.value = 0
    }

    fun updateThreshold(newThresholdG: Float) {
        algorithm.thresholdG = newThresholdG
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate magnitude in m/s^2 and convert to Gs (1g ≈ 9.80665 m/s^2)
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = magnitude / SensorManager.GRAVITY_EARTH

        _currentGForce.value = gForce

        val nowMs = System.currentTimeMillis()
        val triggered = algorithm.processSample(gForce, nowMs)
        _recentSpikes.value = algorithm.getRecentSpikeCount(nowMs)

        if (triggered) {
            _recentSpikes.value = 0
            onShakeTriggered()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
