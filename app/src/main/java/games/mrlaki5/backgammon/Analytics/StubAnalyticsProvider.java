package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Stub analytics provider for development/testing.
 * Logs all events to Logcat. Replace with a real SDK in production.
 */
public class StubAnalyticsProvider implements AnalyticsProvider {

    private static final String TAG = "Analytics";
    private long sessionStartTime;

    @Override
    public void initialize(Context context) {
        Log.d(TAG, "Analytics initialized (stub)");
    }

    @Override
    public void logEvent(String eventName, Bundle params) {
        StringBuilder sb = new StringBuilder("Event: ").append(eventName);
        if (params != null) {
            sb.append(" {");
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append(", ");
            }
            sb.append("}");
        }
        Log.d(TAG, sb.toString());
    }

    @Override
    public void setUserProperty(String name, String value) {
        Log.d(TAG, "UserProperty: " + name + " = " + value);
    }

    @Override
    public void setUserId(String userId) {
        Log.d(TAG, "UserId: " + userId);
    }

    @Override
    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
        Log.d(TAG, "Session started");
    }

    @Override
    public void endSession() {
        long duration = (System.currentTimeMillis() - sessionStartTime) / 1000;
        Log.d(TAG, "Session ended. Duration: " + duration + "s");
    }
}
