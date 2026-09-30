package games.mrlaki5.backgammon.Retention;

import com.royalbackgammon.core.variant.Variant;

import android.content.Context;
import android.content.SharedPreferences;

import games.mrlaki5.backgammon.Analytics.GameAnalytics;
import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.Util.DateUtil;

/**
 * Simple offline daily challenge system.
 * One challenge per day, rotates through a fixed list.
 * No backend needed — uses local date + SharedPreferences.
 *
 * Challenges are intentionally simple to avoid frustration:
 * - Win 1 game
 * - Complete 2 games
 * - Beat Medium AI
 * - Beat Hard AI
 * - Win without getting hit
 * - Complete 3 games
 * - Win with a win streak of 2+
 *
 * Retention purpose: gives users a reason to open the app daily.
 */
public class DailyChallenge {

    private static final String PREFS_NAME = "daily_challenge_prefs";
    private static final String KEY_LAST_CHALLENGE_DAY = "last_challenge_day";
    private static final String KEY_CHALLENGE_INDEX = "challenge_index";
    private static final String KEY_CHALLENGE_PROGRESS = "challenge_progress";
    private static final String KEY_CHALLENGE_COMPLETED = "challenge_completed";
    private static final String KEY_TOTAL_COMPLETED = "total_challenges_completed";

    /** Challenge definitions: id, type, target value */
    public static final Challenge[] CHALLENGES = {
            new Challenge("win_1", ChallengeType.WIN_GAMES, 1),
            new Challenge("complete_2", ChallengeType.COMPLETE_GAMES, 2),
            new Challenge("beat_medium", ChallengeType.BEAT_DIFFICULTY, 1), // 1 = medium
            new Challenge("beat_hard", ChallengeType.BEAT_DIFFICULTY, 2),   // 2 = hard
            new Challenge("win_no_hit", ChallengeType.WIN_WITHOUT_HIT, 1),
            new Challenge("complete_3", ChallengeType.COMPLETE_GAMES, 3),
            new Challenge("streak_2", ChallengeType.WIN_STREAK, 2),
            new Challenge("win_variant", ChallengeType.WIN_IN_VARIANT, 1),
    };

    /** Variants the "win in today's variant" challenge rotates through. */
    public static final Variant[] DAILY_VARIANTS = {
            Variant.STANDARD, Variant.TAVLA, Variant.PORTES, Variant.PLAKOTO,
            Variant.FEVGA, Variant.NARDY, Variant.ACEY_DEUCEY
    };

    private final SharedPreferences prefs;
    private final Context context;
    private CoinManager coinManager;

    public DailyChallenge(Context context) {
        this(context, new CoinManager(context));
    }

    public DailyChallenge(Context context, CoinManager coinManager) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.coinManager = coinManager;
        refreshIfNewDay();
    }

    public void setCoinManager(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    /**
     * Checks if it's a new day and rotates the challenge if needed.
     */
    private void refreshIfNewDay() {
        int today = DateUtil.getDayOfYear();
        int lastDay = prefs.getInt(KEY_LAST_CHALLENGE_DAY, -1);
        if (today != lastDay) {
            // New day — rotate to next challenge
            int currentIndex = prefs.getInt(KEY_CHALLENGE_INDEX, -1);
            int nextIndex = (currentIndex + 1) % CHALLENGES.length;
            prefs.edit()
                    .putInt(KEY_LAST_CHALLENGE_DAY, today)
                    .putInt(KEY_CHALLENGE_INDEX, nextIndex)
                    .putInt(KEY_CHALLENGE_PROGRESS, 0)
                    .putBoolean(KEY_CHALLENGE_COMPLETED, false)
                    .apply();
        }
    }

    /**
     * Returns the current day's challenge.
     */
    /** The variant today's "win in this variant" challenge asks for; stable for the whole day. */
    public static Variant variantOfDay(int dayOfYear) {
        int index = Math.floorMod(dayOfYear, DAILY_VARIANTS.length);
        return DAILY_VARIANTS[index];
    }

    public Variant getTodayVariant() {
        return variantOfDay(prefs.getInt(KEY_LAST_CHALLENGE_DAY, DateUtil.getDayOfYear()));
    }

    public Challenge getTodayChallenge() {
        int index = prefs.getInt(KEY_CHALLENGE_INDEX, 0);
        return CHALLENGES[index % CHALLENGES.length];
    }

    /**
     * Returns current progress toward today's challenge.
     */
    public int getProgress() {
        return prefs.getInt(KEY_CHALLENGE_PROGRESS, 0);
    }

    /**
     * Returns true if today's challenge is already completed.
     */
    public boolean isCompleted() {
        return prefs.getBoolean(KEY_CHALLENGE_COMPLETED, false);
    }

    /**
     * Returns total number of challenges ever completed.
     */
    public int getTotalCompleted() {
        return prefs.getInt(KEY_TOTAL_COMPLETED, 0);
    }

    /**
     * Called when a game is completed. Updates progress based on challenge type.
     *
     * @param won          true if the player won
     * @param difficulty   AI difficulty index (0=easy, 1=medium, 2=hard, 3=royal)
     * @param wasHit       true if the player's checker was hit during the game
     * @param currentStreak current win streak value
     */
    public void onGameCompleted(boolean won, int difficulty, boolean wasHit, int currentStreak) {
        onGameCompleted(won, difficulty, wasHit, currentStreak, Variant.STANDARD);
    }

    /** @param variant the variant that was just played, for the variant-of-the-day challenge */
    public void onGameCompleted(boolean won, int difficulty, boolean wasHit, int currentStreak,
                                Variant variant) {
        if (isCompleted()) return;

        Challenge challenge = getTodayChallenge();
        boolean shouldIncrement = false;

        switch (challenge.type) {
            case WIN_GAMES:
                if (won) shouldIncrement = true;
                break;
            case COMPLETE_GAMES:
                shouldIncrement = true;
                break;
            case BEAT_DIFFICULTY:
                if (won && difficulty >= challenge.targetValue) shouldIncrement = true;
                break;
            case WIN_WITHOUT_HIT:
                if (won && !wasHit) shouldIncrement = true;
                break;
            case WIN_STREAK:
                if (currentStreak >= challenge.targetValue) shouldIncrement = true;
                break;
            case WIN_IN_VARIANT:
                if (won && variant == getTodayVariant()) shouldIncrement = true;
                break;
        }

        if (shouldIncrement) {
            int progress = getProgress() + 1;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_CHALLENGE_PROGRESS, progress);

            if (progress >= challenge.targetValue ||
                    (challenge.type == ChallengeType.BEAT_DIFFICULTY && progress >= 1) ||
                    (challenge.type == ChallengeType.WIN_WITHOUT_HIT && progress >= 1) ||
                    (challenge.type == ChallengeType.WIN_STREAK && progress >= 1) ||
                    (challenge.type == ChallengeType.WIN_IN_VARIANT && progress >= 1)) {
                editor.putBoolean(KEY_CHALLENGE_COMPLETED, true);
                editor.putInt(KEY_TOTAL_COMPLETED, getTotalCompleted() + 1);
                GameAnalytics.get().trackMissionCompleted(challenge.id);
                if (coinManager != null) {
                    coinManager.earn(CoinConfig.DAILY_CHALLENGE_REWARD, "daily_challenge_complete");
                }
                if (context != null) {
                    WeeklyChallenge weeklyChallenge = new WeeklyChallenge(context);
                    weeklyChallenge.onDailyChallengeCompleted();
                }
            }
            editor.apply();
        }
    }

    // ==================== Data classes ====================

    public enum ChallengeType {
        WIN_GAMES,
        COMPLETE_GAMES,
        BEAT_DIFFICULTY,
        WIN_WITHOUT_HIT,
        WIN_STREAK,
        WIN_IN_VARIANT
    }

    public static class Challenge {
        public final String id;
        public final ChallengeType type;
        public final int targetValue;

        public Challenge(String id, ChallengeType type, int targetValue) {
            this.id = id;
            this.type = type;
            this.targetValue = targetValue;
        }
    }
}
