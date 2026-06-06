package com.maher.powerpulse.presentation

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maher.powerpulse.domain.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// Premium Dark Space Palette
val CosmicBackground = Color(0xFF0B111E)
val CosmicSurface = Color(0xFF141C2E)
val CosmicCardBg = Color(0xFF1B263E)
val CosmicBorder = Color(0xFF263556)
val BatteryGreen = Color(0xFF10B981)
val BatteryBlue = Color(0xFF3B82F6)
val OrangeWarning = Color(0xFFF59E0B)
val RedAlert = Color(0xFFEF4444)
val CosmicTextPrimary = Color(0xFFF1F5F9)
val CosmicTextSecondary = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onNavigateToApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Bottom Tab Select: 0 -> General Info, 1 -> Hardware details, 2 -> Battery Analysis, 3 -> RAM/Hardware Audit
    var selectedBottomTab by remember { mutableStateOf(2) }

    // Screen navigation overlay toggles
    var showGeneralDetailsOverlay by remember { mutableStateOf(false) }
    var showHardwareDetailsOverlay by remember { mutableStateOf(false) }
    var showBatteryDetailsOverlay by remember { mutableStateOf(false) }
    var showRamDetailsOverlay by remember { mutableStateOf(false) }

    // Drawer state navigation (if setting overlays)
    var currentDrawerPage by remember { mutableStateOf<String?>(null) } // "settings", "contact", "about" or null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CosmicSurface,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.linearGradient(listOf(BatteryBlue, BatteryGreen)),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "راصد نبض الطاقة",
                                color = CosmicTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "إصدار 1.2.0 الممتاز",
                                color = CosmicTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = CosmicBorder, modifier = Modifier.padding(bottom = 16.dp))

                    // Menu Options
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null, tint = CosmicTextPrimary) },
                        label = { Text("الشاشة الرئيسية", color = CosmicTextPrimary) },
                        selected = currentDrawerPage == null,
                        onClick = {
                            currentDrawerPage = null
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = CosmicCardBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = CosmicTextPrimary) },
                        label = { Text("إعدادات التطبيق", color = CosmicTextPrimary) },
                        selected = currentDrawerPage == "settings",
                        onClick = {
                            currentDrawerPage = "settings"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = CosmicCardBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = CosmicTextPrimary) },
                        label = { Text("اتصل بنا", color = CosmicTextPrimary) },
                        selected = currentDrawerPage == "contact",
                        onClick = {
                            currentDrawerPage = "contact"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = CosmicCardBg
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = CosmicTextPrimary) },
                        label = { Text("حول التطبيق", color = CosmicTextPrimary) },
                        selected = currentDrawerPage == "about",
                        onClick = {
                            currentDrawerPage = "about"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = CosmicCardBg
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Signature
                    Text(
                        "صنع بكل فخر لدعم إمكانية الوصول ♿",
                        color = CosmicTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        gesturesEnabled = true
    ) {
        // Base Content Area depending on Drawer / Sub-Screen Selection
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentDrawerPage) {
                                "settings" -> "إعدادات التطبيق"
                                "contact" -> "اتصل بنا"
                                "about" -> "حول التطبيق"
                                else -> "راصد نبض الطاقة"
                            },
                            color = CosmicTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    },
                    navigationIcon = {
                        if (currentDrawerPage != null) {
                            IconButton(onClick = { currentDrawerPage = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع للرئيسية", tint = CosmicTextPrimary)
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "قائمة الخيارات", tint = CosmicTextPrimary)
                            }
                        }
                    },
                    actions = {
                        if (currentDrawerPage == null) {
                            IconButton(onClick = { onIntent(BatteryIntent.RefreshData) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "تحديث البيانات", tint = BatteryGreen)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = CosmicBackground,
                        titleContentColor = CosmicTextPrimary
                    )
                )
            },
            bottomBar = {
                if (currentDrawerPage == null && !showGeneralDetailsOverlay && !showHardwareDetailsOverlay && !showBatteryDetailsOverlay && !showRamDetailsOverlay) {
                    NavigationBar(
                        containerColor = CosmicSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.PhoneAndroid, "المعلومات العامة") },
                            label = { Text("المعلومات العامة", fontSize = 11.sp) },
                            selected = selectedBottomTab == 0,
                            onClick = { selectedBottomTab = 0 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BatteryBlue,
                                unselectedIconColor = CosmicTextSecondary,
                                indicatorColor = CosmicCardBg
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Memory, "العتاد والمستشعرات") },
                            label = { Text("العتاد والمستشعرات", fontSize = 11.sp) },
                            selected = selectedBottomTab == 1,
                            onClick = { selectedBottomTab = 1 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BatteryBlue,
                                unselectedIconColor = CosmicTextSecondary,
                                indicatorColor = CosmicCardBg
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.ElectricBolt, "البطارية والطاقة") },
                            label = { Text("البطارية وتحليل الطاقة", fontSize = 11.sp) },
                            selected = selectedBottomTab == 2,
                            onClick = { selectedBottomTab = 2 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BatteryBlue,
                                unselectedIconColor = CosmicTextSecondary,
                                indicatorColor = CosmicCardBg
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Speed, "الذاكرة والعتاد") },
                            label = { Text("الذاكرة والعتاد", fontSize = 11.sp) },
                            selected = selectedBottomTab == 3,
                            onClick = { selectedBottomTab = 3 },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BatteryBlue,
                                unselectedIconColor = CosmicTextSecondary,
                                indicatorColor = CosmicCardBg
                            )
                        )
                    }
                }
            },
            containerColor = CosmicBackground,
            modifier = modifier.testTag("app_dashboard_scaffold")
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Determine layout
                when {
                    currentDrawerPage == "settings" -> {
                        SettingsScreenContent(state = state, onIntent = onIntent)
                    }
                    currentDrawerPage == "contact" -> {
                        ContactScreenContent()
                    }
                    currentDrawerPage == "about" -> {
                        AboutScreenContent()
                    }
                    showGeneralDetailsOverlay -> {
                        GeneralDetailsScreenContent(onBack = { showGeneralDetailsOverlay = false })
                    }
                    showHardwareDetailsOverlay -> {
                        HardwareDetailsScreenContent(onBack = { showHardwareDetailsOverlay = false })
                    }
                    showBatteryDetailsOverlay -> {
                        BatteryAppsConsumptionScreenContent(
                            state = state,
                            onIntent = onIntent,
                            onBack = { showBatteryDetailsOverlay = false }
                        )
                    }
                    showRamDetailsOverlay -> {
                        RamUsageDetailsScreenContent(
                            state = state,
                            onIntent = onIntent,
                            onBack = { showRamDetailsOverlay = false }
                        )
                    }
                    else -> {
                        // Core Bottom Tab Router
                        when (selectedBottomTab) {
                            0 -> TabGeneral(
                                onShowMore = { showGeneralDetailsOverlay = true }
                            )
                            1 -> TabHardware(
                                onShowMore = { showHardwareDetailsOverlay = true }
                            )
                            2 -> TabBattery(
                                state = state,
                                onShowMore = { showBatteryDetailsOverlay = true }
                            )
                            3 -> TabRamHardware(
                                state = state,
                                onShowMore = { showRamDetailsOverlay = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DRAWER PAGES SECTION
// ==========================================

@Composable
fun SettingsScreenContent(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "تفضيلات وقراءات لوحة المعلومات",
            color = CosmicTextSecondary,
            fontSize = 14.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "عرض الإحصائيات بالملي أمبير",
                            color = CosmicTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "عرض سعات واستهلاك البطارية بوحدة mAh بدلاً من النسبة المئوية.",
                            color = CosmicTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.showMahInStats,
                        onCheckedChange = { onIntent(BatteryIntent.ToggleShowMah(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BatteryGreen,
                            checkedTrackColor = BatteryGreen.copy(alpha = 0.4f)
                        )
                    )
                }

                HorizontalDivider(color = CosmicBorder)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "تحديث نشط ذكي",
                            color = CosmicTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "مستشعرات نشطة يتم تشغيلها وإيقافها بذكاء مع حماية عمر البطارية.",
                            color = CosmicTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = BatteryBlue.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ContactScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SupportAgent,
            contentDescription = null,
            tint = BatteryBlue,
            modifier = Modifier.size(72.dp)
        )

        Text(
            "الدعم الفني والاتصال بالمطور",
            color = CosmicTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "إذا كان لديك أي اقتراحات أو واجهت مشكلة متعلقة بالتطبيق، يسعدنا تواصلك معنا مباشرة.",
            color = CosmicTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) {}
                ) {
                    Icon(Icons.Default.Email, "البريد الإلكتروني", tint = BatteryGreen)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("البريد الإلكتروني المباشر", color = CosmicTextSecondary, fontSize = 12.sp)
                        Text("www.mahr1006@gmail.com", color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                HorizontalDivider(color = CosmicBorder)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) {}
                ) {
                    Icon(Icons.Default.Person, "المطور المسؤول", tint = BatteryBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("المطور البرمجي", color = CosmicTextSecondary, fontSize = 12.sp)
                        Text("ماهر - Maher PowerPulse Team", color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.radialGradient(listOf(CosmicCardBg, CosmicSurface)),
                    CircleShape
                )
                .border(1.dp, CosmicBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = BatteryGreen,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            "نبض الطاقة الذكي",
            color = CosmicTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "لوحة معلومات لوجستية متكاملة تطلعك على نبض العتاد، السوفتوير، والمستشعرات والبطارية بذكاء.",
            color = CosmicTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ميزات الإصدار الحالي:",
                    color = CosmicTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = BatteryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("دعم كامل لقارئ الشاشة والوصول السهل ♿", color = CosmicTextSecondary, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = BatteryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختبار حي ومتحرك للمستشعرات الأساسية.", color = CosmicTextSecondary, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = BatteryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فرز متقدم للتطبيقات المستهلكة بأكثر من اتجاه.", color = CosmicTextSecondary, fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = BatteryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ميزات عرض بالملي أمبير وإحصائيات دقيقة للغاية.", color = CosmicTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}


// ==========================================
// BOTTOM NAVIGATION TABS
// ==========================================

@Composable
fun TabGeneral(
    onShowMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "معلومات النظام والتشغيل",
            color = CosmicTextSecondary,
            fontSize = 14.sp
        )

        // General Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("اسم و موديل الهاتف", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text(Build.MODEL, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                HorizontalDivider(color = CosmicBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إصدار الأندرويد الأساسي", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text(Build.VERSION.RELEASE, color = BatteryBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                HorizontalDivider(color = CosmicBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الشركة المصنعة", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text(Build.MANUFACTURER.uppercase(), color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Action Button for Full Overlay
        Button(
            onClick = onShowMore,
            colors = ButtonDefaults.buttonColors(containerColor = BatteryBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("شرح مفصل وعرض كل تفاصيل السوفتوير", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TabHardware(
    onShowMore: () -> Unit
) {
    val cores = Runtime.getRuntime().availableProcessors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "معلومات عتاد ومكونات الجهاز",
            color = CosmicTextSecondary,
            fontSize = 14.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أنوية معالج الهاتف (Cores)", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text("$cores أنوية نشطة", color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                HorizontalDivider(color = CosmicBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المجال الافتراضي للهاردوير", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text(Build.HARDWARE, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                HorizontalDivider(color = CosmicBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المستشعرات الجانبية", color = CosmicTextSecondary, fontSize = 13.sp)
                    Text("بوصلة، تسارع، ومستشعر الضوء", color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = onShowMore,
            colors = ButtonDefaults.buttonColors(containerColor = BatteryBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CompassCalibration, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("افتح اختبار المستشعرات والعتاد التفصيلي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TabBattery(
    state: BatteryUiState.Success,
    onShowMore: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Battery Dial Card
        BatterySummaryCard(
            status = state.batteryStatus,
            modifier = Modifier.fillMaxWidth()
        )

        // Anomaly warnings list
        if (state.anomalyAlerts.isNotEmpty()) {
            AnomalyAlertsSection(
                alerts = state.anomalyAlerts,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Hardware silent draw estimate
        HardwareConsumptionCard(
            hardwareList = state.hardwareUsage,
            modifier = Modifier.fillMaxWidth()
        )

        // Navigation button to list of apps with custom sorts
        Button(
            onClick = onShowMore,
            colors = ButtonDefaults.buttonColors(
                containerColor = BatteryGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = BatteryGreen, spotColor = BatteryGreen)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Troubleshoot,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "عرض المزيد وتحليل استهلاك التطبيقات",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ==========================================
// SUB-SCREEN DETAILS OVERLAYS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralDetailsScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val systemInfo = remember(context) { getDetailedSoftwareInfo(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل سوفتوير النظام", fontWeight = FontWeight.Bold, color = CosmicTextPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = CosmicTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicBackground)
            )
        },
        containerColor = CosmicBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    "جميع البيانات أدناه يتم استعلامها فورياً بدقة متكاملة من نواة نظام أندرويد للهاتف لجعل لوحة المعلومات حقيقية بنسبة 100%.",
                    color = CosmicTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(systemInfo) { (key, value) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                        .semantics(mergeDescendants = true) {}
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(key, color = CosmicTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(value, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareDetailsScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val hardwareInfo = remember(context) { getDetailedHardwareInfo(context) }
    
    // Choose active sensor to test: "compass", "light", "accelerometer" or null
    var activeSensorTest by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activeSensorTest == null) "العتاد الفني والمستشعرات" else "اختبار المستشعر الحي", fontWeight = FontWeight.Bold, color = CosmicTextPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeSensorTest != null) {
                            activeSensorTest = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = CosmicTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicBackground)
            )
        },
        containerColor = CosmicBackground
    ) { paddingValues ->
        if (activeSensorTest != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeSensorTest) {
                    "compass" -> CompassTestScreen()
                    "light" -> LightTestScreen()
                    "accelerometer" -> AccelerometerTestScreen()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        "تفاصيل العتاد المسجل بجانب اختبار حي وتفاعلي للحساسات لاختبار استجابة النظام ولتأكيد دقة التشغيل.",
                        color = CosmicTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                item {
                    Text(
                        "لوحات وحساسات قابلة للاختبار (تفاعلي مباشر) ↓",
                        color = BatteryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Interactive Sensor test card rows
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SensorTestLaunchRow(
                            title = "اختبار البوصلة المباشر (Compass)",
                            description = "اختيار تفاعلي يعرض دوران وإصغاء الجهاز للمحاور المغناطيسية برسم حي.",
                            icon = Icons.Default.CompassCalibration,
                            iconColor = BatteryGreen,
                            onClick = { activeSensorTest = "compass" }
                        )

                        SensorTestLaunchRow(
                            title = "اختبار مستشعر الضوء (Ambiance Lux)",
                            description = "قياس مستويات السطوع واللومين الحقيقي المحيط بالهاتف بشكل مباشر.",
                            icon = Icons.Default.LightMode,
                            iconColor = OrangeWarning,
                            onClick = { activeSensorTest = "light" }
                        )

                        SensorTestLaunchRow(
                            title = "اختبار مقياس التسارع والجاذبية",
                            description = "اختبار ليفل فقاعة الماء عبر قراءة محاور الحركة ثنائية الاتجاه X و Y.",
                            icon = Icons.Default.ScreenRotation,
                            iconColor = BatteryBlue,
                            onClick = { activeSensorTest = "accelerometer" }
                        )
                    }
                }

                item {
                    Text(
                        "مكونات الهاردوير والقطع الثابتة ↓",
                        color = CosmicTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                items(hardwareInfo) { (key, value) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                            .semantics(mergeDescendants = true) {}
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(key, color = CosmicTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(value, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorTestLaunchRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, color = CosmicTextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronLeft, "افتح الاختبار", tint = CosmicTextSecondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryAppsConsumptionScreenContent(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onBack: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val sortLabel = when (state.appSorting) {
        AppSortType.TOTAL_CONSUMPTION -> "الاستهلاك الإجمالي"
        AppSortType.BACKGROUND_CONSUMPTION -> "الاستهلاك في الخلفية"
        AppSortType.FOREGROUND_CONSUMPTION -> "الاستهلاك في المقدمة"
        AppSortType.APP_NAME -> "ترتيب حسب الاسم"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحليل تفاصيل استهلاك التطبيقات", fontWeight = FontWeight.Bold, color = CosmicTextPrimary, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = CosmicTextPrimary)
                    }
                },
                actions = {
                    Box {
                        Button(
                            onClick = { sortMenuExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sortLabel, fontSize = 12.sp, color = CosmicTextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, null, tint = CosmicTextPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(CosmicSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("الاستهلاك الإجمالي الكلي", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleSorting(AppSortType.TOTAL_CONSUMPTION))
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الاستهلاك الفعلي في الخلفية", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleSorting(AppSortType.BACKGROUND_CONSUMPTION))
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الاستهلاك الفعلي في المقدمة", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleSorting(AppSortType.FOREGROUND_CONSUMPTION))
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الاسم أبجدياً", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleSorting(AppSortType.APP_NAME))
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicBackground)
            )
        },
        containerColor = CosmicBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "قائمة استهلاك التطبيقات تظهر التفاصيل مع دعم كامل للفرز وتأمين كامل للمعلومات لحمايتك:",
                color = CosmicTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (!state.isPermissionGranted) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrangeWarning.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, OrangeWarning, RoundedCornerShape(12.dp))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = OrangeWarning)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("صلاحية تتبع التطبيقات معطلة", color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("يعرض التطبيق تقديرات لوجستية دقيقة بشكل ذكي. لتفعيل التحليل الحقيقي من معمارية هاتفتك، قم بتمكين إذن التتبع.", color = CosmicTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.appUsage) { app ->
                    AppDetailRowItem(
                        app = app,
                        showMah = state.showMahInStats,
                        capacity = state.batteryStatus.capacityMah
                    )
                }
            }
        }
    }
}

@Composable
fun AppDetailRowItem(
    app: AppUsageInfo,
    showMah: Boolean,
    capacity: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(BatteryBlue.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Text(
                            app.appName.firstOrNull()?.toString()?.uppercase() ?: "A",
                            color = BatteryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(app.appName, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(app.packageName, color = CosmicTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Format metric depending on ShowMah settings
                val formattedUsage = if (showMah) {
                    "${app.totalMah.toInt()} mAh"
                } else {
                    String.format(Locale("ar"), "%.1f٪", app.totalPercentage)
                }

                Text(
                    text = formattedUsage,
                    color = if (app.totalPercentage > 5f) RedAlert else BatteryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Background vs Foreground progress bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("في المقدمة", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text("${app.foregroundDurationMinutes} دقيقة", color = CosmicTextPrimary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (app.percentageForeground / 10f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = BatteryGreen,
                        trackColor = CosmicBorder
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("في الخلفية", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text("${app.backgroundDurationMinutes} دقيقة", color = CosmicTextPrimary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (app.percentageBackground / 10f).coerceIn(0f, 1f) },
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


// ==========================================
// INTERACTIVE ACTIVE SENSOR TEST SCREENS
// ==========================================

@Composable
fun CompassTestScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var rotationMatrix = remember { FloatArray(9) }
    var orientationAngles = remember { FloatArray(3) }
    
    var azimuth by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    var latestGravity = remember { FloatArray(3) }
    var latestGeomagnetic = remember { FloatArray(3) }

    DisposableEffect(Unit) {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        if (accel == null || mag == null) {
            isSensorAvailable = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, latestGravity, 0, event.values.size)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, latestGeomagnetic, 0, event.values.size)
                }

                val success = SensorManager.getRotationMatrix(rotationMatrix, null, latestGravity, latestGeomagnetic)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azRad = orientationAngles[0]
                    // Convert to degrees & normalize
                    var azDeg = Math.toDegrees(azRad.toDouble()).toFloat()
                    azDeg = (azDeg + 360) % 360
                    // Apply a simple low-pass filter
                    azimuth = azimuth + 0.15f * (azDeg - azimuth)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accel != null) sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        if (mag != null) sensorManager.registerListener(listener, mag, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Secondary simulated timer for visual fidelity if sensor is not available
    var simulatedAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(isSensorAvailable) {
        if (!isSensorAvailable) {
            while (true) {
                kotlinx.coroutines.delay(40)
                simulatedAngle = (simulatedAngle + 1.2f) % 360f
            }
        }
    }

    val finalAngle = if (isSensorAvailable) azimuth else simulatedAngle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "بوصلة تحديد الاتجاهات - نشط",
            fontWeight = FontWeight.Bold,
            color = CosmicTextPrimary,
            fontSize = 17.sp
        )

        if (!isSensorAvailable) {
            Surface(
                color = OrangeWarning.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrangeWarning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Text(
                    "ملاحظة: جهاز الحساس المغناطيسي غير متوفر على جهازك/المحاكي الحالي. يتم عرض محاكاة متحركة للمعايرة الدائرية للأهمية الفنية.",
                    color = OrangeWarning,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom drawn Canvas Compass Dial
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.width / 2) * 0.9f

                // Dial background outline
                drawCircle(
                    color = CosmicBorder,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Secondary ring
                drawCircle(
                    color = CosmicBorder.copy(alpha = 0.5f),
                    radius = radius - 15.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Draw ticks every 30 degrees
                for (angle in 0 until 360 step 30) {
                    val angleRad = Math.toRadians((angle - 90).toDouble())
                    val tickLength = if (angle % 90 == 0) 15.dp.toPx() else 8.dp.toPx()
                    val start = Offset(
                        (center.x + (radius - tickLength) * cos(angleRad)).toFloat(),
                        (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + radius * cos(angleRad)).toFloat(),
                        (center.y + radius * sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = if (angle % 90 == 0) BatteryGreen else CosmicBorder,
                        start = start,
                        end = end,
                        strokeWidth = if (angle % 90 == 0) 4.dp.toPx() else 2.dp.toPx()
                    )
                }

                // Draw Cardinal Arabic Points (RTL Support)
                // Need NativeCanvas to write Text beautifully
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val paintNorth = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    textSize = 16.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Compass letters
                drawContext.canvas.nativeCanvas.drawText("ش", center.x, center.y - radius + 32.dp.toPx(), paintNorth) // North
                drawContext.canvas.nativeCanvas.drawText("ج", center.x, center.y + radius - 20.dp.toPx(), paint) // South
                drawContext.canvas.nativeCanvas.drawText("ق", center.x + radius - 24.dp.toPx(), center.y + 5.dp.toPx(), paint) // East
                drawContext.canvas.nativeCanvas.drawText("غ", center.x - radius + 24.dp.toPx(), center.y + 5.dp.toPx(), paint) // West

                // Draw dynamic Needle pivoted toward azimuth
                val needleAngleRad = Math.toRadians((finalAngle - 90).toDouble())
                val needleTip = Offset(
                    (center.x + (radius - 40.dp.toPx()) * cos(needleAngleRad)).toFloat(),
                    (center.y + (radius - 40.dp.toPx()) * sin(needleAngleRad)).toFloat()
                )
                val backTip = Offset(
                    (center.x - 20.dp.toPx() * cos(needleAngleRad)).toFloat(),
                    (center.y - 20.dp.toPx() * sin(needleAngleRad)).toFloat()
                )

                // Draw Red Needle (Facing Target North)
                drawLine(
                    color = RedAlert,
                    start = center,
                    end = needleTip,
                    strokeWidth = 6.dp.toPx()
                )
                // Draw opposite Needle tail
                drawLine(
                    color = CosmicBorder,
                    start = center,
                    end = backTip,
                    strokeWidth = 4.dp.toPx()
                )

                // Center pivot cap
                drawCircle(color = BatteryBlue, radius = 6.dp.toPx(), center = center)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Degree print
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    String.format(Locale("ar"), "زاوية الدوران: %.1f°", finalAngle),
                    color = CosmicTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                val directionText = when {
                    finalAngle >= 337.5 || finalAngle < 22.5 -> "شمال مطلق"
                    finalAngle >= 22.5 && finalAngle < 67.5 -> "شمال شرقي"
                    finalAngle >= 67.5 && finalAngle < 112.5 -> "شرق"
                    finalAngle >= 112.5 && finalAngle < 157.5 -> "جنوب شرقي"
                    finalAngle >= 157.5 && finalAngle < 202.5 -> "جنوب مطلق"
                    finalAngle >= 202.5 && finalAngle < 247.5 -> "جنوب غربي"
                    finalAngle >= 247.5 && finalAngle < 292.5 -> "غرب"
                    else -> "شمال غربي"
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(directionText, color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LightTestScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var luxValue by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor == null) {
            isSensorAvailable = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    luxValue = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Simulated Slider value modifier if sensor not available
    var simulatedValue by remember { mutableStateOf(150f) }

    val finalLux = if (isSensorAvailable) luxValue else simulatedValue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "مقياس شدة الإضاءة المحيطة (Lux)",
            fontWeight = FontWeight.Bold,
            color = CosmicTextPrimary,
            fontSize = 17.sp
        )

        if (!isSensorAvailable) {
            Surface(
                color = OrangeWarning.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrangeWarning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "حساس الضوء الجانبي للأسف غير متاح حالياً. قم باستخدام المؤشر التفاعلي أدناه لتعديل محاكاة لوحة المقياس الذكية:",
                        color = OrangeWarning,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = simulatedValue,
                        onValueChange = { simulatedValue = it },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = OrangeWarning,
                            activeTrackColor = OrangeWarning
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Lux Dial Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            val progressFactor = (finalLux / 1000f).coerceIn(0f, 1f)
            CircularProgressIndicator(
                progress = { 1f },
                color = CosmicBorder,
                strokeWidth = 14.dp,
                modifier = Modifier.size(190.dp)
            )
            CircularProgressIndicator(
                progress = { progressFactor },
                color = OrangeWarning,
                strokeWidth = 14.dp,
                modifier = Modifier.size(190.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale("ar"), "%.1f", finalLux),
                    color = CosmicTextPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "لوكس (Lux)",
                    color = CosmicTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Diagnostic Description
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val ambianceCategory = when {
                    finalLux < 10 -> "مظلم تماماً (توفير طاقة أقصى للشاشة)"
                    finalLux < 150 -> "إضاءة خافتة بمكتب هادئ"
                    finalLux < 500 -> "إضاءة مريحة ومتزنة"
                    finalLux < 2000 -> "سطوع عالٍ ببيئة نهارية"
                    else -> "أشعة شمس مباشرة حادة (إضاءة شاشة كاملة)"
                }
                Text("تحليل البيئة الضوئية المحيطة:", color = CosmicTextSecondary, fontSize = 12.sp)
                Text(ambianceCategory, color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AccelerometerTestScreen() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    
    var xTilt by remember { mutableStateOf(0f) }
    var yTilt by remember { mutableStateOf(0f) }
    var zTilt by remember { mutableStateOf(10f) }
    
    var isSensorAvailable by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel == null) {
            isSensorAvailable = false
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // Dampen the sensor reads using a quick weight
                    xTilt = xTilt + 0.2f * (event.values[0] - xTilt)
                    yTilt = yTilt + 0.2f * (event.values[1] - yTilt)
                    zTilt = zTilt + 0.2f * (event.values[2] - zTilt)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accel != null) {
            sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Simulated Slider movement if sensor not available
    var simulatedX by remember { mutableStateOf(0.0f) }
    var simulatedY by remember { mutableStateOf(0.0f) }
    
    if (!isSensorAvailable) {
        LaunchedEffect(Unit) {
            var waveArg = 0.0
            while (true) {
                kotlinx.coroutines.delay(30)
                waveArg += 0.05
                simulatedX = (sin(waveArg) * 4f).toFloat()
                simulatedY = (cos(waveArg) * 4f).toFloat()
            }
        }
    }

    val finalX = if (isSensorAvailable) xTilt else simulatedX
    val finalY = if (isSensorAvailable) yTilt else simulatedY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "مقياس الجاذبية والحركة ثنائي المحور",
            fontWeight = FontWeight.Bold,
            color = CosmicTextPrimary,
            fontSize = 17.sp
        )

        if (!isSensorAvailable) {
            Surface(
                color = OrangeWarning.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrangeWarning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Text(
                    "ملاحظة: حساس مقياس التسارع غير نشط. يتم تقديم محاكاة متحركة للمستوى الجيرو-مغناطيسي للعرض فقط.",
                    color = OrangeWarning,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bubble Level Visualizer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(220.dp)
                .background(CosmicSurface, CircleShape)
                .border(2.dp, CosmicBorder, CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Draw inner bullseye targets
                drawCircle(color = CosmicBorder, radius = 30.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                drawCircle(color = CosmicBorder, radius = 60.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))

                // Map Tilt values (approx range -8 to 8) to coordinates
                val maxOffset = 90.dp.toPx()
                // Accelerometer raw values: negative tilt x moves bubble right, positive moves left. Y tilt positive moves down, negative up.
                val dx = -(finalX / 8f).coerceIn(-1f, 1f) * maxOffset
                val dy = (finalY / 8f).coerceIn(-1f, 1f) * maxOffset

                val bubbleCenter = Offset(center.x + dx, center.y + dy)

                // Draw Bubble (المؤشر العائم)
                drawCircle(
                    brush = Brush.radialGradient(listOf(BatteryBlue, BatteryBlue.copy(alpha = 0.6f))),
                    radius = 16.dp.toPx(),
                    center = bubbleCenter
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metric Readout Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("محور X (أفقي)", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text(String.format(Locale("ar"), "%.2f م/ث²", finalX), color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("محور Y (رأسي)", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text(String.format(Locale("ar"), "%.2f م/ث²", finalY), color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}


// ==========================================
// SYSTEM STATS EXTRACTION HELPERS
// ==========================================

fun getDetailedSoftwareInfo(context: Context): List<Pair<String, String>> {
    val df = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale("ar"))
    val buildTime = try {
        df.format(Date(Build.TIME))
    } catch (e: Exception) {
        "غير متوفر"
    }

    val uptimeMs = SystemClock.elapsedRealtime()
    val hours = uptimeMs / (1000 * 60 * 60)
    val minutes = (uptimeMs / (1000 * 60)) % 60
    val formattedUptime = "نشط منذ $hours ساعة و $minutes دقيقة"

    val patch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Build.VERSION.SECURITY_PATCH
    } else {
        "غير متوفر"
    }

    // Checking Root
    val isRooted = checkRootStatus()
    val rootStr = if (isRooted) "نعم، النظام مكسور الحماية (Rooted)" else "لا، نظام حماية محكم أصلي"

    return listOf(
        "اسم الجهاز التسويقي" to Build.MODEL,
        "الجهة المصنعة للجهاز" to Build.MANUFACTURER,
        "العلامة التجارية الحالية" to Build.BRAND,
        "إصدار أندرويد" to Build.VERSION.RELEASE,
        "مستوى واجهة برمجة التطبيقات (API)" to Build.VERSION.SDK_INT.toString(),
        "تاريخ بناء السوفتوير" to buildTime,
        "تحديث تصحيح الأمان للنظام" to patch,
        "رقم البناء (Build ID)" to Build.ID,
        "نوع البناء ونوع الترخيص" to Build.TYPE,
        "اللوحة الأم للهاتف (Board)" to Build.BOARD,
        "المعالج الافتراضي المكتشف" to Build.HARDWARE,
        "مدة العمل منذ آخر تشغيل" to formattedUptime,
        "صلاحيات الجذر الحالية" to rootStr
    )
}

fun getDetailedHardwareInfo(context: Context): List<Pair<String, String>> {
    val abis = Build.SUPPORTED_ABIS.joinToString(", ")
    val cores = Runtime.getRuntime().availableProcessors()

    // RAM memory querying
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f)
    val availRamGb = memoryInfo.availMem / (1024f * 1024f * 1024f)
    val totalRamStr = String.format(Locale("ar"), "%.2f جيجابايت", totalRamGb)
    val availRamStr = String.format(Locale("ar"), "%.2f جيجابايت متوفر", availRamGb)

    // Resolution & Display Metrics
    val dMetrics = context.resources.displayMetrics
    val rgbWidth = dMetrics.widthPixels
    val rgbHeight = dMetrics.heightPixels
    val density = dMetrics.densityDpi
    val resolutionStr = "$rgbWidth x $rgbHeight بكسل"
    val densityStr = "$density DPI (دوت بكسل)"

    return listOf(
        "معمارية المعالج (ABI)" to abis,
        "أنوية المعالج النشطة" to "$cores أنوية مستقرة",
        "لوحة الهاردوير للهاتف" to Build.HARDWARE,
        "إجمالي الذاكرة العشوائية RAM" to totalRamStr,
        "الذاكرة العشوائية المتوفرة" to availRamStr,
        "دقة وأبعاد الشاشة" to resolutionStr,
        "نسبة كثافة الشاشة" to densityStr,
        "معالج الرسوميات الأولي الكاشف" to if (Build.VERSION.SDK_INT >= 21) Build.SUPPORTED_ABIS[0] else "مدمج"
    )
}

fun checkRootStatus(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )
    for (path in paths) {
        if (java.io.File(path).exists()) return true
    }
    return false
}


// ==========================================
// PRE-EXISTING ACCIDENTALLY OVERRIDE CARD UI
// ==========================================

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
            // Gauge indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(160.dp),
                    color = CosmicBorder,
                    strokeWidth = 10.dp
                )
                CircularProgressIndicator(
                    progress = { status.percentage / 100f },
                    modifier = Modifier.size(160.dp),
                    color = when {
                        status.percentage > 30 -> BatteryGreen
                        status.percentage > 15 -> OrangeWarning
                        else -> RedAlert
                    },
                    strokeWidth = 10.dp
                )

                Column(
                    modifier = Modifier.semantics(mergeDescendants = true) {},
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

            Surface(
                color = CosmicCardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .semantics(mergeDescendants = true) {},
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

            HorizontalDivider(color = CosmicBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Favorite, "حالة البطارية الصحية", tint = RedAlert, modifier = Modifier.size(20.dp))
                    Text("صحة البطارية", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text(status.health, color = CosmicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).height(40.dp).background(CosmicBorder))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Thermostat, "درجة حرارة الجهاز", tint = OrangeWarning, modifier = Modifier.size(20.dp))
                    Text("درجة الحرارة", color = CosmicTextSecondary, fontSize = 11.sp)
                    Text("${status.temperature}°م", color = CosmicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.width(1.dp).height(40.dp).background(CosmicBorder))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Bolt, "السعة القصوى المتوقعة للبطارية", tint = BatteryBlue, modifier = Modifier.size(20.dp))
                    Text("السعة المقدرة", color = CosmicTextSecondary, fontSize = 11.sp)
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
                "الاستهلاك المقدر للعتاد والنظام",
                color = CosmicTextPrimary,
                fontSize = 15.sp,
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
                    else -> Icons.Default.Devices
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = BatteryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(hw.arabicName, color = CosmicTextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale("ar"), "%.1f٪", hw.percentage),
                            color = CosmicTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${hw.consumptionMah} mAh",
                            color = CosmicTextSecondary,
                            fontSize = 11.sp
                        )
                    }
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
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(1.dp, RedAlert.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ReportProblem, null, tint = RedAlert, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "تحذيرات استنزاف الطاقة المكتشفة",
                    color = CosmicTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(1f)
                )
            }

            alerts.forEach { alert ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedAlert.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, RedAlert.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .semantics(mergeDescendants = true) {}
                ) {
                    Text(alert.title, color = RedAlert, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(alert.description, color = CosmicTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TabRamHardware(
    state: BatteryUiState.Success,
    onShowMore: () -> Unit
) {
    val specs = state.hardwareSpecs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "مراقبة موارد الذاكرة والعتاد بالوقت الفعلي",
            color = CosmicTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        // 1. Memory (RAM) Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = BatteryGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "الذاكرة العشوائية (RAM)",
                        color = CosmicTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${String.format(Locale.ENGLISH, "%.1f", specs.ramUsedPercent)}%",
                        color = BatteryGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smooth progress indicator
                LinearProgressIndicator(
                    progress = { specs.ramUsedPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = BatteryGreen,
                    trackColor = CosmicBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("مستعملة", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text(String.format(Locale.ENGLISH, "%.2f جيجا", specs.usedRamGb), color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("متاحة وحرة", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text(String.format(Locale.ENGLISH, "%.2f جيجا", specs.freeRamGb), color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("إجمالي السعة", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text(String.format(Locale.ENGLISH, "%.1f جيجا", specs.totalRamGb), color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // 2. Processor (CPU) Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = BatteryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "وحدة المعالجة المركزية (CPU)",
                        color = CosmicTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${String.format(Locale.ENGLISH, "%.1f", specs.cpuUsagePercent)}%",
                        color = BatteryBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { specs.cpuUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = BatteryBlue,
                    trackColor = CosmicBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("طراز المعالج (SoC)", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text(specs.cpuModel, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("حالة كفاءة النواة", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text("ذكية ونشطة", color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. Graphics Processor (GPU) Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrangeWarning, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "معالج الرسوميات (GPU)",
                        color = CosmicTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${String.format(Locale.ENGLISH, "%.1f", specs.gpuUsagePercent)}%",
                        color = OrangeWarning,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { specs.gpuUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = OrangeWarning,
                    trackColor = CosmicBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("محرك الرسوم والتطبيقات", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text(specs.gpuModel, color = CosmicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("كفاءة الترميز", color = CosmicTextSecondary, fontSize = 11.sp)
                        Text("استهلاك محسن", color = BatteryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large view detail button
        Button(
            onClick = onShowMore,
            colors = ButtonDefaults.buttonColors(containerColor = BatteryGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("شرح وإدارة التطبيقات التي تستهلك الرام", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RamUsageDetailsScreenContent(
    state: BatteryUiState.Success,
    onIntent: (BatteryIntent) -> Unit,
    onBack: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val specs = state.hardwareSpecs
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Watch RAM optimization and trigger dialog or snackbar feedback when completed
    LaunchedEffect(state.isRamCleaned) {
        if (state.isRamCleaned) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "تم تنظيف الذاكرة العشوائية وتحرير المساحة وإيقاف التطبيقات بنجاح!",
                    actionLabel = "حسناً"
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        Scaffold(
            containerColor = CosmicBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurface)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "عودة",
                            tint = CosmicTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تفاصيل استهلاك الذاكرة وإدارة الرام",
                        color = CosmicTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Sorting dropdown
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "ترتيب", tint = CosmicTextPrimary)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(CosmicSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("الفرز حسب حجم الاستهلاك", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleRamSorting(RamSortType.BY_USAGE))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الفرز حسب الاسم أبجدياً", color = CosmicTextPrimary) },
                                onClick = {
                                    onIntent(BatteryIntent.ToggleRamSorting(RamSortType.BY_NAME))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Main scrolling content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Status Summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(BatteryGreen.copy(alpha = 0.15f), CircleShape)
                                    .border(1.5.dp, BatteryGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = BatteryGreen, modifier = Modifier.size(26.dp))
                            }
                            Column {
                                Text(
                                    text = "الحالة الراهنة لذاكرة رام",
                                    color = CosmicTextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "المستعمل ${String.format(Locale.ENGLISH, "%.2f", specs.usedRamGb)} جيجا / حرة ${String.format(Locale.ENGLISH, "%.2f", specs.freeRamGb)} جيجا",
                                    color = CosmicTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التطبيقات والعمليات النشطة بالذاكرة (${specs.ramAppsList.size})",
                            color = CosmicTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.ramSorting == RamSortType.BY_USAGE) "الترتيب: حسب الاستهلاك" else "الترتيب: أبجدياً",
                            color = BatteryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Active processes list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(specs.ramAppsList, key = { it.packageName + "_" + it.ramUsageMb }) { app ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CosmicBorder.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(if (app.isSystemProcess) CosmicCardBg else CosmicBorder, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (app.isSystemProcess) {
                                                Icon(Icons.Default.Settings, contentDescription = null, tint = CosmicTextSecondary, modifier = Modifier.size(20.dp))
                                            } else {
                                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BatteryBlue, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = app.appName,
                                                color = CosmicTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (app.isSystemProcess) "عملية من نظام التشغيل الأساسي" else app.packageName,
                                                color = CosmicTextSecondary,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.ENGLISH, "%.1f", app.ramUsageMb)} م.ب",
                                            color = if (app.ramUsageMb > 250.0) RedAlert else CosmicTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // RAM Clean Button at the absolute bottom
                Button(
                    onClick = { onIntent(BatteryIntent.CleanRam) },
                    colors = ButtonDefaults.buttonColors(containerColor = BatteryGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنظيف الرام وتحسين الذاكرة والعشوائية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
