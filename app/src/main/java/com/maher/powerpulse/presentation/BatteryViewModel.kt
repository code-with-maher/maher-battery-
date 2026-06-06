package com.maher.powerpulse.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maher.powerpulse.data.BatteryStatsRepository
import com.maher.powerpulse.domain.*
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
                is BatteryIntent.ToggleShowMah -> handleToggleShowMah(intent.show)
                is BatteryIntent.ToggleRamSorting -> handleToggleRamSorting(intent.sortType)
                is BatteryIntent.CleanRam -> handleCleanRam()
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

            // Resolve sorting and settings
            val previousSuccess = _uiState.value as? BatteryUiState.Success
            val currentSorting = previousSuccess?.appSorting ?: AppSortType.TOTAL_CONSUMPTION
            val currentShowMah = previousSuccess?.showMahInStats ?: false
            val currentRamSorting = previousSuccess?.ramSorting ?: RamSortType.BY_USAGE
            val isCurrentlyCleaned = previousSuccess?.isRamCleaned ?: false

            val sortedApps = sortApps(appUsage, currentSorting)
            
            // Get hardware metrics
            val specs = repository.getHardwareSpecs(currentRamSorting)
            val finalSpecs = if (isCurrentlyCleaned) {
                specs.copy(
                    usedRamGb = (specs.usedRamGb - 1.1).coerceAtLeast(1.5),
                    freeRamGb = (specs.freeRamGb + 1.1).coerceAtMost(specs.totalRamGb),
                    ramUsedPercent = (((specs.usedRamGb - 1.1) / specs.totalRamGb) * 100).toFloat().coerceAtLeast(15f),
                    cpuUsagePercent = (specs.cpuUsagePercent * 0.45f).coerceAtLeast(10f),
                    gpuUsagePercent = (specs.gpuUsagePercent * 0.5f).coerceAtLeast(8f),
                    ramAppsList = specs.ramAppsList.map {
                        if (!it.isSystemProcess) it.copy(ramUsageMb = Math.round(it.ramUsageMb * 0.25 * 10.0) / 10.0) else it
                    }.filter { it.ramUsageMb > 10.0 }
                )
            } else {
                specs
            }

            // Resolve selected app if any is currently active inside Diagnostics
            val currentSelectedApp = previousSuccess?.let { state ->
                state.selectedAppDetail?.let { current ->
                    appUsage.find { it.packageName == current.packageName } ?: current
                }
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
                mockHistoricalTrend = mockTrends,
                showMahInStats = currentShowMah,
                hardwareSpecs = finalSpecs,
                ramSorting = currentRamSorting,
                isRamCleaned = isCurrentlyCleaned
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

    private fun handleToggleShowMah(show: Boolean) {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        _uiState.update {
            state.copy(showMahInStats = show)
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

    private fun handleToggleRamSorting(type: RamSortType) {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        viewModelScope.launch {
            val specs = repository.getHardwareSpecs(type)
            val updatedSpecs = if (state.isRamCleaned) {
                specs.copy(
                    usedRamGb = (specs.usedRamGb - 1.1).coerceAtLeast(1.5),
                    freeRamGb = (specs.freeRamGb + 1.1).coerceAtMost(specs.totalRamGb),
                    ramUsedPercent = (((specs.usedRamGb - 1.1) / specs.totalRamGb) * 100).toFloat().coerceAtLeast(15f),
                    cpuUsagePercent = (specs.cpuUsagePercent * 0.45f).coerceAtLeast(10f),
                    gpuUsagePercent = (specs.gpuUsagePercent * 0.5f).coerceAtLeast(8f),
                    ramAppsList = specs.ramAppsList.map {
                        if (!it.isSystemProcess) it.copy(ramUsageMb = Math.round(it.ramUsageMb * 0.25 * 10.0) / 10.0) else it
                    }.filter { it.ramUsageMb > 10.0 }
                )
            } else {
                specs
            }
            _uiState.update {
                state.copy(
                    ramSorting = type,
                    hardwareSpecs = updatedSpecs
                )
            }
        }
    }

    private fun handleCleanRam() {
        val state = _uiState.value as? BatteryUiState.Success ?: return
        viewModelScope.launch {
            state.hardwareSpecs.ramAppsList.forEach { app ->
                if (!app.isSystemProcess) {
                    repository.killBackgroundProcesses(app.packageName)
                }
            }
            
            val currentSpecs = state.hardwareSpecs
            val freedAmountGb = (currentSpecs.usedRamGb * 0.25).coerceIn(0.8, 1.8)
            val updatedUsedGb = Math.round((currentSpecs.usedRamGb - freedAmountGb) * 100.0) / 100.0
            val updatedFreeGb = Math.round((currentSpecs.freeRamGb + freedAmountGb) * 100.0) / 100.0
            val updatedPercent = (((updatedUsedGb / currentSpecs.totalRamGb) * 100).toFloat()).coerceAtLeast(15f)
            
            val updatedRamAppsList = currentSpecs.ramAppsList.map { app ->
                if (!app.isSystemProcess) {
                    app.copy(ramUsageMb = Math.round(app.ramUsageMb * 0.25 * 10.0) / 10.0)
                } else {
                    app
                }
            }.filter { it.ramUsageMb > 10.0 }
            
            _uiState.update {
                state.copy(
                    isRamCleaned = true,
                    hardwareSpecs = currentSpecs.copy(
                        usedRamGb = updatedUsedGb,
                        freeRamGb = updatedFreeGb,
                        ramUsedPercent = updatedPercent,
                        cpuUsagePercent = (currentSpecs.cpuUsagePercent * 0.35f).coerceAtLeast(8.5f),
                        gpuUsagePercent = (currentSpecs.gpuUsagePercent * 0.4f).coerceAtLeast(5.0f),
                        ramAppsList = updatedRamAppsList
                    )
                )
            }
        }
    }

    private fun sortApps(apps: List<AppUsageInfo>, type: AppSortType): List<AppUsageInfo> {
        return when (type) {
            AppSortType.TOTAL_CONSUMPTION -> apps.sortedByDescending { it.totalMah }
            AppSortType.BACKGROUND_CONSUMPTION -> apps.sortedByDescending { it.backgroundMah }
            AppSortType.FOREGROUND_CONSUMPTION -> apps.sortedByDescending { it.foregroundMah }
            AppSortType.APP_NAME -> apps.sortedBy { it.appName.lowercase() }
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
