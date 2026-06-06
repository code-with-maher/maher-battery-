# Strict and Aggressive R8 rules for PowerPulse release optimize
# Maximize code flattening and shrink resources

# Suppress all unimportant compiler and framework warnings
-dontwarn **

# Allow R8 to perform aggressive optimization on visibility access modifiers and class structures
-allowaccessmodification
-repackageclasses ''

# Extreme optimization passes to shrink class definitions further
-optimizationpasses 5

# Strip developer / debug logs completely for clean execution and smaller binary size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Preserve Main Activity entry point
-keep class com.maher.powerpulse.MainActivity { *; }

# Keep Compose function markers
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

