package games.mrlaki5.backgammon.Monetization.ads;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks rewarded ad usage per placement to enforce frequency caps.
 * Global limit: max 5 rewarded ads per day across ALL placements.
 * Global cooldown: 60 seconds between any two rewarded ads.
 */
public class RewardedAdTracker {

    private static final String PREFS_NAME = "rewarded_ad_tracker_prefs";
    private static final String KEY_TOTAL_TODAY = "total_today";
    private static final String KEY_LAST_AD_TIME = "last_ad_time";
    private static final String KEY_LAST_DAY = "last_day";
    private static final int MAX_TOTAL_PER_DAY = 5;
    private static final int GLOBAL_COOLDOWN_MS = 60_000;

    private final SharedPreferences prefs;

    public RewardedAdTracker(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        refreshIfNewDay();
    }

    private long getLastDayEpoch() {
        try {
            return prefs.getLong(KEY_LAST_DAY, -1L);
        } catch (ClassCastException e) {
            return -1L;
        }
    }

    private synchronized void refreshIfNewDay() {
        long today = System.currentTimeMillis() / 86400000L;
        if (today != getLastDayEpoch()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_LAST_DAY, today);
            editor.putInt(KEY_TOTAL_TODAY, 0);
            for (RewardedAdPlacement p : RewardedAdPlacement.values()) {
                editor.putInt("count_" + p.id, 0);
            }
            editor.commit();
        }
    }

    /**
     * Returns true if the given placement can show a rewarded ad right now.
     */
    public synchronized boolean canShow(RewardedAdPlacement placement) {
        refreshIfNewDay();

        // Global daily limit
        if (prefs.getInt(KEY_TOTAL_TODAY, 0) >= MAX_TOTAL_PER_DAY) return false;

        // Global cooldown
        long lastTime = prefs.getLong(KEY_LAST_AD_TIME, 0);
        if (System.currentTimeMillis() - lastTime < GLOBAL_COOLDOWN_MS) return false;

        // Per-placement daily limit
        if (placement.maxPerDay > 0) {
            int count = prefs.getInt("count_" + placement.id, 0);
            if (count >= placement.maxPerDay) return false;
        }

        // Per-placement cooldown
        if (placement.cooldownSeconds > 0) {
            long lastPlacement = prefs.getLong("last_" + placement.id, 0);
            if (System.currentTimeMillis() - lastPlacement < placement.cooldownSeconds * 1000L) return false;
        }

        return true;
    }

    /**
     * Records that a rewarded ad was shown for the given placement.
     * Call AFTER the ad is successfully shown and reward granted.
     */
    public synchronized void recordShow(RewardedAdPlacement placement) {
        long now = System.currentTimeMillis();
        prefs.edit()
                .putInt(KEY_TOTAL_TODAY, prefs.getInt(KEY_TOTAL_TODAY, 0) + 1)
                .putLong(KEY_LAST_AD_TIME, now)
                .putInt("count_" + placement.id, prefs.getInt("count_" + placement.id, 0) + 1)
                .putLong("last_" + placement.id, now)
                .commit();
    }

    /** Returns how many times the placement was used today. */
    public synchronized int getUsageToday(RewardedAdPlacement placement) {
        refreshIfNewDay();
        return prefs.getInt("count_" + placement.id, 0);
    }

    /** Returns remaining global daily rewarded ads. */
    public synchronized int getRemainingToday() {
        refreshIfNewDay();
        return MAX_TOTAL_PER_DAY - prefs.getInt(KEY_TOTAL_TODAY, 0);
    }

    /**
     * Returns true if the placement (or the global daily cap) has used up
     * all of today's allowance — i.e. it won't become available again until tomorrow.
     */
    public synchronized boolean isDailyLimitReached(RewardedAdPlacement placement) {
        refreshIfNewDay();
        if (prefs.getInt(KEY_TOTAL_TODAY, 0) >= MAX_TOTAL_PER_DAY) return true;
        if (placement.maxPerDay > 0 && prefs.getInt("count_" + placement.id, 0) >= placement.maxPerDay) return true;
        return false;
    }

    /**
     * Seconds remaining before this placement can show again, considering both the
     * per-placement cooldown and the global cross-placement cooldown. 0 = ready now
     * (though the daily cap may still be blocking it — check isDailyLimitReached too).
     */
    public synchronized long getCooldownRemainingSeconds(RewardedAdPlacement placement) {
        refreshIfNewDay();
        long now = System.currentTimeMillis();
        long remainingMs = GLOBAL_COOLDOWN_MS - (now - prefs.getLong(KEY_LAST_AD_TIME, 0));

        if (placement.cooldownSeconds > 0) {
            long lastPlacement = prefs.getLong("last_" + placement.id, 0);
            long placementRemaining = placement.cooldownSeconds * 1000L - (now - lastPlacement);
            remainingMs = Math.max(remainingMs, placementRemaining);
        }
        return Math.max(0, remainingMs) / 1000L;
    }
}
