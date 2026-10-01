package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Real crash reporter backed by Firebase Crashlytics.
 *
 * Fatal (uncaught) crashes are captured automatically once initialized and are
 * uploaded the next time the app starts with network access — no extra wiring
 * needed. reportException()/log()/setKey() add context for the next fatal report
 * or can be sent immediately as non-fatal events.
 *
 * Requires google-services.json in app/ (already present in this project) and the
 * Crashlytics Gradle plugin (applied conditionally in app/build.gradle).
 */
public class FirebaseCrashReporter implements CrashReporter {

    private static final String TAG = "FirebaseCrashReporter";

    private FirebaseCrashlytics crashlytics;

    @Override
    public void initialize(Context context) {
        try {
            crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCrashlyticsCollectionEnabled(true);
            Log.d(TAG, "Firebase Crashlytics initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Crashlytics", e);
        }
    }

    @Override
    public void reportException(Throwable throwable) {
        if (crashlytics == null) return;
        try {
            crashlytics.recordException(throwable);
        } catch (Exception e) {
            Log.e(TAG, "Error recording exception", e);
        }
    }

    @Override
    public void reportException(Throwable throwable, String message) {
        if (crashlytics == null) return;
        try {
            crashlytics.log(message);
            crashlytics.recordException(throwable);
        } catch (Exception e) {
            Log.e(TAG, "Error recording exception with message", e);
        }
    }

    @Override
    public void setKey(String key, String value) {
        if (crashlytics == null) return;
        try {
            crashlytics.setCustomKey(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom key: " + key, e);
        }
    }

    @Override
    public void setUserId(String userId) {
        if (crashlytics == null) return;
        try {
            crashlytics.setUserId(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error setting user id", e);
        }
    }

    @Override
    public void log(String message) {
        if (crashlytics == null) return;
        try {
            crashlytics.log(message);
        } catch (Exception e) {
            Log.e(TAG, "Error logging breadcrumb", e);
        }
    }

    @Override
    public void testCrash() {
        throw new RuntimeException("Test crash from FirebaseCrashReporter");
    }
}
