package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.util.Log;

/**
 * Stub crash reporter for development.
 * Logs exceptions to Logcat instead of sending to a remote service.
 * Replace with Sentry/ACRA implementation in production.
 */
public class StubCrashReporter implements CrashReporter {

    private static final String TAG = "CrashReporter";

    @Override
    public void initialize(Context context) {
        Log.d(TAG, "Crash reporter initialized (stub)");
        // In production, set up the uncaught exception handler here
    }

    @Override
    public void reportException(Throwable throwable) {
        Log.e(TAG, "Non-fatal exception", throwable);
    }

    @Override
    public void reportException(Throwable throwable, String message) {
        Log.e(TAG, "Non-fatal: " + message, throwable);
    }

    @Override
    public void setKey(String key, String value) {
        Log.d(TAG, "Key: " + key + " = " + value);
    }

    @Override
    public void setUserId(String userId) {
        Log.d(TAG, "CrashReporter userId: " + userId);
    }

    @Override
    public void log(String message) {
        Log.d(TAG, "Breadcrumb: " + message);
    }

    @Override
    public void testCrash() {
        throw new RuntimeException("Test crash from StubCrashReporter");
    }
}
