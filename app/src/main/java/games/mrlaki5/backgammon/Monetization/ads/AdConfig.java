package games.mrlaki5.backgammon.Monetization.ads;

import games.mrlaki5.backgammon.BuildConfig;

/**
 * Centralized ad configuration.
 * All ad-related constants and zone IDs live here for easy management.
 *
 * To change ad behavior, modify these values — no hunting through game code needed.
 */
public final class AdConfig {

    private AdConfig() {}

    // ==================== Zone IDs ====================

    /**
     * Tapsell Plus Interstitial Zone ID.
     * Shown between games (post-game result screen).
     */
    public static final String ZONE_INTERSTITIAL = BuildConfig.TAPSELL_ZONE_INTERSTITIAL;

    /**
     * Tapsell Plus Rewarded Video Zone ID.
     * Shown when user requests a hint.
     */
    public static final String ZONE_REWARDED = BuildConfig.TAPSELL_ZONE_REWARDED;

    // ==================== Frequency Config ====================

    /**
     * Show interstitial ad every N completed games.
     * Example: 3 means after every 3rd game, an interstitial may show.
     */
    public static final int INTERSTITIAL_EVERY_N_GAMES = 3;

    // ==================== Policy Rules ====================

    /**
     * Never show ads during gameplay (mid-turn, during dice roll, during moves).
     */
    public static final boolean NEVER_MID_GAME = true;

    /**
     * Rewarded ads only on explicit user action (hint button, etc).
     */
    public static final boolean REWARDED_USER_INITIATED_ONLY = true;
}
