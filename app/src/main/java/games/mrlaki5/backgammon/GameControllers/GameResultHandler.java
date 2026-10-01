package games.mrlaki5.backgammon.GameControllers;

import android.content.Context;

import com.royalbackgammon.core.variant.Variant;
import android.content.SharedPreferences;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;
import games.mrlaki5.backgammon.Retention.AchievementManager;
import games.mrlaki5.backgammon.Retention.DailyChallenge;
import games.mrlaki5.backgammon.Retention.WeeklyChallenge;
import games.mrlaki5.backgammon.Util.DateUtil;
import games.mrlaki5.backgammon.WinStreakTracker;

public class GameResultHandler {

    public static class ProcessedResult {
        public final int currentStreak;
        public final int previousStreak;
        public final int coinsEarned;
        public final int eloDelta;
        public final String coinBreakdown;

        public ProcessedResult(int currentStreak, int previousStreak, int coinsEarned,
                               int eloDelta, String coinBreakdown) {
            this.currentStreak = currentStreak;
            this.previousStreak = previousStreak;
            this.coinsEarned = coinsEarned;
            this.eloDelta = eloDelta;
            this.coinBreakdown = coinBreakdown;
        }
    }

    private final Context context;
    private final PlayerProfileManager profileManager;
    private final CoinManager coinManager;

    public GameResultHandler(Context context, PlayerProfileManager profileManager) {
        this(context, profileManager, new CoinManager(context));
    }

    public GameResultHandler(Context context, PlayerProfileManager profileManager, CoinManager coinManager) {
        this.context = context;
        this.profileManager = profileManager;
        this.coinManager = coinManager;
    }

    /**
     * Process game outcome: ELO update (per variant), win streak, coins scaled by points won,
     * daily/weekly challenges, achievements.
     */
    public ProcessedResult processResult(boolean won, String gameMode, boolean passAndPlayMode,
                                         boolean tutorialMode, Variant variant, int points,
                                         int difficulty) {
        int currentStreak = 0;
        int previousStreak = 0;
        int coinsEarned = 0;
        int eloDelta = 0;
        StringBuilder coinBreakdown = new StringBuilder();

        if (!passAndPlayMode && !tutorialMode) {
            WinStreakTracker streakTracker = new WinStreakTracker(context);
            previousStreak = streakTracker.getCurrentStreak();
            int botElo = PlayerProfileManager.getBotElo(difficulty);

            eloDelta = profileManager.recordGameResult(won, botElo, gameMode, variant);

            if (won) {
                profileManager.incrementWinCount();
                currentStreak = streakTracker.recordWin();
                coinsEarned = CoinConfig.WIN_BASE * Math.max(1, points);
                coinBreakdown.append(context.getString(R.string.coins_base_win, coinsEarned));

                int diffBonus = 0;
                if (difficulty == 1) diffBonus = CoinConfig.WIN_BONUS_MEDIUM;
                else if (difficulty == 2) diffBonus = CoinConfig.WIN_BONUS_HARD;
                else if (difficulty == 3) diffBonus = CoinConfig.WIN_BONUS_ROYAL;

                if (diffBonus > 0) {
                    coinsEarned += diffBonus;
                    coinBreakdown.append(" • ").append(context.getString(R.string.coins_diff_bonus, diffBonus));
                }

                if (coinManager != null) {
                    coinManager.earn(coinsEarned, "game_win");
                }

                // First game of the day
                SharedPreferences flowPrefs = context.getSharedPreferences("game_flow_prefs", Context.MODE_PRIVATE);
                int today = DateUtil.getDayOfYear();
                if (today != flowPrefs.getInt("last_game_day", -1)) {
                    if (coinManager != null) {
                        coinManager.earn(CoinConfig.FIRST_GAME_OF_DAY, "first_game_of_day");
                    }
                    coinBreakdown.append(" • ").append(context.getString(R.string.coins_first_game, CoinConfig.FIRST_GAME_OF_DAY));
                    flowPrefs.edit().putInt("last_game_day", today).apply();
                }

                // Streak milestone check
                if (currentStreak > 0 && currentStreak % CoinConfig.STREAK_MILESTONE_EVERY == 0) {
                    if (coinManager != null) {
                        coinManager.earn(CoinConfig.STREAK_MILESTONE_REWARD, "streak_milestone");
                    }
                    coinBreakdown.append(" • ").append(context.getString(R.string.coins_streak_bonus, CoinConfig.STREAK_MILESTONE_REWARD));
                }
            } else {
                streakTracker.recordLoss();
            }

            // Update achievements
            AchievementManager achievements = new AchievementManager(context, coinManager);
            achievements.onGameCompleted(won, difficulty, currentStreak);

            // Update daily challenge
            DailyChallenge dailyChallenge = new DailyChallenge(context, coinManager);
            dailyChallenge.onGameCompleted(won, difficulty, false, currentStreak, variant);

            // Update weekly challenge
            WeeklyChallenge weeklyChallenge = new WeeklyChallenge(context);
            weeklyChallenge.onGameCompleted(won);
        }

        return new ProcessedResult(currentStreak, previousStreak, coinsEarned, eloDelta, coinBreakdown.toString());
    }
}
