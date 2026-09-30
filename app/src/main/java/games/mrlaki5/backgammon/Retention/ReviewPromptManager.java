package games.mrlaki5.backgammon.Retention;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import games.mrlaki5.backgammon.BuildConfig;

/**
 * Smart review prompt manager.
 * Shows a review request at optimal moments to maximize positive reviews.
 *
 * Rules:
 * - Only show after a WIN (happy user = good review)
 * - Only after at least 3 completed games (user knows the app)
 * - Only once per 7 days max
 * - If user dismisses 3 times, never show again
 * - Never during gameplay, ads, or after a loss
 *
 * Store-agnostic: opens the store page URL based on BuildConfig.STORE flavor.
 */
public class ReviewPromptManager {

    private static final String PREFS_NAME = "review_prompt_prefs";
    private static final String KEY_LAST_PROMPT_TIME = "last_prompt_time";
    private static final String KEY_DISMISS_COUNT = "dismiss_count";
    private static final String KEY_GAMES_COMPLETED = "games_for_review";
    private static final String KEY_RATED = "has_rated";

    private static final int MIN_GAMES_BEFORE_PROMPT = 3;
    private static final long MIN_DAYS_BETWEEN_PROMPTS = 7;
    private static final int MAX_DISMISSALS = 3;

    private final SharedPreferences prefs;

    public ReviewPromptManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Call after every completed game. Increments the internal counter.
     */
    public void onGameCompleted() {
        int count = prefs.getInt(KEY_GAMES_COMPLETED, 0) + 1;
        prefs.edit().putInt(KEY_GAMES_COMPLETED, count).apply();
    }

    /**
     * Returns true if conditions are met to show a review prompt.
     * Call this ONLY after a player WIN.
     */
    public boolean shouldShowPrompt() {
        // Already rated — never show again
        if (prefs.getBoolean(KEY_RATED, false)) return false;

        // Dismissed too many times — give up
        if (prefs.getInt(KEY_DISMISS_COUNT, 0) >= MAX_DISMISSALS) return false;

        // Not enough games played
        if (prefs.getInt(KEY_GAMES_COMPLETED, 0) < MIN_GAMES_BEFORE_PROMPT) return false;

        // Too soon since last prompt
        long lastPrompt = prefs.getLong(KEY_LAST_PROMPT_TIME, 0);
        long daysSinceLastPrompt = (System.currentTimeMillis() - lastPrompt) / (1000 * 60 * 60 * 24);
        if (lastPrompt > 0 && daysSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) return false;

        return true;
    }

    /**
     * Call when the review prompt is shown.
     */
    public void onPromptShown() {
        prefs.edit().putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * Call when user dismisses the prompt without rating.
     */
    public void onPromptDismissed() {
        int count = prefs.getInt(KEY_DISMISS_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_DISMISS_COUNT, count).apply();
    }

    /**
     * Call when user taps "Rate" / "Yes".
     */
    public void onUserAccepted() {
        prefs.edit().putBoolean(KEY_RATED, true).apply();
    }

    /**
     * Opens the store page for this app.
     * Works for Bazaar, Myket, and Google Play based on build flavor.
     */
    public static void openStorePage(Activity activity) {
        String store = BuildConfig.STORE;
        String packageName = activity.getPackageName();
        String url;

        switch (store) {
            case "bazaar":
                url = "https://cafebazaar.ir/app/" + packageName;
                break;
            case "myket":
                url = "https://myket.ir/app/" + packageName;
                break;
            default:
                url = "https://play.google.com/store/apps/details?id=" + packageName;
                break;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            // If no browser/store app available, silently fail
        }
    }
}
