package com.example.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.PointMode
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppUsageInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val app = state.selectedAppDetail

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "التشخيص العميق للتطبيق",
                        color = CosmicTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onIntent(BatteryIntent.ClearDiagnosticSelection)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("app_details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = CosmicTextPrimary
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
        modifier = modifier.testTag("app_details_root")
    ) { innerPadding ->
        if (app == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Troubleshoot,
                        contentDescription = null,
                        tint = CosmicTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري تحميل بيانات التشخيص...",
                        color = CosmicTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Core info card
                AppDiagnosticHeaderCard(app)

                // Historical trend canvas graph card
                AppTrendChartCard(
                    trends = state.mockHistoricalTrend,
                    appName = app.appName
                )

                // Deep analysis parameters block
                DeepDiagnosticParamsCard(app)

                // Force stop interceptor button
                Button(
                    onClick = {
                        onIntent(BatteryIntent.ForceStopApp(app.packageName))
                        // Open system Application Details Settings
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                // Fallback fail silently
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedAlert,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = RedAlert, spotColor = RedAlert)
                        .testTag("force_stop_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "إيقاف إجباري للتطبيق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppDiagnosticHeaderCard(app: AppUsageInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BatteryBlue, BatteryGreen)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (app.appName.trim().firstOrNull() ?: 'أ').uppercaseChar().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = app.appName,
                    color = CosmicTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = CosmicTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (app.totalMah > 20f) RedAlert.copy(alpha = 0.15f) else BatteryGreen.copy(alpha = 0.15f),
                                id = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (app.totalMah > 20f) "تأثير مرتفع ⚠️" else "تأثير مستقر ✅",
                            color = if (app.totalMah > 20f) RedAlert else BatteryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppTrendChartCard(
    trends: List<Float>,
    appName: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "معدل استهلاك الطاقة اليومي (7 أيام)",
                color = CosmicTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("جاري استخلاص المنحنى البياني...", color = CosmicTextSecondary)
                }
            } else {
                val maxVal = trends.maxOrNull()?.coerceAtLeast(10f) ?: 100f
                val daysArabic = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

                // High fidelity custom Canvas drawing
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / 6
                    
                    val points = mutableListOf<Offset>()
                    for (i in trends.indices) {
                        val x = i * spacing
                        val ratio = trends[i] / maxVal
                        val y = height - (ratio * height * 0.85f) - 10f
                        points.add(Offset(x, y))
                    }

                    // Render grid lines
                    val gridLines = 4
                    for (j in 0..gridLines) {
                        val gridY = height * (j.toFloat() / gridLines)
                        drawLine(
                            color = CosmicBorder.copy(alpha = 0.4f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Create path matching points
                    val path = Path()
                    path.moveTo(points[0].x, points[0].y)
                    for (k in 1 until points.size) {
                        // Draw connection representation
                        val pPre = points[k - 1]
                        val pCur = points[k]
                        path.quadraticBezierTo(
                            (pPre.x + pCur.x) / 2,
                            pPre.y,
                            pCur.x,
                            pCur.y
                        )
                    }

                    // Draw line stroke
                    drawPath(
                        path = path,
                        color = BatteryGreen,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Draw filled slope area underneath
                    val fillPath = Path()
                    fillPath.addPath(path)
                    fillPath.lineTo(points.last().x, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BatteryGreen.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )

                    // Plot node indicator stars
                    for (pt in points) {
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = pt
                        )
                        drawCircle(
                            color = BatteryGreen,
                            radius = 8f,
                            center = pt,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                // X-Axis Labels Row showing Arabic days aligned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysArabic.forEach { dayName ->
                        Text(
                            text = dayName.take(3), // display shorthand
                            color = CosmicTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeepDiagnosticParamsCard(app: AppUsageInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "تقييم أداء الاستهلاك والتأثير",
                color = CosmicTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic Param Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total mAh Drained
                ParamMetricRow(
                    label = "إجمالي الطاقة المستهلكة",
                    value = "${app.totalMah} mAh (${app.totalPercentage}٪ من البطارية)",
                    iconColor = BatteryGreen
                )

                Divider(color = CosmicBorder)

                // Foreground Rate
                ParamMetricRow(
                    label = "متوسط التفريغ في المقدمة",
                    value = "${app.foregroundMah} mAh / لـ ${app.foregroundDurationMinutes} دقيقة",
                    iconColor = BatteryBlue
                )

                Divider(color = CosmicBorder)

                // Background Abuse Rate
                ParamMetricRow(
                    label = "استهلاك الخلفية التراكمي",
                    value = "${app.backgroundMah} mAh / لـ ${app.backgroundDurationMinutes} دقيقة",
                    iconColor = OrangeWarning
                )

                Divider(color = CosmicBorder)

                // Estimated Wakelock Impact
                ParamMetricRow(
                    label = "نشاط اليقظة الخلفي (Wakelock)",
                    value = if (app.backgroundDurationMinutes > 120) "مستمر بشكل زائد ⚠️" else "طبيعي وضمن الاستقرار ✅",
                    iconColor = if (app.backgroundDurationMinutes > 120) RedAlert else BatteryGreen
                )
            }
        }
    }
}

@Composable
fun ParamMetricRow(
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(iconColor, CircleShape)
            )
            Text(
                text = label,
                color = CosmicTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value,
            color = CosmicTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
