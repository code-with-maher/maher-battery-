package com.example.presentation

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
fun AppListScreen(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "تحليل استهلاك التطبيقات",
                        color = CosmicTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val nextSorting = if (state.appSorting == AppSortType.TOTAL_CONSUMPTION) {
                        AppSortType.BACKGROUND_ABUSE
                    } else {
                        AppSortType.TOTAL_CONSUMPTION
                    }
                    onIntent(BatteryIntent.ToggleSorting(nextSorting))
                },
                containerColor = BatteryGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("sorting_fab")
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "تغيير الفرز"
                )
            }
        },
        containerColor = CosmicBackground,
        modifier = modifier.testTag("app_list_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header showing the active sorting criteria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ترتيب فرز التطبيقات:",
                    color = CosmicTextSecondary,
                    fontSize = 13.sp
                )
                
                Text(
                    text = if (state.appSorting == AppSortType.TOTAL_CONSUMPTION) {
                        "الاستهلاك الفعلي الإجمالي ⚙️"
                    } else {
                        "الاستهلاك المفرط بالخلفية ⏳"
                    },
                    color = BatteryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Feature Tip / Permission prompt
                if (!state.isPermissionGranted) {
                    item {
                        PermissionPromptCard {
                            try {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    }
                }

                items(state.appUsage, key = { it.packageName }) { appInfo ->
                    AppConsumptionItem(
                        appInfo = appInfo,
                        onClick = { onNavigateToDetail(appInfo.packageName) },
                        modifier = Modifier.testTag("app_item_${appInfo.packageName}")
                    )
                }
            }
        }
    }
}

@Composable
fun AppConsumptionItem(
    appInfo: AppUsageInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon Placeholder matching application initial
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BatteryBlue, BatteryGreen)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val letterCheck = appInfo.appName.trim().firstOrNull() ?: 'أ'
                Text(
                    text = letterCheck.uppercaseChar().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = appInfo.appName,
                    color = CosmicTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scope 1: Foreground Consumption
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "في المقدمة",
                            color = CosmicTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${appInfo.percentageForeground}٪ م. (${appInfo.foregroundMah} mAh)",
                            color = BatteryGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "المدة: ${appInfo.foregroundDurationMinutes} د",
                            color = CosmicTextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Divider line
                    Box(modifier = Modifier.width(1.dp).height(35.dp).background(CosmicBorder))
                    Spacer(modifier = Modifier.width(12.dp))

                    // Scope 2: Background Consumption
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "في الخلفية",
                            color = CosmicTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${appInfo.percentageBackground}٪ م. (${appInfo.backgroundMah} mAh)",
                            color = OrangeWarning,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "المدة: ${appInfo.backgroundDurationMinutes} د",
                            color = CosmicTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Arrow Indicator for Nav Action
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = CosmicTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PermissionPromptCard(
    onGrantClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BatteryBlue.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = BatteryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "مطلوب صلاحية لإحصاءات الاستخدام",
                    color = CosmicTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "يفتقر التطبيق إلى صلاحية تتبع وقت تشغيل التطبيقات. نوصي بتفعيل الصلاحية ليتسنى لنا عرض تحليلات دقيقة بنسبة 100٪ عن استهلاك التطبيقات الفردي.",
                color = CosmicTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Right
            )

            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BatteryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    "منح الصلاحية الآن",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
