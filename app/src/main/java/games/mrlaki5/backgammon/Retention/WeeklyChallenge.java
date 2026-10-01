package games.mrlaki5.backgammon.Retention;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;

/**
 * Weekly challenge system. Three concurrent challenges per week.
 * Resets every 7 days (epoch-week comparison).
 *
 * Challenges:
 * 1. "Player of the Week" — Complete all 7 daily challenges
 * 2. "Weekly Warrior" — Win 15 games
 * 3. "Endurance" — Complete 20 games without quitting
 */
public class WeeklyChallenge {

    private static final String PREFS_NAME = "weekly_challenge_prefs";
    private static final String KEY_LAST_WEEK = "last_week";
    private static final String KEY_DAILY_CHALLENGES_DONE = "daily_done";
    private static final String KEY_WINS = "weekly_wins";
    private static final String KEY_GAMES_NO_QUIT = "weekly_games_no_quit";
    private static final String KEY_CHALLENGE_1_CLAIMED = "c1_claimed";
    private static final String KEY_CHALLENGE_2_CLAIMED = "c2_claimed";
    private static final String KEY_CHALLENGE_3_CLAIMED = "c3_claimed";

    public static final int DAILY_TARGET = 7;
    public static final int WINS_TARGET = 15;
    public static final int ENDURANCE_TARGET = 20;
    public static final int REWARD_PLAYER_OF_WEEK = CoinConfig.WEEKLY_CHALLENGE_1_REWARD;
    public static final int REWARD_WARRIOR = CoinConfig.WEEKLY_CHALLENGE_2_REWARD;
    public static final int REWARD_ENDURANCE = CoinConfig.WEEKLY_CHALLENGE_3_REWARD;

    private final SharedPreferences prefs;
    private final CoinManager coinManager;

    public WeeklyChallenge(Context context) {
        this(context, new CoinManager(context));
    }

    public WeeklyChallenge(Context context, CoinManager coinManager) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.coinManager = coinManager;
        refreshIfNewWeek();
    }

    private long getLastWeekEpoch() {
        try {
            return prefs.getLong(KEY_LAST_WEEK, -1L);
        } catch (ClassCastException e) {
            return -1L;
        }
    }

    private void refreshIfNewWeek() {
        long epochWeek = System.currentTimeMillis() / (7L * 86400000L);
        long lastWeek = getLastWeekEpoch();
        if (epochWeek != lastWeek) {
            prefs.edit()
                    .putLong(KEY_LAST_WEEK, epochWeek)
                    .putInt(KEY_DAILY_CHALLENGES_DONE, 0)
                    .putInt(KEY_WINS, 0)
                    .putInt(KEY_GAMES_NO_QUIT, 0)
                    .putBoolean(KEY_CHALLENGE_1_CLAIMED, false)
                    .putBoolean(KEY_CHALLENGE_2_CLAIMED, false)
                    .putBoolean(KEY_CHALLENGE_3_CLAIMED, false)
                    .apply();
        }
    }

    public void onDailyChallengeCompleted() {
        int val = prefs.getInt(KEY_DAILY_CHALLENGES_DONE, 0) + 1;
        prefs.edit().putInt(KEY_DAILY_CHALLENGES_DONE, val).apply();
    }

    public void onGameCompleted(boolean won) {
        SharedPreferences.Editor editor = prefs.edit();
        if (won) {
            editor.putInt(KEY_WINS, prefs.getInt(KEY_WINS, 0) + 1);
        }
        editor.putInt(KEY_GAMES_NO_QUIT, prefs.getInt(KEY_GAMES_NO_QUIT, 0) + 1);
        editor.apply();
    }

    public void onGameAbandoned() {
        prefs.edit().putInt(KEY_GAMES_NO_QUIT, 0).apply();
    }

    // --- Progress getters ---
    public int getDailyChallengesDone() { return prefs.getInt(KEY_DAILY_CHALLENGES_DONE, 0); }
    public int getWeeklyWins() { return prefs.getInt(KEY_WINS, 0); }
    public int getGamesNoQuit() { return prefs.getInt(KEY_GAMES_NO_QUIT, 0); }

    public boolean isChallenge1Complete() { return getDailyChallengesDone() >= DAILY_TARGET; }
    public boolean isChallenge2Complete() { return getWeeklyWins() >= WINS_TARGET; }
    public boolean isChallenge3Complete() { return getGamesNoQuit() >= ENDURANCE_TARGET; }

    public boolean isChallenge1Claimed() { return prefs.getBoolean(KEY_CHALLENGE_1_CLAIMED, false); }
    public boolean isChallenge2Claimed() { return prefs.getBoolean(KEY_CHALLENGE_2_CLAIMED, false); }
    public boolean isChallenge3Claimed() { return prefs.getBoolean(KEY_CHALLENGE_3_CLAIMED, false); }

    public void claimChallenge1() {
        prefs.edit().putBoolean(KEY_CHALLENGE_1_CLAIMED, true).apply();
        if (coinManager != null) {
            coinManager.earn(CoinConfig.WEEKLY_CHALLENGE_1_REWARD, "weekly_challenge_1");
        }
        GameAnalytics.getInstance().trackWeeklyChallengeCompleted("weekly_challenge_1", CoinConfig.WEEKLY_CHALLENGE_1_REWARD);
    }

    public void claimChallenge2() {
        prefs.edit().putBoolean(KEY_CHALLENGE_2_CLAIMED, true).apply();
        if (coinManager != null) {
            coinManager.earn(CoinConfig.WEEKLY_CHALLENGE_2_REWARD, "weekly_challenge_2");
        }
        GameAnalytics.getInstance().trackWeeklyChallengeCompleted("weekly_challenge_2", CoinConfig.WEEKLY_CHALLENGE_2_REWARD);
    }

    public void claimChallenge3() {
        prefs.edit().putBoolean(KEY_CHALLENGE_3_CLAIMED, true).apply();
        if (coinManager != null) {
            coinManager.earn(CoinConfig.WEEKLY_CHALLENGE_3_REWARD, "weekly_challenge_3");
        }
        GameAnalytics.getInstance().trackWeeklyChallengeCompleted("weekly_challenge_3", CoinConfig.WEEKLY_CHALLENGE_3_REWARD);
    }
}
