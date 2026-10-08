package com.example.alfajr.core.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaState(
    val qiblaBearing: Float = 0f,          // Absolute Qibla compass bearing (0-360°)
    val compassHeading: Float = 0f,        // Current device compass heading (0-360°)
    val relativeAngle: Float = 0f,         // Difference to turn towards Kaaba (-180° to +180°)
    val distanceKm: Int = 0,               // Great-circle distance to Kaaba in km
    val isFacingQibla: Boolean = false,    // Within ±6 degrees of Qibla
    val hasCompassSensor: Boolean = false, // True if active physical sensor is reporting
    val isManualMode: Boolean = false,     // True if user adjusted manually
    val cardinalDirection: String = "NORTH",
    val turnDirectionHint: String = "POINTING NORTH"
)

/**
 * High-precision Qibla direction calculator and sensor manager.
 * Implements great-circle spherical geodesics to the Holy Kaaba in Makkah.
 * Supports multiple sensor hardware levels (Rotation Vector, Geomagnetic, Orientation, Accel+Mag)
 * and seamless interactive manual heading adjustment for devices without magnetometer.
 */
class QiblaEngine(context: Context) : SensorEventListener {

    companion object {
        const val KAABA_LAT = 21.4225
        const val KAABA_LNG = 39.8262
        private const val EARTH_RADIUS_KM = 6371.0
        private const val DEG_TO_RAD = Math.PI / 180.0
        private const val RAD_TO_DEG = 180.0 / Math.PI

        fun calculateQiblaBearing(lat: Double, lng: Double): Float {
            // If at Kaaba, bearing is 0
            val dLat = Math.abs(lat - KAABA_LAT)
            val dLngDiff = Math.abs(lng - KAABA_LNG)
            if (dLat < 0.01 && dLngDiff < 0.01) return 0f

            val lat1 = lat * DEG_TO_RAD
            val lng1 = lng * DEG_TO_RAD
            val lat2 = KAABA_LAT * DEG_TO_RAD
            val lng2 = KAABA_LNG * DEG_TO_RAD

            val dLng = lng2 - lng1
            val y = sin(dLng) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
            var b = (atan2(y, x) * RAD_TO_DEG).toFloat()
            if (b < 0) b += 360f
            return b
        }

        fun calculateDistanceToKaaba(lat: Double, lng: Double): Int {
            val dLat = (KAABA_LAT - lat) * DEG_TO_RAD
            val dLng = (KAABA_LNG - lng) * DEG_TO_RAD
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(lat * DEG_TO_RAD) * cos(KAABA_LAT * DEG_TO_RAD) *
                    sin(dLng / 2) * sin(dLng / 2)
            val c = 2 * asin(sqrt(a))
            return (EARTH_RADIUS_KM * c).toInt()
        }

        fun getCardinalDirection(bearing: Float): String = when {
            bearing in 337.5f..360f || bearing in 0f..22.5f -> "NORTH"
            bearing in 22.5f..67.5f -> "NORTH-EAST"
            bearing in 67.5f..112.5f -> "EAST"
            bearing in 112.5f..157.5f -> "SOUTH-EAST"
            bearing in 157.5f..202.5f -> "SOUTH"
            bearing in 202.5f..247.5f -> "SOUTH-WEST"
            bearing in 247.5f..292.5f -> "WEST"
            bearing in 292.5f..337.5f -> "NORTH-WEST"
            else -> "NORTH"
        }
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val geomagneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    @Suppress("DEPRECATION")
    private val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _qiblaState = MutableStateFlow(QiblaState())
    val qiblaState: StateFlow<QiblaState> = _qiblaState.asStateFlow()

    private var currentLat: Double = KAABA_LAT
    private var currentLng: Double = KAABA_LNG
    private var manualHeadingOffset: Float = 0f
    private var isManualMode: Boolean = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccel = FloatArray(3)
    private val lastMag = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    private var isListening = false
    private var smoothedHeading = 0f

    init {
        val hasSensorHardware = rotationSensor != null ||
                geomagneticSensor != null ||
                orientationSensor != null ||
                (accelSensor != null && magSensor != null)
        _qiblaState.value = _qiblaState.value.copy(hasCompassSensor = hasSensorHardware)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLng = longitude
        recalculateQibla()
    }

    fun rotateManual(deltaDegrees: Float) {
        isManualMode = true
        var newHeading = (_qiblaState.value.compassHeading + deltaDegrees) % 360f
        if (newHeading < 0) newHeading += 360f
        manualHeadingOffset = newHeading
        updateWithHeading(newHeading, manual = true)
    }

    fun setManualHeading(heading: Float) {
        isManualMode = true
        var h = heading % 360f
        if (h < 0) h += 360f
        manualHeadingOffset = h
        updateWithHeading(h, manual = true)
    }

    fun resetToSensors() {
        isManualMode = false
        manualHeadingOffset = 0f
        recalculateQibla()
    }

    private fun recalculateQibla() {
        val qBearing = calculateQiblaBearing(currentLat, currentLng)
        val dist = calculateDistanceToKaaba(currentLat, currentLng)
        val heading = if (isManualMode) manualHeadingOffset else smoothedHeading
        updateWithHeading(heading, manual = isManualMode, qBearing = qBearing, dist = dist)
    }

    private fun updateWithHeading(
        heading: Float,
        manual: Boolean,
        qBearing: Float = calculateQiblaBearing(currentLat, currentLng),
        dist: Int = calculateDistanceToKaaba(currentLat, currentLng)
    ) {
        var diff = qBearing - heading
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f

        val isFacing = if (dist == 0) true else kotlin.math.abs(diff) <= 6.0f
        val cardinal = if (dist == 0) "LOCAL" else getCardinalDirection(qBearing)

        val hint = when {
            dist == 0 -> "AT THE HOLY KAABA"
            isFacing -> "★ FACING THE KAABA ★"
            diff > 0 -> "TURN ${diff.toInt()}° RIGHT ➔"
            else -> "⬅ TURN ${(-diff).toInt()}° LEFT"
        }

        val hasSensor = rotationSensor != null ||
                geomagneticSensor != null ||
                orientationSensor != null ||
                (accelSensor != null && magSensor != null)

        _qiblaState.value = _qiblaState.value.copy(
            qiblaBearing = qBearing,
            compassHeading = heading,
            relativeAngle = diff,
            distanceKm = dist,
            isFacingQibla = isFacing,
            hasCompassSensor = hasSensor,
            isManualMode = manual,
            cardinalDirection = cardinal,
            turnDirectionHint = hint
        )
    }

    fun startListening() {
        if (isListening || sensorManager == null) return

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } else if (geomagneticSensor != null) {
            sensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } else if (orientationSensor != null) {
            @Suppress("DEPRECATION")
            sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } else if (accelSensor != null && magSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        } else {
            recalculateQibla()
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || isManualMode) return

        var rawAzimuth: Float? = null

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = (orientationAngles[0] * RAD_TO_DEG).toFloat()
                if (azimuth < 0) azimuth += 360f
                rawAzimuth = azimuth
            }
            @Suppress("DEPRECATION")
            Sensor.TYPE_ORIENTATION -> {
                var azimuth = event.values[0]
                if (azimuth < 0) azimuth += 360f
                rawAzimuth = azimuth
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccel, 0, 3)
                hasAccel = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMag, 0, 3)
                hasMag = true
            }
        }

        if (rawAzimuth == null && hasAccel && hasMag) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccel, lastMag)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = (orientationAngles[0] * RAD_TO_DEG).toFloat()
                if (azimuth < 0) azimuth += 360f
                rawAzimuth = azimuth
            }
        }

        if (rawAzimuth != null) {
            // Low-pass filter to smooth out sensor jitter
            smoothedHeading = smoothHeading(smoothedHeading, rawAzimuth, 0.25f)
            updateWithHeading(smoothedHeading, manual = false)
        }
    }

    private fun smoothHeading(oldH: Float, newH: Float, alpha: Float): Float {
        var diff = newH - oldH
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        var result = oldH + alpha * diff
        if (result < 0) result += 360f
        if (result >= 360f) result -= 360f
        return result
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
