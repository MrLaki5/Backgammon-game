package games.mrlaki5.backgammon.Retention;

import android.content.Context;
import android.content.SharedPreferences;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.Util.DateUtil;

/**
 * Manages 10 offline achievements.
 * Stored in SharedPreferences — no backend needed.
 *
 * Achievements:
 * 1. first_win       — Win your first game
 * 2. games_10        — Play 10 games
 * 3. games_50        — Play 50 games
 * 4. games_100       — Play 100 games
 * 5. streak_5        — Win 5 games in a row
 * 6. beat_hard       — Beat Hard AI
 * 7. beat_royal      — Beat Royal AI
 * 8. no_quit_10      — Complete 10 games without quitting
 * 9. daily_win       — Win a game on first try of the day
 * 10. tutorial_done  — Complete the tutorial
 */
public class AchievementManager {

    private static final String PREFS_NAME = "achievements_prefs";
    private static final String KEY_TOTAL_GAMES = "total_games_played";
    private static final String KEY_TOTAL_WINS = "total_wins";
    private static final String KEY_GAMES_WITHOUT_QUIT = "games_without_quit";
    private static final String KEY_GAMES_TODAY = "games_today";
    private static final String KEY_LAST_PLAY_DAY = "last_play_day";

    public static final String[] ACHIEVEMENT_IDS = {
            "first_win",
            "games_10",
            "games_50",
            "games_100",
            "streak_5",
            "beat_hard",
            "beat_royal",
            "no_quit_10",
            "daily_win",
            "tutorial_done"
    };

    private final SharedPreferences prefs;
    private CoinManager coinManager;

    public AchievementManager(Context context) {
        this(context, new CoinManager(context));
    }

    public AchievementManager(Context context, CoinManager coinManager) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.coinManager = coinManager;
    }

    public void setCoinManager(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    /**
     * Called when a game is completed (not abandoned).
     *
     * @param won        true if player won
     * @param difficulty AI difficulty (0=easy, 1=medium, 2=hard, 3=royal)
     * @param winStreak  current win streak
     */
    public void onGameCompleted(boolean won, int difficulty, int winStreak) {
        SharedPreferences.Editor editor = prefs.edit();

        // Increment counters
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        editor.putInt(KEY_TOTAL_GAMES, totalGames);

        int gamesNoQuit = prefs.getInt(KEY_GAMES_WITHOUT_QUIT, 0) + 1;
        editor.putInt(KEY_GAMES_WITHOUT_QUIT, gamesNoQuit);

        if (won) {
            int totalWins = prefs.getInt(KEY_TOTAL_WINS, 0) + 1;
            editor.putInt(KEY_TOTAL_WINS, totalWins);
        }

        editor.apply();

        // Check achievements
        if (won) {
            unlock("first_win");
        }
        if (totalGames >= 10) unlock("games_10");
        if (totalGames >= 50) unlock("games_50");
        if (totalGames >= 100) unlock("games_100");
        if (winStreak >= 5) unlock("streak_5");
        if (won && difficulty >= 2) unlock("beat_hard");
        if (won && difficulty >= 3) unlock("beat_royal");
        if (gamesNoQuit >= 10) unlock("no_quit_10");

        // Daily win check
        int today = DateUtil.getDayOfYear();
        int lastDay = prefs.getInt(KEY_LAST_PLAY_DAY, -1);
        if (today != lastDay) {
            prefs.edit()
                    .putInt(KEY_LAST_PLAY_DAY, today)
                    .putInt(KEY_GAMES_TODAY, 0)
                    .apply();
        }
        int gamesToday = prefs.getInt(KEY_GAMES_TODAY, 0) + 1;
        prefs.edit().putInt(KEY_GAMES_TODAY, gamesToday).apply();
        if (won && gamesToday == 1) {
            unlock("daily_win");
        }
    }

    /**
     * Called when player quits a game (resets no-quit counter).
     */
    public void onGameAbandoned() {
        prefs.edit().putInt(KEY_GAMES_WITHOUT_QUIT, 0).apply();
    }

    /**
     * Called when tutorial is completed.
     */
    public void onTutorialCompleted() {
        unlock("tutorial_done");
    }

    /**
     * Returns true if the given achievement is unlocked.
     */
    public boolean isUnlocked(String achievementId) {
        return prefs.getBoolean("unlocked_" + achievementId, false);
    }

    /**
     * Returns the count of unlocked achievements.
     */
    public int getUnlockedCount() {
        int count = 0;
        for (String id : ACHIEVEMENT_IDS) {
            if (isUnlocked(id)) count++;
        }
        return count;
    }

    /**
     * Returns total games played (for display).
     */
    public int getTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }

    /**
     * Returns total wins (for display).
     */
    public int getTotalWins() {
        return prefs.getInt(KEY_TOTAL_WINS, 0);
    }

    private int getAchievementReward(String achievementId) {
        if ("games_100".equals(achievementId)) {
            return CoinConfig.ACHIEVEMENT_REWARD_LEGENDARY;
        } else if ("beat_royal".equals(achievementId) || "streak_5".equals(achievementId)) {
            return CoinConfig.ACHIEVEMENT_REWARD_EPIC;
        } else if ("beat_hard".equals(achievementId) || "games_50".equals(achievementId) || "no_quit_10".equals(achievementId)) {
            return CoinConfig.ACHIEVEMENT_REWARD_RARE;
        } else {
            return CoinConfig.ACHIEVEMENT_REWARD_COMMON;
        }
    }

    private void unlock(String achievementId) {
        if (!isUnlocked(achievementId)) {
            prefs.edit().putBoolean("unlocked_" + achievementId, true).apply();
            GameAnalytics.get().trackAchievementUnlocked(achievementId);
            if (coinManager != null) {
                int reward = getAchievementReward(achievementId);
                coinManager.earn(reward, "achievement_" + achievementId);
            }
        }
    }
}
