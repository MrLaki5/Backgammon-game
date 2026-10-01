package games.mrlaki5.backgammon.Analytics;

import android.content.Context;

/**
 * Abstraction for crash/error reporting. Implementations can use:
 * - Sentry (self-hosted or cloud, Iran-accessible with proper endpoint)
 * - ACRA (open-source, no Google dependency, sends to email/custom backend)
 * - Firebase Crashlytics (may be blocked/slow in Iran)
 *
 * The chosen solution must be accessible from Iran without VPN.
 */
public interface CrashReporter {

    /**
     * Initializes the crash reporter. Call once at app startup.
     */
    void initialize(Context context);

    /**
     * Reports a non-fatal exception (caught error that doesn't crash the app).
     */
    void reportException(Throwable throwable);

    /**
     * Reports a non-fatal exception with additional context message.
     */
    void reportException(Throwable throwable, String message);

    /**
     * Sets a key-value breadcrumb for debugging crash context.
     */
    void setKey(String key, String value);

    /**
     * Sets the user identifier for associating crashes with users.
     */
    void setUserId(String userId);

    /**
     * Logs a breadcrumb message (shown in crash reports for context).
     */
    void log(String message);

    /**
     * Forces a test crash (for verifying the reporter works in production).
     */
    void testCrash();
}
