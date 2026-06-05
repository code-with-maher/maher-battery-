package com.maher.powerpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maher.powerpulse.data.BatteryStatsRepository
import com.maher.powerpulse.presentation.*
import com.maher.powerpulse.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Force full Right-To-Left direction for pure Arabic localized support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val repository = remember { BatteryStatsRepository(applicationContext) }
                    
                    val modelFactory = remember(repository) {
                        object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(BatteryViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return BatteryViewModel(repository) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    }

                    val viewModel: BatteryViewModel = viewModel(factory = modelFactory)
                    val uiState = viewModel.uiState.collectAsState()

                    val navController = rememberNavController()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        when (val state = uiState.value) {
                            is BatteryUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is BatteryUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                            is BatteryUiState.Success -> {
                                NavHost(
                                    navController = navController,
                                    startDestination = "dashboard",
                                    modifier = Modifier.padding(innerPadding)
                                ) {
                                    composable("dashboard") {
                                        DashboardScreen(
                                            state = state,
                                            onIntent = { viewModel.processIntent(it) },
                                            onNavigateToApps = {
                                                viewModel.processIntent(BatteryIntent.RefreshData)
                                                navController.navigate("apps")
                                            }
                                        )
                                    }
                                    composable("apps") {
                                        AppListScreen(
                                            state = state,
                                            onIntent = { viewModel.processIntent(it) },
                                            onNavigateBack = { navController.popBackStack() },
                                            onNavigateToDetail = { packageName ->
                                                viewModel.processIntent(BatteryIntent.SelectAppForDiagnostic(packageName))
                                                navController.navigate("diagnostics")
                                            }
                                        )
                                    }
                                    composable("diagnostics") {
                                        AppDetailsScreen(
                                            state = state,
                                            onIntent = { viewModel.processIntent(it) },
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions again when resuming if the user has navigated to System settings to set OPSTR_GET_USAGE_STATS
        // We can check if state isSuccess, and reload
    }
}
