package games.mrlaki5.backgammon.Economy;

/**
 * All coin economy constants. Change values here to rebalance.
 */
public final class CoinConfig {
    private CoinConfig() {}

    // === Earning ===
    public static final int WIN_BASE = 10;
    public static final int WIN_BONUS_MEDIUM = 5;
    public static final int WIN_BONUS_HARD = 10;
    public static final int WIN_BONUS_ROYAL = 20;

    // Daily login bonus (day 1 → day 7+)
    public static final int[] DAILY_LOGIN_BONUS = {5, 10, 15, 20, 30, 40, 50};
    public static final int[] DAILY_LOGIN_REWARDS = DAILY_LOGIN_BONUS;

    // Achievement rewards
    public static final int ACHIEVEMENT_REWARD_COMMON    = 30;
    public static final int ACHIEVEMENT_REWARD_RARE      = 75;
    public static final int ACHIEVEMENT_REWARD_EPIC      = 150;
    public static final int ACHIEVEMENT_REWARD_LEGENDARY = 300;
    public static final int ACHIEVEMENT_UNLOCK = ACHIEVEMENT_REWARD_COMMON;
    public static final int ACHIEVEMENT_UNLOCK_MAJOR = ACHIEVEMENT_REWARD_EPIC;

    // Daily challenge completion
    public static final int DAILY_CHALLENGE_REWARD = 30;
    public static final int DAILY_CHALLENGE_COMPLETE = DAILY_CHALLENGE_REWARD;

    // Win streak milestones
    public static final int STREAK_MILESTONE_EVERY  = 5;   // every 5 wins
    public static final int STREAK_MILESTONE_REWARD = 75;  // coins per milestone

    public static final int FIRST_GAME_OF_DAY = 5;
    public static final int REWARDED_AD_WATCH = 15;
    public static final int GAME_REVIEW_REWARD = 20;
    public static final int DAILY_CHALLENGE_BONUS_CHEST = 25;  // on top of base
    public static final int DOUBLE_REWARD_MULTIPLIER = 2;
    public static final int WEEKLY_CHALLENGE_1_REWARD = 100;
    public static final int WEEKLY_CHALLENGE_2_REWARD = 75;
    public static final int WEEKLY_CHALLENGE_3_REWARD = 75;

    // === Spending ===
    public static final int HINT_COST         = 25;
    public static final int UNDO_COST_1       = 40;
    public static final int UNDO_COST_2       = 60;
    public static final int UNDO_COST_3       = 90;
    public static final int UNDO_COST         = UNDO_COST_1;

    // Shop item prices — must match CoinShopActivity catalog
    // Avatar Frames
    public static final int FRAME_BRONZE_PRICE  = 150;
    public static final int FRAME_SILVER_PRICE  = 400;
    public static final int FRAME_CARPET_PRICE  = 550;
    public static final int FRAME_GOLD_PRICE    = 1000;
    public static final int FRAME_PEACOCK_PRICE = 1500;
    public static final int FRAME_DIAMOND_PRICE = 3000;
    public static final int FRAME_SULTAN_PRICE  = 5000;
    // Dice Skins
    public static final int DICE_WALNUT_PRICE   = 200;
    public static final int DICE_RUBY_PRICE     = 500;
    public static final int DICE_MARBLE_PRICE   = 700;
    public static final int DICE_CRYSTAL_PRICE  = 1400;
    public static final int DICE_DRAGON_PRICE   = 3500;
    // Titles
    public static final int TITLE_SHARP_PRICE      = 100;
    public static final int TITLE_TACTICIAN_PRICE  = 350;
    public static final int TITLE_MASTER_PRICE     = 600;
    public static final int TITLE_KING_PRICE       = 1200;
    public static final int TITLE_SULTAN_PRICE     = 2500;
    // Rentals (24h)
    public static final int RENTAL_CYBERPUNK_PRICE = 80;
    public static final int RENTAL_LUXURY_PRICE    = 120;
    public static final int RENTAL_DIAMOND_PRICE   = 50;
    public static final int RENTAL_SULTAN_PRICE    = 80;
    public static final int RENTAL_DRAGON_PRICE    = 60;

    // Theme unlock prices
    public static final int THEME_LUXURY_PRICE = 600;
    public static final int THEME_GALAXY_PRICE = 700;
    public static final int THEME_EGYPT_PRICE = 650;
    public static final int THEME_NEON_PRICE = 500;

    // Legacy spending aliases
    public static final int THEME_RENTAL_COST = RENTAL_DIAMOND_PRICE;
    public static final int AVATAR_FRAME_COST = FRAME_BRONZE_PRICE;
    public static final int DICE_SKIN_COST = DICE_WALNUT_PRICE;
    public static final int PROFILE_TITLE_COST = TITLE_SHARP_PRICE;
}
