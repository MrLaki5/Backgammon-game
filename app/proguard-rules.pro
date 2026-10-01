# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Strip all android.util.Log calls from release builds. Firebase Crashlytics handles crash
# reporting; verbose debug logs have no value in production and expose implementation details.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(java.lang.String, java.lang.String);
    public static int v(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int d(java.lang.String, java.lang.String);
    public static int d(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int i(java.lang.String, java.lang.String);
    public static int i(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.Throwable);
}

# Keep source file names and line numbers for readable crash reports in Firebase Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# RoyalOnBoardImage customizes these legacy renderer paints via reflection.
-keepclassmembers class games.mrlaki5.backgammon.GameView.OnBoardImage {
    private android.graphics.Paint RedChipPaint;
    private android.graphics.Paint WhiteChipPaint;
    private android.graphics.Paint BorderChipPaint;
    private android.graphics.Paint TextPaint;
}

# ShopItem IDs are stored in SharedPreferences and matched at runtime — must not be renamed.
-keepclassmembers class games.mrlaki5.backgammon.Economy.ShopItem {
    public final java.lang.String id;
    public java.lang.String[] bundleContents;
}

# PlayerProfileManager key constants are compared against stored strings at runtime.
-keepclassmembers class games.mrlaki5.backgammon.Database.PlayerProfileManager {
    private static final java.lang.String KEY_*;
}

# Keep the full Economy package accessible — item IDs, categories, and rarities are matched by name.
-keep class games.mrlaki5.backgammon.Economy.ShopItem$Category { *; }
-keep class games.mrlaki5.backgammon.Economy.ShopItem$Rarity { *; }
-keep class games.mrlaki5.backgammon.Economy.ShopItem$Acquisition { *; }

# Season and catalogue classes reference item IDs by string — keep their public surface.
-keepnames class games.mrlaki5.backgammon.Economy.Season { *; }
-keepnames class games.mrlaki5.backgammon.Economy.SeasonCatalogue { *; }

# GameReviewData / TurnRecord — serialised to SharedPreferences as JSON; field names must survive.
-keepclassmembers class games.mrlaki5.backgammon.Analysis.** {
    <fields>;
}

# ChallengeCode uses string parsing; keep the class name stable across builds.
-keepnames class games.mrlaki5.backgammon.Economy.ChallengeCode { *; }

# Tapsell Plus SDK references optional ad-network adapters that are not bundled.
# Suppress R8 missing-class warnings for all of them.
-dontwarn com.adcolony.**
-dontwarn com.unity3d.ads.**
-dontwarn com.unity3d.services.**
-dontwarn com.vungle.**
-dontwarn com.ironsource.**
-dontwarn com.applovin.**
-dontwarn com.chartboost.**
-dontwarn com.mopub.**
-dontwarn ir.tapsell.plus.adnetworks.**
# AdMob / IMA SDK — referenced by Tapsell but not in the foss/bazaar dependency tree
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.interactivemedia.**
