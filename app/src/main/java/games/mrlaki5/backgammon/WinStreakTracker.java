package games.mrlaki5.backgammon;

import android.content.Context;
import android.content.SharedPreferences;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Economy.CoinManager;

/**
 * Tracks the player's current win streak (consecutive wins vs AI).
 * Resets on loss or quit. Does not count Pass & Play or Tutorial.
 *
 * Stored in SharedPreferences. Lightweight, offline, no backend needed.
 *
 * Retention purpose: motivates "one more game" to keep the streak alive.
 */
public class WinStreakTracker {

    private static final String PREFS_NAME = "win_streak_prefs";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_BEST_STREAK = "best_streak";

    private static final String MILESTONE_PREFS = "streak_milestone_prefs";
    private static final int[] MILESTONES = {5, 10, 25, 50, 100};
    private static final int[] MILESTONE_REWARDS = {30, 75, 150, 300, 500};

    private final SharedPreferences prefs;
    private final Context context;

    public WinStreakTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Records a win. Increments the streak and grants one-time milestone rewards.
     * @return the new streak value
     */
    public int recordWin() {
        int current = prefs.getInt(KEY_CURRENT_STREAK, 0) + 1;
        int best = prefs.getInt(KEY_BEST_STREAK, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_CURRENT_STREAK, current);
        if (current > best) {
            editor.putInt(KEY_BEST_STREAK, current);
        }
        editor.apply();
        checkAndGrantMilestones(current);
        return current;
    }

    private void checkAndGrantMilestones(int streak) {
        SharedPreferences milestonePrefs = context.getSharedPreferences(MILESTONE_PREFS, Context.MODE_PRIVATE);
        for (int i = 0; i < MILESTONES.length; i++) {
            if (streak >= MILESTONES[i]
                    && !milestonePrefs.getBoolean("milestone_" + MILESTONES[i], false)) {
                milestonePrefs.edit()
                        .putBoolean("milestone_" + MILESTONES[i], true)
                        .apply();
                int reward = MILESTONE_REWARDS[i];
                new CoinManager(context).earn(reward, "streak_milestone");
                GameAnalytics.get().trackStreakMilestone(MILESTONES[i], reward);
            }
        }
    }

    /**
     * Records a loss. Resets the streak to 0.
     */
    public void recordLoss() {
        prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply();
    }

    /**
     * Gets the current win streak.
     */
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /**
     * Gets the best (longest) win streak ever.
     */
    public int getBestStreak() {
        return prefs.getInt(KEY_BEST_STREAK, 0);
    }

    /**
     * Restores the streak to a specific value (used by streak-save rewarded ad).
     */
    public void restoreStreak(int value) {
        prefs.edit().putInt(KEY_CURRENT_STREAK, value).apply();
    }
}
