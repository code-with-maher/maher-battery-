package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.*
import com.example.presentation.*
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockState = BatteryUiState.Success(
        batteryStatus = BatteryStatusInfo(
            percentage = 85,
            health = "ممتاز",
            temperature = 36.5f,
            voltage = 3.8f,
            chargingStatus = "يتم التفريغ (على البطارية)",
            capacityMah = 4500,
            remainingHours = 14.2f
        ),
        hardwareUsage = listOf(
            HardwareUsageInfo("screen", "الشاشة", 12.5f, 562.5f),
            HardwareUsageInfo("wifi", "الواي فاي", 4.2f, 189.0f),
            HardwareUsageInfo("mobile_network", "شبكة الجوال", 5.0f, 225.0f),
            HardwareUsageInfo("android_system", "نظام أندرويد", 2.8f, 126.0f),
            HardwareUsageInfo("standby", "وضع الاستعداد", 2.2f, 99.0f),
            HardwareUsageInfo("camera", "الكاميرا", 1.1f, 49.5f)
        ),
        appUsage = listOf(
            AppUsageInfo("com.google.android.youtube", "يوتيوب", 4.5f, 2.0f, 202.5f, 90.0f, 45, 120),
            AppUsageInfo("com.whatsapp", "واتساب", 3.0f, 1.5f, 135.0f, 67.5f, 65, 240)
        ),
        anomalyAlerts = listOf(
            AnomalyAlert("temp_high", "ارتفاع ملحوظ في الحرارة", "وصلت حرارة الجهاز إلى 38.5 درجة مئوية.", AlertSeverity.HIGH, "درجة الحرارة", false)
        ),
        appSorting = AppSortType.TOTAL_CONSUMPTION,
        isPermissionGranted = true
    )

    composeTestRule.setContent {
        MyApplicationTheme {
            DashboardScreen(
                state = mockState,
                onIntent = {},
                onNavigateToApps = {}
            )
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
