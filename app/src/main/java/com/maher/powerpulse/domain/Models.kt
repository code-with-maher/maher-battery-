package com.maher.powerpulse.domain

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

data class HardwareSpecsInfo(
    val totalRamGb: Double,
    val usedRamGb: Double,
    val freeRamGb: Double,
    val ramUsedPercent: Float,
    val cpuModel: String,
    val cpuUsagePercent: Float,
    val gpuModel: String,
    val gpuUsagePercent: Float,
    val ramAppsList: List<RamAppUsageInfo>
)

data class RamAppUsageInfo(
    val packageName: String,
    val appName: String,
    val ramUsageMb: Double,
    val isSystemProcess: Boolean
)

enum class RamSortType {
    BY_USAGE, BY_NAME
}
