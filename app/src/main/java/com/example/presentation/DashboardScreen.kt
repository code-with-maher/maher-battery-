package com.example.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AlertSeverity
import com.example.domain.AnomalyAlert
import com.example.domain.BatteryStatusInfo
import com.example.domain.HardwareUsageInfo

// Premium Dark Color Palette
val CosmicBackground = Color(0xFF0B0F19)
val CosmicSurface = Color(0xFF151D30)
val CosmicCardBg = Color(0xFF1A243C)
val CosmicBorder = Color(0xFF243254)
val BatteryGreen = Color(0xFF10B981)
val BatteryBlue = Color(0xFF3B82F6)
val OrangeWarning = Color(0xFFF59E0B)
val RedAlert = Color(0xFFEF4444)
val CosmicTextPrimary = Color(0xFFF8FAFC)
val CosmicTextSecondary = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onNavigateToApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "راصد البطارية الذكي",
                        color = CosmicTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(BatteryIntent.RefreshData) },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "قفل تحديث البيانات",
                            tint = BatteryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CosmicBackground,
                    titleContentColor = CosmicTextPrimary
                )
            )
        },
        containerColor = CosmicBackground,
        modifier = modifier.testTag("dashboard_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Component 1: Battery Summary Header Card
            BatterySummaryCard(
                status = state.batteryStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_header_card")
            )

            // Component 3: Anomaly Alert Section (تنبيهات الاستهلاك المرتفع)
            if (state.anomalyAlerts.isNotEmpty()) {
                AnomalyAlertsSection(
                    alerts = state.anomalyAlerts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anomaly_alert_section")
                )
            }

            // Component 2: Hidden Hardware & System Consumption
            HardwareConsumptionCard(
                hardwareList = state.hardwareUsage,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_consumption_section")
            )

            // Component 4: Navigation Button (عرض استهلاك التطبيقات)
            Button(
                onClick = onNavigateToApps,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BatteryGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = BatteryGreen, spotColor = BatteryGreen)
                    .testTag("view_apps_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "عرض استهلاك التطبيقات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BatterySummaryCard(
    status: BatteryStatusInfo,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Progress Gauge Representation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(175.dp)
            ) {
                // Background track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(165.dp),
                    color = CosmicBorder,
                    strokeWidth = 10.dp
                )
                // Foreground battery level
                CircularProgressIndicator(
                    progress = { status.percentage / 100f },
                    modifier = Modifier.size(165.dp),
                    color = when {
                        status.percentage > 30 -> BatteryGreen
                        status.percentage > 15 -> OrangeWarning
                        else -> RedAlert
                    },
                    strokeWidth = 10.dp
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "${status.percentage}٪",
                        color = CosmicTextPrimary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        status.chargingStatus,
                        color = BatteryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Estimate time remaining
            Surface(
                color = CosmicCardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "الوقت المتبقي التقريبي:",
                        color = CosmicTextSecondary,
                        fontSize = 13.sp
                    )
                    val hours = status.remainingHours.toInt()
                    val minutes = ((status.remainingHours - hours) * 60).toInt()
                    Text(
                        if (hours > 0) "$hours ساعة و $minutes دقيقة" else "$minutes دقيقة",
                        color = BatteryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Divider(color = CosmicBorder, thickness = 1.dp)

            // Health, Temp, Voltage Table Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Item 1: Health
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "صحة البطارية",
                        tint = RedAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("حالة البطارية", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text(status.health, color = CosmicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Separator
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(CosmicBorder))

                // Item 2: Temperature
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "درجة الحرارة",
                        tint = OrangeWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("حرارة الجهاز", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text("${status.temperature}°مئوية", color = CosmicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Separator
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(CosmicBorder))

                // Item 3: Capacity
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LightningBolt,
                        contentDescription = "السعة القصوى والجهد",
                        tint = BatteryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("السعة القصوى", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text("${status.capacityMah} mAh", color = CosmicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HardwareConsumptionCard(
    hardwareList: List<HardwareUsageInfo>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "الاستهلاك الخفي للنظام والعتاد",
                color = CosmicTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            hardwareList.forEach { hw ->
                val icon = when (hw.id) {
                    "screen" -> Icons.Default.PhoneAndroid
                    "wifi" -> Icons.Default.Wifi
                    "mobile_network" -> Icons.Default.CellTower
                    "camera" -> Icons.Default.CameraAlt
                    "android_system" -> Icons.Default.Adb
                    else -> Icons.Default.HourglassEmpty
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = hw.arabicName,
                                tint = BatteryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = hw.arabicName,
                                color = CosmicTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Text(
                            text = "${hw.percentage}٪ (${hw.consumptionMah} mAh)",
                            color = CosmicTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Progress bar representing HW utilization
                    LinearProgressIndicator(
                        progress = { hw.percentage / 20f }, // scaled for visibility relative to max hardware partition
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = BatteryBlue,
                        trackColor = CosmicBorder
                    )
                }
            }
        }
    }
}

@Composable
fun AnomalyAlertsSection(
    alerts: List<AnomalyAlert>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "أيقونة تنبيه",
                tint = RedAlert,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "تنبيهات الاستهلاك المرتفع",
                color = RedAlert,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        alerts.forEach { alert ->
            val tintColor = when (alert.severity) {
                AlertSeverity.HIGH -> RedAlert
                AlertSeverity.MEDIUM -> OrangeWarning
                AlertSeverity.LOW -> BatteryBlue
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, tintColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(tintColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alert.isApp) Icons.Default.WarningAmber else Icons.Default.Info,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = alert.title,
                            color = CosmicTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alert.description,
                            color = CosmicTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }
}
