package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Real analytics provider backed by Firebase Analytics.
 *
 * Architecture:
 *   GameAnalytics (facade) → AnalyticsProvider (interface) → FirebaseAnalyticsProvider → Firebase SDK
 *
 * Notes:
 * - Firebase batches events and uploads periodically (not real-time in production)
 * - Works in Iran: SDK queues events locally and sends when connection is available
 * - Requires google-services.json in app/ directory
 * - Max 500 event types, 25 params per event, 40-char event names (all respected in AnalyticsEvent)
 */
public class FirebaseAnalyticsProvider implements AnalyticsProvider {

    private static final String TAG = "FirebaseAnalytics";

    private FirebaseAnalytics firebaseAnalytics;
    private long sessionStartTime;

    @Override
    public void initialize(Context context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            // Enable analytics collection (can be toggled for GDPR if needed)
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);
            Log.d(TAG, "Firebase Analytics initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Analytics", e);
            // Graceful degradation — will log to Logcat if firebase fails
        }
    }

    @Override
    public void logEvent(String eventName, Bundle params) {
        if (firebaseAnalytics == null) {
            Log.w(TAG, "Firebase not initialized, dropping event: " + eventName);
            return;
        }
        try {
            firebaseAnalytics.logEvent(eventName, params);
        } catch (Exception e) {
            Log.e(TAG, "Error logging event: " + eventName, e);
        }
    }

    @Override
    public void setUserProperty(String name, String value) {
        if (firebaseAnalytics == null) return;
        try {
            firebaseAnalytics.setUserProperty(name, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting user property: " + name, e);
        }
    }

    @Override
    public void setUserId(String userId) {
        if (firebaseAnalytics == null) return;
        try {
            firebaseAnalytics.setUserId(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error setting user ID", e);
        }
    }

    @Override
    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
        // Firebase auto-tracks sessions, but we log our own for granular control
        logEvent(AnalyticsEvent.SESSION_START, null);
    }

    @Override
    public void endSession() {
        long durationSec = (System.currentTimeMillis() - sessionStartTime) / 1000;
        Bundle params = new Bundle();
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSec);
        logEvent(AnalyticsEvent.SESSION_END, params);
    }
}
