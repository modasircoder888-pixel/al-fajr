package com.example.alfajr.core.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false
)

/**
 * Lightweight, zero-polling battery state observer.
 * Uses Android's sticky ACTION_BATTERY_CHANGED intent.
 * Designed for low-power smartwatch hardware to prevent unnecessary CPU wakeups.
 */
class BatteryMonitor(private val context: Context) {

    private val _batteryState = MutableStateFlow(getInitialBatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateFromIntent(intent)
            }
        }
    }

    private fun getInitialBatteryInfo(): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(null, filter)
        return parseBatteryIntent(stickyIntent)
    }

    private fun updateFromIntent(intent: Intent?) {
        val info = parseBatteryIntent(intent)
        _batteryState.value = info
    }

    private fun parseBatteryIntent(intent: Intent?): BatteryInfo {
        if (intent == null) return BatteryInfo(level = 100, isCharging = false)

        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val level = if (rawLevel >= 0 && scale > 0) {
            (rawLevel * 100) / scale
        } else {
            100
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryInfo(level = level, isCharging = isCharging)
    }

    fun startListening() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, filter)
            isReceiverRegistered = true
        }
    }

    fun stopListening() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
            isReceiverRegistered = false
        }
    }
}
