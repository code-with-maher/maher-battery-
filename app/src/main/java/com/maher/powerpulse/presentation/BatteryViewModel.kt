package com.maher.powerpulse.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maher.powerpulse.data.BatteryStatsRepository
import com.maher.powerpulse.domain.AppUsageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BatteryViewModel(private val repository: BatteryStatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BatteryUiState>(BatteryUiState.Loading)
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    init {
        // Initial state loading
        processIntent(BatteryIntent.LoadScreenData)
    }

    fun processIntent(intent: BatteryIntent) {
        viewModelScope.launch {
            when (intent) {
                is BatteryIntent.LoadScreenData -> loadData()
                is BatteryIntent.RefreshData -> loadData(isRefresh = true)
                is BatteryIntent.ToggleSorting -> handleToggleSorting(intent.sortType)
                is BatteryIntent.SelectAppForDiagnostic -> handleSelectApp(intent.packageName)
                is BatteryIntent.ClearDiagnosticSelection -> handleClearDiagnosticSelection()
                is BatteryIntent.ForceStopApp -> handleForceStopApp(intent.packageName)
                is BatteryIntent.SystemPermissionChanged -> handlePermissionChanged()
            }
        }
    }

    private suspend fun loadData(isRefresh: Boolean = false) {
        if (isRefresh || _uiState.value is BatteryUiState.Loading) {
            // Keep previous success visible during pull-to-refresh to avoid white screens
            if (_uiState.value !is BatteryUiState.Success) {
                _uiState.value = BatteryUiState.Loading
            }
        }

        try {
            val status = repository.getBatteryStatus()
            
            // Discharged percentage since full charge (approx max - current)
            val dischargedPercent = (100 - status.percentage).coerceAtLeast(1)
            
            val (hwUsage, appUsage) = repository.getHardwareAndAppUsage(dischargedPercent)
            val alerts = repository.getAnomalyAlerts(hwUsage, appUsage, status.temperature)
            val isPermissionGranted = repository.isUsageAccessGranted()

            // Resolve sorting
            val currentSorting = when (val state = _uiState.value) {
                is BatteryUiState.Success -> state.appSorting
                else -> AppSortType.TOTAL_CONSUMPTION
            }

            val sortedApps = sortApps(appUsage, currentSorting)

            // Resolve selected app if any is currently active inside Diagnostics
            val currentSelectedApp = when (val state = _uiState.value) {
                is BatteryUiState.Success -> {
                    state.selectedAppDetail?.let { current ->
                        appUsage.find { it.packageName == current.packageName } ?: current
                    }
                }
                else -> null
            }

            val mockTrends = currentSelectedApp?.let { generateTrendForApp(it) } ?: emptyList()

            _uiState.value = BatteryUiState.Success(
                batteryStatus = status,
                hardwareUsage = hwUsage,
                appUsage = sortedApps,
                anomalyAlerts = alerts,
                appSorting = currentSorting,
                isPermissionGranted = isPermissionGranted,
                selectedAppDetail = currentSelectedApp,
                mockHistoricalTrend = mockTrends
            )
        } catch (e: Exception) {
            _uiState.value = BatteryUiState.Error(e.localizedMessage ?: "حدث خطأ غير متوقع أثناء معالجة البيانات.")
        }
    }

    private fun handleToggleSorting(type: AppSortType) {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        _uiState.update {
            val sorted = sortApps(state.appUsage, type)
            state.copy(
                appSorting = type,
                appUsage = sorted
            )
        }
    }

    private fun handleSelectApp(packageName: String) {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        val app = state.appUsage.find { it.packageName == packageName } ?: return
        val trend = generateTrendForApp(app)
        
        _uiState.update {
            state.copy(
                selectedAppDetail = app,
                mockHistoricalTrend = trend
            )
        }
    }

    private fun handleClearDiagnosticSelection() {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        _uiState.update {
            state.copy(
                selectedAppDetail = null,
                mockHistoricalTrend = emptyList()
            )
        }
    }

    private fun handleForceStopApp(packageName: String) {
        // Here we trigger the intent action outside or let the view handle it.
        // We can simulated update details or trigger recalculation
        viewModelScope.launch {
            loadData(isRefresh = true)
        }
    }

    private fun handlePermissionChanged() {
        viewModelScope.launch {
            loadData(isRefresh = true)
        }
    }

    private fun sortApps(apps: List<AppUsageInfo>, type: AppSortType): List<AppUsageInfo> {
        return when (type) {
            AppSortType.TOTAL_CONSUMPTION -> apps.sortedByDescending { it.totalMah }
            AppSortType.BACKGROUND_ABUSE -> apps.sortedByDescending { it.backgroundMah }
        }
    }

    private fun generateTrendForApp(app: AppUsageInfo): List<Float> {
        val seed = app.packageName.hashCode()
        // Seven days of history
        val baseMah = app.totalMah.coerceAtLeast(10f)
        return List(7) { index ->
            // Simulating variations of usage over the week
            val ratio = 0.5f + (Math.abs((seed + index * 17) % 11) / 10f) // 0.5 to 1.5
            Math.round(baseMah * ratio * 10f) / 10f
        }
    }
}
