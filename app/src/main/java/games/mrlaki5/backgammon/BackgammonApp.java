package games.mrlaki5.backgammon;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import games.mrlaki5.backgammon.Analytics.AnalyticsProvider;
import games.mrlaki5.backgammon.Diagnostics.Connectivity;
import games.mrlaki5.backgammon.Diagnostics.StartupWatchdog;
import games.mrlaki5.backgammon.Analytics.CrashReporter;
import games.mrlaki5.backgammon.Analytics.FirebaseAnalyticsProvider;
import games.mrlaki5.backgammon.Analytics.FirebaseCrashReporter;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Analytics.StubAnalyticsProvider;
import games.mrlaki5.backgammon.Analytics.StubCrashReporter;

import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.TapsellPlusInitListener;
import ir.tapsell.plus.model.AdNetworks;
import ir.tapsell.plus.model.AdNetworkError;

/**
 * Custom Application class for global SDK initialization.
 *
 * Initialization order:
 * 1. TapsellPlus (ads)
 * 2. Analytics (Firebase or Stub)
 * 3. Audio
 */
public class BackgammonApp extends Application {

    private static final String TAG = "BackgammonApp";

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch_done";

    private static final long ADS_INIT_DELAY_MS = 1500L;
    private static volatile boolean adsReady = false;

    /**
     * Every SDK hand-off happens here, never on the main thread. A reachable network is
     * not the same thing as a reachable server: on a filtered or throttled connection the
     * device reports itself online while the SDK's first request hangs for tens of
     * seconds. On the main thread that is an ANR; on this thread it costs nothing.
     */
    private final java.util.concurrent.ExecutorService sdkExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "sdk-init");
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });

    private boolean analyticsInitialized = false;
    private int activeActivityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Debug builds report what blocks the main thread instead of just freezing
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());
            StartupWatchdog.start();
        }

        // Ads are initialized after the first frame, and never at the cost of startup:
        // on a slow or filtered network this SDK call can stall the main thread (ANR).
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> sdkExecutor.execute(this::initializeAds), ADS_INIT_DELAY_MS);

        // Initialize menu audio (click + music)
        MenuAudioManager.get().init(this);

        // Register activity lifecycle for analytics initialization and session tracking
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (!analyticsInitialized) {
                    analyticsInitialized = true;
                    // Analytics/Crashlytics setup also touches the network; keep it off the
                    // first frame and never let it break startup.
                    sdkExecutor.execute(() -> {
                        try {
                            if (!Connectivity.isOnline(BackgammonApp.this)) {
                                Log.d(TAG, "No connection — analytics stays on the stub provider");
                                return;
                            }
                            initializeAnalytics(BackgammonApp.this);
                        } catch (Throwable t) {
                            Log.w(TAG, "Analytics initialization threw", t);
                        }
                    });
                }
            }

            @Override public void onActivityStarted(Activity activity) {
                activeActivityCount++;
                if (activeActivityCount == 1) {
                    // App came to foreground — session starts
                    GameAnalytics.get().trackSessionStart();
                }
            }

            @Override public void onActivityResumed(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}

            @Override public void onActivityStopped(Activity activity) {
                activeActivityCount--;
                if (activeActivityCount == 0) {
                    // All activities stopped — app went to background — session ends
                    GameAnalytics.get().trackSessionEnd();
                }
            }

            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    private void initializeAds() {
        if (!Connectivity.isOnline(this)) {
            // Offline: starting the ad SDK would only risk a stall
            Log.d(TAG, "No connection — skipping ad SDK initialization");
            return;
        }
        try {
            TapsellPlus.setDebugMode(Log.DEBUG);
            TapsellPlus.initialize(this, BuildConfig.TAPSELL_APP_KEY, new TapsellPlusInitListener() {
                @Override
                public void onInitializeSuccess(AdNetworks adNetworks) {
                    adsReady = true;
                    Log.d(TAG, "TapsellPlus initialized successfully");
                }

                @Override
                public void onInitializeFailed(AdNetworks adNetworks, AdNetworkError adNetworkError) {
                    Log.w(TAG, "TapsellPlus init failed: " + adNetworkError.getErrorMessage());
                }
            });
        } catch (Throwable t) {
            // The game must start even when the ad SDK cannot
            Log.w(TAG, "TapsellPlus initialization threw", t);
        }
    }

    /** True once the ad SDK reported success; ad calls before that are skipped. */
    public static boolean areAdsReady() {
        return adsReady;
    }

    private void initializeAnalytics(android.content.Context context) {
        // Use Firebase Analytics as the real provider
        // Falls back gracefully if google-services.json is missing
        AnalyticsProvider provider;
        try {
            provider = new FirebaseAnalyticsProvider();
            Log.d(TAG, "Using Firebase Analytics provider");
        } catch (Exception e) {
            Log.w(TAG, "Firebase unavailable, using stub analytics", e);
            provider = new StubAnalyticsProvider();
        }

        CrashReporter crashReporter;
        try {
            crashReporter = new FirebaseCrashReporter();
        } catch (Exception e) {
            Log.w(TAG, "Firebase Crashlytics unavailable, using stub crash reporter", e);
            crashReporter = new StubCrashReporter();
        }

        GameAnalytics.init(context, provider, crashReporter);

        // Track app open
        GameAnalytics.get().trackAppOpen();

        // Track first launch (one-time)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_FIRST_LAUNCH, false)) {
            GameAnalytics.get().trackFirstLaunch();
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply();
        }
    }
}
