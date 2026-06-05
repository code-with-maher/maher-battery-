package com.example.presentation

import com.example.domain.*

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
        val mockHistoricalTrend: List<Float> = emptyList()
    ) : BatteryUiState
    
    data class Error(val message: String) : BatteryUiState
}

enum class AppSortType {
    TOTAL_CONSUMPTION,  // الاستهلاك الفعلي الإجمالي
    BACKGROUND_ABUSE    // الاستهلاك في الخلفية
}

sealed interface BatteryIntent {
    object LoadScreenData : BatteryIntent
    object RefreshData : BatteryIntent
    data class ToggleSorting(val sortType: AppSortType) : BatteryIntent
    data class SelectAppForDiagnostic(val packageName: String) : BatteryIntent
    object ClearDiagnosticSelection : BatteryIntent
    data class ForceStopApp(val packageName: String) : BatteryIntent
    object SystemPermissionChanged : BatteryIntent
}
