package games.mrlaki5.backgammon.Monetization.ads;

/**
 * All rewarded ad placement points in the app.
 * Each has its own frequency cap and cooldown.
 */
public enum RewardedAdPlacement {
    HINT("hint", 3, 0, 0),                    // 3 per game, no daily cap, no cooldown
    FREE_COINS("free_coins", 0, 3, 15 * 60),  // 3 per day, 15 min cooldown (seconds)
    DOUBLE_REWARD("double_reward", 0, 0, 0),   // no cap (naturally limited to wins)
    SAVE_STREAK("save_streak", 0, 1, 0),       // 1 per day
    BONUS_CHEST("bonus_chest", 0, 1, 0),       // 1 per day (1 daily challenge)
    THEME_UNLOCK("theme_unlock", 0, 0, 0),     // no cap — permanent unlock
    THEME_DISCOUNT("theme_discount", 0, 0, 0), // no cap — 50% discount for 1h
    GAME_REVIEW("game_review", 0, 2, 0);       // 2 per day, offered after a post-game review

    public final String id;
    public final int maxPerGame;       // 0 = unlimited within game
    public final int maxPerDay;        // 0 = unlimited per day
    public final int cooldownSeconds;  // 0 = no cooldown

    RewardedAdPlacement(String id, int maxPerGame, int maxPerDay, int cooldownSeconds) {
        this.id = id;
        this.maxPerGame = maxPerGame;
        this.maxPerDay = maxPerDay;
        this.cooldownSeconds = cooldownSeconds;
    }
}
