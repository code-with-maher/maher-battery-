package com.maher.powerpulse.presentation

import com.maher.powerpulse.domain.*

sealed interface BatteryUiState {
    object Loading : BatteryUiState
    
    data class Success(
        val batteryStatus: BatteryStatusInfo,
        val hardwareUsage: List<HardwareUsageInfo>,
        val appUsage: List<AppUsageInfo>,
        val anomalyAlerts: List<AnomalyAlert>,
        val appSorting: AppSortType = AppSortType.TOTAL_CONSUMPTION,
        val isPermissionGranted: Boolean,
        val selectedAppDetail: AppUsageInfo? = null,
        val mockHistoricalTrend: List<Float> = emptyList(),
        val showMahInStats: Boolean = false,
        val hardwareSpecs: HardwareSpecsInfo,
        val ramSorting: RamSortType = RamSortType.BY_USAGE,
        val isRamCleaned: Boolean = false
    ) : BatteryUiState
    
    data class Error(val message: String) : BatteryUiState
}

enum class AppSortType {
    TOTAL_CONSUMPTION,       // الاستهلاك الإجمالي للبطارية
    BACKGROUND_CONSUMPTION,  // الاستهلاك في الخلفية
    FOREGROUND_CONSUMPTION,  // الاستهلاك في المقدمة
    APP_NAME                 // اسم التطبيق أبجدياً
}

sealed interface BatteryIntent {
    object LoadScreenData : BatteryIntent
    object RefreshData : BatteryIntent
    data class ToggleSorting(val sortType: AppSortType) : BatteryIntent
    data class SelectAppForDiagnostic(val packageName: String) : BatteryIntent
    object ClearDiagnosticSelection : BatteryIntent
    data class ForceStopApp(val packageName: String) : BatteryIntent
    object SystemPermissionChanged : BatteryIntent
    data class ToggleShowMah(val show: Boolean) : BatteryIntent
    data class ToggleRamSorting(val sortType: RamSortType) : BatteryIntent
    object CleanRam : BatteryIntent
}

