package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.sqrt

class StepTracker(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepsFlow = MutableSharedFlow<Int>(extraBufferCapacity = 10)
    val stepsFlow: SharedFlow<Int> = _stepsFlow

    private var initialStepCount = -1f
    private var lastAccelerometerTime: Long = 0
    private var lastShakeForce = 0f
    private val SHAKE_THRESHOLD = 13.0f // acceleration threshold for a step in m/s^2

    // Anti-Cheat Variables
    private var lastStepTime: Long = 0
    private val MIN_TIME_BETWEEN_STEPS_MS = 250 // Max ~4 steps/sec for realistic human walk/run
    private val _antiCheatTriggered = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val antiCheatTriggered: SharedFlow<String> = _antiCheatTriggered

    fun startTracking() {
        // Register hardware step counter (preferred, low battery drain)
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
        // Register accelerometer as fallback (detects pattern of steps via movement shakes)
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val currentTime = System.currentTimeMillis()

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]
            if (initialStepCount < 0) {
                initialStepCount = totalSteps
            } else {
                val delta = (totalSteps - initialStepCount).toInt()
                if (delta > 0) {
                    if (detectCheating(currentTime)) {
                        return
                    }
                    initialStepCount = totalSteps
                    _stepsFlow.tryEmit(delta)
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // Calculate total acceleration magnitude
            val gForce = sqrt(x * x + y * y + z * z)
            if (gForce > SHAKE_THRESHOLD) {
                if (currentTime - lastAccelerometerTime > 350) { // filter out minor vibrations
                    if (detectCheating(currentTime)) {
                        return
                    }
                    lastAccelerometerTime = currentTime
                    _stepsFlow.tryEmit(1)
                }
            }
        }
    }

    private fun detectCheating(currentTime: Long): Boolean {
        val timeDiff = currentTime - lastStepTime
        if (lastStepTime != 0L && timeDiff < MIN_TIME_BETWEEN_STEPS_MS) {
            // Rapid consecutive steps detected! Flags suspicious shaking / speed hack
            _antiCheatTriggered.tryEmit("SUSPICIOUS_SPEED")
            lastStepTime = currentTime
            return true // reject step
        }
        lastStepTime = currentTime
        return false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    // Dynamic Walk Simulator (allows users to walk on treadmill/workout mode inside the app)
    // with velocity anti-cheat built-in
    fun simulateWalkSteps(count: Int, simulatedSpeedKmh: Float) {
        if (simulatedSpeedKmh > 15.0f) {
            // Cheating detected: simulation is too fast for standard running/walking
            _antiCheatTriggered.tryEmit("SIMULATOR_SPEED_EXCEEDED")
            return
        }
        _stepsFlow.tryEmit(count)
    }
}
