package games.mrlaki5.backgammon.Analytics;

import android.content.Context;
import android.os.Bundle;

/**
 * Singleton facade for analytics + crash reporting.
 * Provides type-safe convenience methods for all game events.
 *
 * Architecture:
 *   Game Code → GameAnalytics → AnalyticsProvider → SDK (Firebase/Metrix/etc.)
 *
 * Usage:
 *   GameAnalytics.init(activity, analyticsProvider, crashReporter);
 *   GameAnalytics.get().trackGameStarted("vs_bot", "medium", "royal");
 */
public class GameAnalytics {

    private static GameAnalytics instance;

    private AnalyticsProvider analytics;
    private CrashReporter crashReporter;

    private GameAnalytics() {}

    public static void init(Context context, AnalyticsProvider analytics, CrashReporter crashReporter) {
        instance = new GameAnalytics();
        instance.analytics = analytics;
        instance.crashReporter = crashReporter;
        analytics.initialize(context);
        crashReporter.initialize(context);
    }

    public static GameAnalytics get() {
        if (instance == null) {
            // Fallback to stubs if not initialized
            instance = new GameAnalytics();
            instance.analytics = new StubAnalyticsProvider();
            instance.crashReporter = new StubCrashReporter();
        }
        return instance;
    }

    public static GameAnalytics getInstance() {
        return get();
    }

    /** Returns the raw provider for direct logEvent calls if needed. */
    public AnalyticsProvider getProvider() {
        return analytics;
    }

    // ==================== APP LIFECYCLE ====================

    public void trackAppOpen() {
        analytics.logEvent(AnalyticsEvent.APP_OPEN, null);
        analytics.startSession();
    }

    public void trackFirstLaunch() {
        analytics.logEvent(AnalyticsEvent.FIRST_LAUNCH, null);
    }

    public void trackSessionStart() {
        analytics.logEvent(AnalyticsEvent.SESSION_START, null);
        analytics.startSession();
    }

    public void trackSessionEnd() {
        analytics.endSession();
        analytics.logEvent(AnalyticsEvent.SESSION_END, null);
    }

    // ==================== TUTORIAL ====================

    public void trackTutorialStarted() {
        analytics.logEvent(AnalyticsEvent.TUTORIAL_STARTED, null);
    }

    public void trackTutorialStep(int step) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_STEP, step);
        analytics.logEvent(AnalyticsEvent.TUTORIAL_STEP, params);
    }

    public void trackTutorialCompleted() {
        analytics.logEvent(AnalyticsEvent.TUTORIAL_COMPLETED, null);
    }

    public void trackTutorialSkipped(int atStep) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_STEP, atStep);
        analytics.logEvent(AnalyticsEvent.TUTORIAL_SKIPPED, params);
    }

    // ==================== GAME ====================

    public void trackGameStarted(String mode, String difficulty, String theme) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        if (difficulty != null) {
            params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        }
        if (theme != null) {
            params.putString(AnalyticsEvent.PARAM_THEME, theme);
        }
        analytics.logEvent(AnalyticsEvent.GAME_STARTED, params);
    }

    /** @deprecated Use trackGameStarted(mode, difficulty, theme) */
    @Deprecated
    public void trackGameStarted(String mode, String difficulty) {
        trackGameStarted(mode, difficulty, null);
    }

    public void trackGameCompleted(String mode, String winner, long durationSeconds,
                                   int gamesPlayed, int eloBefore, int eloAfter) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_WINNER, winner);
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSeconds);
        params.putInt(AnalyticsEvent.PARAM_GAMES_PLAYED, gamesPlayed);
        params.putInt(AnalyticsEvent.PARAM_ELO_BEFORE, eloBefore);
        params.putInt(AnalyticsEvent.PARAM_ELO_AFTER, eloAfter);
        analytics.logEvent(AnalyticsEvent.GAME_COMPLETED, params);
    }

    /** @deprecated Use full version with elo params */
    @Deprecated
    public void trackGameCompleted(String mode, int winner, long durationSeconds) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putInt(AnalyticsEvent.PARAM_WINNER, winner);
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSeconds);
        analytics.logEvent(AnalyticsEvent.GAME_COMPLETED, params);
    }

    public void trackGameWon(String mode, String difficulty, long durationSeconds) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSeconds);
        analytics.logEvent(AnalyticsEvent.GAME_WON, params);
    }

    public void trackGameLost(String mode, String difficulty, long durationSeconds) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSeconds);
        analytics.logEvent(AnalyticsEvent.GAME_LOST, params);
    }

    public void trackGameAbandoned(String mode, String difficulty, long durationSeconds) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        params.putLong(AnalyticsEvent.PARAM_DURATION_SEC, durationSeconds);
        analytics.logEvent(AnalyticsEvent.GAME_ABANDONED, params);
    }

    public void trackRematchClicked(String mode, String difficulty) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        analytics.logEvent(AnalyticsEvent.REMATCH_CLICKED, params);
    }

    public void trackRematchStarted(String mode, String difficulty) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        analytics.logEvent(AnalyticsEvent.REMATCH_STARTED, params);
    }

    public void trackRematchCompleted(String mode, String difficulty, String winner) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        params.putString(AnalyticsEvent.PARAM_WINNER, winner);
        analytics.logEvent(AnalyticsEvent.REMATCH_COMPLETED, params);
    }

    public void trackGameRestarted(String mode, String difficulty) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        params.putString(AnalyticsEvent.PARAM_DIFFICULTY, difficulty);
        analytics.logEvent(AnalyticsEvent.GAME_RESTARTED, params);
    }

    // ==================== ADS ====================

    public void trackAdRequested(String adType, String placement) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        analytics.logEvent(AnalyticsEvent.AD_REQUESTED, params);
    }

    public void trackAdLoaded(String adType, String placement) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        analytics.logEvent(AnalyticsEvent.AD_LOADED, params);
    }

    public void trackAdFailed(String adType, String placement, String error) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        params.putString(AnalyticsEvent.PARAM_ERROR, error);
        analytics.logEvent(AnalyticsEvent.AD_FAILED, params);
    }

    public void trackAdShown(String adType, String placement, int gameNumber) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        params.putInt(AnalyticsEvent.PARAM_GAME_NUMBER, gameNumber);
        analytics.logEvent(AnalyticsEvent.AD_SHOWN, params);
    }

    /** @deprecated Use trackAdShown(adType, placement, gameNumber) */
    @Deprecated
    public void trackAdShown(String adType) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        analytics.logEvent(AnalyticsEvent.AD_SHOWN, params);
    }

    public void trackAdClicked(String adType, String placement) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        analytics.logEvent(AnalyticsEvent.AD_CLICKED, params);
    }

    /** @deprecated Use trackAdClicked(adType, placement) */
    @Deprecated
    public void trackAdClicked(String adType) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_AD_TYPE, adType);
        analytics.logEvent(AnalyticsEvent.AD_CLICKED, params);
    }

    public void trackRewardedAdStarted(String placement, String rewardType) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        params.putString(AnalyticsEvent.PARAM_REWARD_TYPE, rewardType);
        analytics.logEvent(AnalyticsEvent.REWARDED_AD_STARTED, params);
    }

    public void trackRewardedAdCompleted(String placement, String rewardType) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        params.putString(AnalyticsEvent.PARAM_REWARD_TYPE, rewardType);
        analytics.logEvent(AnalyticsEvent.REWARDED_AD_COMPLETED, params);
    }

    public void trackRewardedAdFailed(String placement, String error) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        params.putString(AnalyticsEvent.PARAM_ERROR, error);
        analytics.logEvent(AnalyticsEvent.REWARDED_AD_FAILED, params);
    }

    /** @deprecated Use trackRewardedAdCompleted */
    @Deprecated
    public void trackAdRewardEarned() {
        analytics.logEvent(AnalyticsEvent.REWARDED_AD_COMPLETED, null);
    }

    // ==================== UX ====================

    public void trackMenuPlayClicked(String mode) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MODE, mode);
        analytics.logEvent(AnalyticsEvent.MENU_PLAY_CLICKED, params);
    }

    public void trackTutorialClicked() {
        analytics.logEvent(AnalyticsEvent.TUTORIAL_CLICKED, null);
    }

    public void trackSettingsOpened() {
        analytics.logEvent(AnalyticsEvent.SETTINGS_OPENED, null);
    }

    public void trackScoresOpened() {
        analytics.logEvent(AnalyticsEvent.SCORES_OPENED, null);
    }

    public void trackThemeSelected(String theme) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SELECTED_THEME, theme);
        analytics.logEvent(AnalyticsEvent.THEME_SELECTED, params);
    }

    public void trackDifficultySelected(String difficulty) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SELECTED_DIFFICULTY, difficulty);
        analytics.logEvent(AnalyticsEvent.DIFFICULTY_SELECTED, params);
    }

    public void trackLanguageChanged(String language) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_LANGUAGE, language);
        analytics.logEvent(AnalyticsEvent.LANGUAGE_CHANGED, params);
    }

    // ==================== MONETIZATION ====================

    public void trackPurchaseStarted(String sku, String store) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SKU, sku);
        params.putString(AnalyticsEvent.PARAM_STORE, store);
        analytics.logEvent(AnalyticsEvent.PURCHASE_STARTED, params);
    }

    public void trackPurchaseSuccess(String sku, String store) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SKU, sku);
        params.putString(AnalyticsEvent.PARAM_STORE, store);
        analytics.logEvent(AnalyticsEvent.PURCHASE_SUCCESS, params);
    }

    public void trackPurchaseFailed(String sku, String store, String error) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SKU, sku);
        params.putString(AnalyticsEvent.PARAM_STORE, store);
        params.putString(AnalyticsEvent.PARAM_ERROR, error);
        analytics.logEvent(AnalyticsEvent.PURCHASE_FAILED, params);
    }

    public void trackRemoveAdsClicked() {
        analytics.logEvent(AnalyticsEvent.REMOVE_ADS_CLICKED, null);
    }

    public void trackThemePurchaseClicked(String theme) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_SELECTED_THEME, theme);
        analytics.logEvent(AnalyticsEvent.THEME_PURCHASE_CLICKED, params);
    }

    /** @deprecated Use trackPurchaseStarted */
    @Deprecated
    public void trackPurchaseAttempt(String sku, String store) {
        trackPurchaseStarted(sku, store);
    }

    // ==================== RETENTION ====================

    public void trackDailyRewardOpened(int dayStreak) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_DAY_STREAK, dayStreak);
        analytics.logEvent(AnalyticsEvent.DAILY_REWARD_OPENED, params);
    }

    public void trackDailyRewardClaimed(int dayStreak, String rewardId) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_DAY_STREAK, dayStreak);
        params.putString(AnalyticsEvent.PARAM_REWARD_ID, rewardId);
        analytics.logEvent(AnalyticsEvent.DAILY_REWARD_CLAIMED, params);
    }

    public void trackMissionStarted(String missionId) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MISSION_ID, missionId);
        analytics.logEvent(AnalyticsEvent.MISSION_STARTED, params);
    }

    public void trackMissionCompleted(String missionId) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_MISSION_ID, missionId);
        analytics.logEvent(AnalyticsEvent.MISSION_COMPLETED, params);
    }

    public void trackAchievementUnlocked(String achievementId) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_ACHIEVEMENT_ID, achievementId);
        analytics.logEvent(AnalyticsEvent.ACHIEVEMENT_UNLOCKED, params);
    }

    public void trackStreakMilestone(int milestone, int reward) {
        Bundle params = new Bundle();
        params.putInt("milestone", milestone);
        params.putInt(AnalyticsEvent.PARAM_AMOUNT, reward);
        analytics.logEvent("streak_milestone", params);
    }

    // ==================== ECONOMY & REWARDS ====================

    public void trackCoinEarned(int amount, String source, int newBalance) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_AMOUNT, amount);
        params.putString(AnalyticsEvent.PARAM_SOURCE, source);
        params.putInt(AnalyticsEvent.PARAM_NEW_BALANCE, newBalance);
        analytics.logEvent(AnalyticsEvent.COIN_EARNED, params);
    }

    public void trackCoinSpent(int amount, String item, int newBalance) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_AMOUNT, amount);
        params.putString(AnalyticsEvent.PARAM_ITEM, item);
        params.putInt(AnalyticsEvent.PARAM_NEW_BALANCE, newBalance);
        analytics.logEvent(AnalyticsEvent.COIN_SPENT, params);
    }

    public void trackDailyLoginClaimed(int day, int amount) {
        Bundle params = new Bundle();
        params.putInt(AnalyticsEvent.PARAM_DAY, day);
        params.putInt(AnalyticsEvent.PARAM_AMOUNT, amount);
        analytics.logEvent(AnalyticsEvent.DAILY_LOGIN_CLAIMED, params);
    }

    public void trackWeeklyChallengeCompleted(String challengeId) {
        trackWeeklyChallengeCompleted(challengeId, 0);
    }

    public void trackWeeklyChallengeCompleted(String challengeId, int amount) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_CHALLENGE_ID, challengeId);
        if (amount > 0) {
            params.putInt(AnalyticsEvent.PARAM_AMOUNT, amount);
        }
        analytics.logEvent(AnalyticsEvent.WEEKLY_CHALLENGE_COMPLETED, params);
    }

    public void trackRewardedAdWatched(String placement) {
        Bundle params = new Bundle();
        params.putString(AnalyticsEvent.PARAM_PLACEMENT, placement);
        analytics.logEvent(AnalyticsEvent.REWARDED_AD_WATCHED, params);
    }

    public void trackStreakSaved() {
        analytics.logEvent(AnalyticsEvent.STREAK_SAVED, null);
    }

    public void trackDoubleRewardClaimed() {
        analytics.logEvent(AnalyticsEvent.DOUBLE_REWARD_CLAIMED, null);
    }

    // ==================== MATCHMAKING (future) ====================

    public void trackMatchmakingStarted() {
        analytics.logEvent(AnalyticsEvent.MATCHMAKING_STARTED, null);
    }

    public void trackMatchmakingMatched(long waitSeconds) {
        Bundle params = new Bundle();
        params.putLong(AnalyticsEvent.PARAM_WAIT_SEC, waitSeconds);
        analytics.logEvent(AnalyticsEvent.MATCHMAKING_MATCHED, params);
    }

    public void trackMatchmakingAbandoned(long waitSeconds) {
        Bundle params = new Bundle();
        params.putLong(AnalyticsEvent.PARAM_WAIT_SEC, waitSeconds);
        analytics.logEvent(AnalyticsEvent.MATCHMAKING_ABANDONED, params);
    }

    // ==================== CRASH REPORTING ====================

    public void reportError(Throwable t) {
        crashReporter.reportException(t);
    }

    public void reportError(Throwable t, String context) {
        crashReporter.reportException(t, context);
    }

    public void logBreadcrumb(String message) {
        crashReporter.log(message);
    }

    public void setUserId(String userId) {
        analytics.setUserId(userId);
        crashReporter.setUserId(userId);
    }

    public void setUserProperty(String name, String value) {
        analytics.setUserProperty(name, value);
    }
}
