package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.os.Bundle;

/**
 * Abstraction for analytics tracking. Implementations can use:
 * - Firebase Analytics (may be slow/filtered in Iran)
 * - Matomo/Piwik (self-hosted, Iran-accessible)
 * - A native Iranian analytics SDK
 * - Custom backend logging
 *
 * Decision on which SDK to use is deferred to the developer — this interface
 * allows swapping without touching game code.
 */
public interface AnalyticsProvider {

    /**
     * Initializes the analytics SDK. Call once at app startup.
     */
    void initialize(Context context);

    /**
     * Logs an event with the given name and parameters.
     *
     * @param eventName One of the constants from {@link AnalyticsEvent}
     * @param params Optional key-value parameters (use Bundle). Null if no params.
     */
    void logEvent(String eventName, Bundle params);

    /**
     * Sets a user property (e.g., preferred language, player level).
     */
    void setUserProperty(String name, String value);

    /**
     * Sets the user ID for cross-device tracking (optional, only after auth).
     */
    void setUserId(String userId);

    /**
     * Logs the start of a timed session. Call in onResume of main activity.
     */
    void startSession();

    /**
     * Logs the end of a session with duration. Call in onPause of main activity.
     */
    void endSession();
}
