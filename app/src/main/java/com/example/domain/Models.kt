package com.example.domain

import androidx.compose.ui.graphics.vector.ImageVector

data class BatteryStatusInfo(
    val percentage: Int,
    val health: String,
    val temperature: Float,
    val voltage: Float,
    val chargingStatus: String,
    val capacityMah: Int,
    val remainingHours: Float
)

data class HardwareUsageInfo(
    val id: String,
    val arabicName: String,
    val percentage: Float,
    val consumptionMah: Float
)

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val percentageForeground: Float,
    val percentageBackground: Float,
    val foregroundMah: Float,
    val backgroundMah: Float,
    val foregroundDurationMinutes: Long,
    val backgroundDurationMinutes: Long
) {
    val totalPercentage: Float
        get() = percentageForeground + percentageBackground

    val totalMah: Float
        get() = foregroundMah + backgroundMah
}

data class AnomalyAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val componentName: String,
    val isApp: Boolean
)

enum class AlertSeverity {
    HIGH, MEDIUM, LOW
}
