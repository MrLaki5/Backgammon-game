package games.mrlaki5.backgammon.Analytics;

/**
 * Predefined analytics event names and parameter keys.
 * All events that must be tracked for product analysis, retention, and monetization.
 *
 * Naming convention: snake_case, max 40 chars (Firebase limit).
 */
public final class AnalyticsEvent {

    private AnalyticsEvent() {}

    // ==================== APP LIFECYCLE ====================
    public static final String APP_OPEN = "app_open";
    public static final String FIRST_LAUNCH = "first_launch";
    public static final String SESSION_START = "session_start";
    public static final String SESSION_END = "session_end";

    // ==================== TUTORIAL ====================
    public static final String TUTORIAL_STARTED = "tutorial_started";
    public static final String TUTORIAL_STEP = "tutorial_step";
    public static final String TUTORIAL_COMPLETED = "tutorial_completed";
    public static final String TUTORIAL_SKIPPED = "tutorial_skipped";

    // ==================== GAME ====================
    public static final String GAME_STARTED = "game_started";
    public static final String GAME_COMPLETED = "game_completed";
    public static final String GAME_WON = "game_won";
    public static final String GAME_LOST = "game_lost";
    public static final String GAME_ABANDONED = "game_abandoned";
    public static final String REMATCH_CLICKED = "rematch_clicked";
    public static final String REMATCH_STARTED = "rematch_started";
    public static final String REMATCH_COMPLETED = "rematch_completed";
    public static final String GAME_RESTARTED = "game_restarted";

    // ==================== ADS ====================
    public static final String AD_REQUESTED = "ad_requested";
    public static final String AD_LOADED = "ad_loaded";
    public static final String AD_FAILED = "ad_failed";
    public static final String AD_SHOWN = "ad_shown";
    public static final String AD_CLICKED = "ad_clicked";
    public static final String REWARDED_AD_STARTED = "rewarded_ad_started";
    public static final String REWARDED_AD_COMPLETED = "rewarded_ad_completed";
    public static final String REWARDED_AD_FAILED = "rewarded_ad_failed";

    // ==================== UX ====================
    public static final String MENU_PLAY_CLICKED = "menu_play_clicked";
    public static final String TUTORIAL_CLICKED = "tutorial_clicked";
    public static final String SETTINGS_OPENED = "settings_opened";
    public static final String SCORES_OPENED = "scores_opened";
    public static final String THEME_SELECTED = "theme_selected";
    public static final String DIFFICULTY_SELECTED = "difficulty_selected";
    public static final String LANGUAGE_CHANGED = "language_changed";

    // ==================== MONETIZATION ====================
    public static final String PURCHASE_STARTED = "purchase_started";
    public static final String PURCHASE_SUCCESS = "purchase_success";
    public static final String PURCHASE_FAILED = "purchase_failed";
    public static final String REMOVE_ADS_CLICKED = "remove_ads_clicked";
    public static final String THEME_PURCHASE_CLICKED = "theme_purchase_clicked";

    // Legacy aliases (kept for backward compat with existing code)
    public static final String IAP_PURCHASE_ATTEMPT = PURCHASE_STARTED;
    public static final String IAP_PURCHASE_SUCCESS = PURCHASE_SUCCESS;

    // ==================== RETENTION ====================
    public static final String DAILY_REWARD_OPENED = "daily_reward_opened";
    public static final String DAILY_REWARD_CLAIMED = "daily_reward_claimed";
    public static final String MISSION_STARTED = "mission_started";
    public static final String MISSION_COMPLETED = "mission_completed";
    public static final String ACHIEVEMENT_UNLOCKED = "achievement_unlocked";

    // ==================== MATCHMAKING (future) ====================
    public static final String MATCHMAKING_STARTED = "matchmaking_started";
    public static final String MATCHMAKING_MATCHED = "matchmaking_matched";
    public static final String MATCHMAKING_ABANDONED = "matchmaking_abandoned";

    // ==================== ECONOMY & REWARDS ====================
    public static final String COIN_EARNED = "coin_earned";
    public static final String COIN_SPENT = "coin_spent";
    public static final String DAILY_LOGIN_CLAIMED = "daily_login_claimed";
    public static final String WEEKLY_CHALLENGE_COMPLETED = "weekly_challenge_completed";
    public static final String REWARDED_AD_WATCHED = "rewarded_ad_watched";
    public static final String STREAK_SAVED = "streak_saved";
    public static final String DOUBLE_REWARD_CLAIMED = "double_reward_claimed";

    // Legacy alias
    public static final String AD_REWARD_EARNED = REWARDED_AD_COMPLETED;

    // ==================== PARAMETER KEYS ====================
    public static final String PARAM_MODE = "mode";                 // vs_bot, pass_and_play, online
    public static final String PARAM_DIFFICULTY = "difficulty";      // easy, medium, hard, royal
    public static final String PARAM_THEME = "theme";               // royal, pop_art, cyberpunk, luxury
    public static final String PARAM_WINNER = "winner";             // player number or "player"/"bot"
    public static final String PARAM_DURATION_SEC = "duration_sec";
    public static final String PARAM_GAMES_PLAYED = "games_played";
    public static final String PARAM_ELO_BEFORE = "elo_before";
    public static final String PARAM_ELO_AFTER = "elo_after";

    // Economy parameters
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_SOURCE = "source";
    public static final String PARAM_ITEM = "item";
    public static final String PARAM_NEW_BALANCE = "new_balance";
    public static final String PARAM_DAY = "day";
    public static final String PARAM_CHALLENGE_ID = "challenge_id";

    // Ad parameters
    public static final String PARAM_AD_TYPE = "ad_type";           // interstitial, rewarded
    public static final String PARAM_PLACEMENT = "placement";       // post_game, hint, daily_reward
    public static final String PARAM_GAME_NUMBER = "game_number";   // nth game in session
    public static final String PARAM_REWARD_TYPE = "reward_type";   // hint, extra_move, theme_trial
    public static final String PARAM_ERROR = "error";               // error message for failures

    // UX parameters
    public static final String PARAM_SELECTED_THEME = "selected_theme";
    public static final String PARAM_SELECTED_DIFFICULTY = "selected_difficulty";
    public static final String PARAM_LANGUAGE = "language";
    public static final String PARAM_STEP = "step";                 // tutorial step number

    // Monetization parameters
    public static final String PARAM_SKU = "sku";
    public static final String PARAM_STORE = "store";               // bazaar, myket, foss
    public static final String PARAM_PRICE = "price";

    // Retention parameters
    public static final String PARAM_DAY_STREAK = "day_streak";
    public static final String PARAM_WIN_STREAK = "win_streak";
    public static final String PARAM_REWARD_ID = "reward_id";
    public static final String PARAM_MISSION_ID = "mission_id";
    public static final String PARAM_ACHIEVEMENT_ID = "achievement_id";

    // Matchmaking parameters
    public static final String PARAM_WAIT_SEC = "wait_sec";
}
