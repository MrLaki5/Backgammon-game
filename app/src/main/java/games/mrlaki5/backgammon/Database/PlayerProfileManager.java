package games.mrlaki5.backgammon.Database;

import com.royalbackgammon.core.variant.Variant;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import games.mrlaki5.backgammon.Economy.CoinConfig;
import games.mrlaki5.backgammon.Economy.CoinManager;
import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;

/**
 * Manages player profile data, ELO ratings, match history, and cosmetic inventory.
 */
public class PlayerProfileManager {

    private static volatile PlayerProfileManager sInstance;

    private static final String PREFS_NAME = "player_profile_prefs";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_ELO = "player_elo";
    private static final String KEY_WINS = "player_wins";
    private static final String KEY_LOSSES = "player_losses";
    private static final String KEY_TOTAL_GAMES = "player_total_games";
    private static final String KEY_ACTIVE_FRAME = "active_frame";
    private static final String KEY_ACTIVE_DICE = "active_dice";
    private static final String KEY_ACTIVE_TITLE = "active_title";
    public static final String KEY_ACTIVE_THEME = "active_theme";
    private static final String KEY_PURCHASED_ITEMS = "purchased_items";

    public static final int BOT_ELO_EASY = 800;
    public static final int BOT_ELO_MEDIUM = 1100;
    public static final int BOT_ELO_HARD = 1400;
    public static final int BOT_ELO_ROYAL = 1700;

    private final Context context;
    private final SharedPreferences prefs;
    private final DbHelper dbHelper;

    public static synchronized PlayerProfileManager getInstance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new PlayerProfileManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public static PlayerProfileManager getInstance() {
        return sInstance;
    }

    public PlayerProfileManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dbHelper = new DbHelper(this.context);
        sInstance = this;
        initDefaults();
    }

    private void initDefaults() {
        if (!prefs.contains(KEY_ELO)) {
            prefs.edit()
                    .putInt(KEY_ELO, PlayerProfile.DEFAULT_ELO)
                    .putInt(KEY_WINS, 0)
                    .putInt(KEY_LOSSES, 0)
                    .putInt(KEY_TOTAL_GAMES, 0)
                    .putString(KEY_DISPLAY_NAME, context.getString(R.string.pass_and_play_player1_default))
                    .putString(KEY_ACTIVE_FRAME, "frame_default")
                    .putString(KEY_ACTIVE_DICE, "dice_default")
                    .putString(KEY_ACTIVE_TITLE, "title_beginner")
                    .putString(KEY_ACTIVE_THEME, "theme_royal")
                    .apply();

            addPurchasedItem("frame_default");
            addPurchasedItem("dice_default");
            addPurchasedItem("title_beginner");
            addPurchasedItem("theme_royal");
        }
    }

    public static int getBotElo(int difficulty) {
        switch (difficulty) {
            case 0: return BOT_ELO_EASY;
            case 1: return BOT_ELO_MEDIUM;
            case 2: return BOT_ELO_HARD;
            case 3: return BOT_ELO_ROYAL;
            default: return BOT_ELO_MEDIUM;
        }
    }

    public int getElo() {
        return prefs.getInt(KEY_ELO, PlayerProfile.DEFAULT_ELO);
    }

    /** Rating for [variant]; STANDARD keeps the legacy global rating key. */
    public int getElo(Variant variant) {
        return prefs.getInt(eloKey(variant), PlayerProfile.DEFAULT_ELO);
    }

    private static String eloKey(Variant variant) {
        return variant == Variant.STANDARD ? KEY_ELO : KEY_ELO + "_" + variant.name();
    }

    /** Games recorded for [variant]; STANDARD shares the global counter. */
    public int getGamesPlayed(Variant variant) {
        return prefs.getInt(gamesKey(variant), 0);
    }

    private static String gamesKey(Variant variant) {
        return variant == Variant.STANDARD ? KEY_TOTAL_GAMES : KEY_TOTAL_GAMES + "_" + variant.name();
    }

    public int getWins() {
        return prefs.getInt(KEY_WINS, 0);
    }

    /** Returns the total wins ever recorded for unlock requirement checks. */
    public int getTotalWins() {
        int totalWins = prefs.getInt("total_wins", 0);
        return Math.max(totalWins, getWins());
    }

    public int getLosses() {
        return prefs.getInt(KEY_LOSSES, 0);
    }

    public int getTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }

    public int getWinRate() {
        int total = getTotalGames();
        if (total == 0) return 0;
        return (int) Math.round((getWins() * 100.0) / total);
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, context.getString(R.string.pass_and_play_player1_default));
    }

    public void setDisplayName(String name) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    public String getActiveFrame() {
        return prefs.getString(KEY_ACTIVE_FRAME, "frame_default");
    }

    public void setActiveFrame(String frameId) {
        prefs.edit().putString(KEY_ACTIVE_FRAME, frameId).apply();
    }

    public String getActiveDice() {
        return prefs.getString(KEY_ACTIVE_DICE, "dice_default");
    }

    public void setActiveDice(String diceId) {
        prefs.edit().putString(KEY_ACTIVE_DICE, diceId).apply();
    }

    public String getActiveTitle() {
        return prefs.getString(KEY_ACTIVE_TITLE, "title_beginner");
    }

    public void setActiveTitle(String titleId) {
        prefs.edit().putString(KEY_ACTIVE_TITLE, titleId).apply();
    }

    public String getActiveTheme() {
        return prefs.getString(KEY_ACTIVE_THEME, "theme_royal");
    }

    public void setActiveTheme(String themeId) {
        prefs.edit().putString(KEY_ACTIVE_THEME, themeId).apply();
    }

    public Set<String> getPurchasedItems() {
        return prefs.getStringSet(KEY_PURCHASED_ITEMS, new HashSet<>());
    }

    public boolean isItemPurchased(String itemId) {
        Set<String> items = getPurchasedItems();
        return items != null && items.contains(itemId);
    }

    public void addPurchasedItem(String itemId) {
        Set<String> items = new HashSet<>(getPurchasedItems());
        items.add(itemId);
        prefs.edit().putStringSet(KEY_PURCHASED_ITEMS, items).apply();
    }

    public void removePurchasedItem(String itemId) {
        Set<String> items = new HashSet<>(getPurchasedItems());
        if (items.remove(itemId)) {
            prefs.edit().putStringSet(KEY_PURCHASED_ITEMS, items).apply();
        }
    }

    public static final String RENTAL_DIAMOND = "rental_diamond";
    public static final String RENTAL_SULTAN = "rental_sultan";
    public static final String RENTAL_DRAGON = "rental_dragon";

    private static final String[] KNOWN_RENTAL_IDS = {
            RENTAL_DIAMOND,
            RENTAL_SULTAN,
            RENTAL_DRAGON
    };

    public void purchaseRental(String itemId, int durationHours) {
        long expiry = System.currentTimeMillis() + durationHours * 3600_000L;
        prefs.edit().putLong("rental_expiry_" + itemId, expiry).apply();
    }

    public boolean isRentalActive(String itemId) {
        return System.currentTimeMillis() < prefs.getLong("rental_expiry_" + itemId, 0);
    }

    public long getRentalExpiry(String itemId) {
        return prefs.getLong("rental_expiry_" + itemId, 0);
    }

    public void checkAndExpireRentals() {
        for (String id : KNOWN_RENTAL_IDS) {
            if (!isRentalActive(id)) {
                removePurchasedItem(id);
                switch (id) {
                    case RENTAL_DIAMOND:
                        if ("frame_diamond".equals(getActiveFrame()) && !isItemPurchased("frame_diamond")) {
                            setActiveFrame("frame_default");
                        }
                        break;
                    case RENTAL_SULTAN:
                        if ("frame_sultan".equals(getActiveFrame()) && !isItemPurchased("frame_sultan")) {
                            setActiveFrame("frame_default");
                        }
                        break;
                    case RENTAL_DRAGON:
                        if ("dice_dragon".equals(getActiveDice()) && !isItemPurchased("dice_dragon")) {
                            setActiveDice("dice_default");
                        }
                        break;
                }
            }
        }
    }

    /**
     * Records match result, computes new ELO rating via EloCalculator, and saves to database.
     *
     * @param won true if player won
     * @param opponentElo opponent's ELO rating
     * @param gameMode mode of game ("vs_bot", "pass_and_play")
     * @param variant rule variant; each variant has its own rating
     * @return ELO delta (positive if gained, negative if lost)
     */
    public int recordGameResult(boolean won, int opponentElo, String gameMode, Variant variant) {
        int currentElo = getElo(variant);
        int variantGames = prefs.getInt(gamesKey(variant), 0);
        int delta = EloCalculator.ratingDelta(currentElo, opponentElo, won, variantGames);
        int newElo = currentElo + delta;
        if (newElo < 100) newElo = 100;
        int totalGames = getTotalGames();

        int wins = getWins() + (won ? 1 : 0);
        int losses = getLosses() + (won ? 0 : 1);
        int newTotalGames = totalGames + 1;

        SharedPreferences.Editor editor = prefs.edit()
                .putInt(eloKey(variant), newElo)
                .putInt(KEY_WINS, wins)
                .putInt(KEY_LOSSES, losses)
                .putInt(KEY_TOTAL_GAMES, newTotalGames);
        if (variant != Variant.STANDARD) {
            editor.putInt(gamesKey(variant), variantGames + 1);
        }
        if (won) {
            int totalWins = prefs.getInt("total_wins", 0);
            editor.putInt("total_wins", totalWins + 1);
        }
        editor.apply();

        // Update database profile
        try {
            PlayerProfile profile = dbHelper.getOrCreateProfile(getDisplayName());
            if (variant == Variant.STANDARD) {
                profile.setElo(newElo);
            }
            profile.setWins(wins);
            profile.setLosses(losses);
            profile.setTotalGames(newTotalGames);
            dbHelper.saveProfile(profile);
        } catch (Exception ignored) {}

        return delta;
    }

    // ── Theme Economy Methods ──────────────────────────────────────────────────

    public boolean hasPurchasedTheme(int id) {
        // Theme IDs 0, 1, 2, 4 are free by default
        if (id == GamePreferences.THEME_ROYAL ||
                id == GamePreferences.THEME_POP_ART ||
                id == GamePreferences.THEME_CYBERPUNK ||
                id == GamePreferences.THEME_WOODLAND) {
            return true;
        }
        return prefs.getBoolean("theme_purchased_" + id, false);
    }

    public boolean purchaseTheme(int id, int coinCost) {
        CoinManager coinManager = new CoinManager(context);
        if (!coinManager.spend(coinCost, "theme_" + id)) {
            return false;
        }
        prefs.edit().putBoolean("theme_purchased_" + id, true).apply();
        GamePreferences.unlockTheme(context, id);
        return true;
    }

    public void applyThemeDiscount(int id, float discountFraction, long durationMs) {
        int basePrice = getBaseThemePrice(id);
        int discountedPrice = Math.max(0, Math.round(basePrice * (1f - discountFraction)));
        long expiry = System.currentTimeMillis() + durationMs;
        prefs.edit()
                .putInt("theme_discount_" + id + "_price", discountedPrice)
                .putLong("theme_discount_" + id + "_expiry", expiry)
                .apply();
    }

    public int getThemePrice(int id) {
        long expiry = prefs.getLong("theme_discount_" + id + "_expiry", 0);
        if (System.currentTimeMillis() < expiry) {
            return prefs.getInt("theme_discount_" + id + "_price", getBaseThemePrice(id));
        }
        return getBaseThemePrice(id);
    }

    public static int getBaseThemePrice(int id) {
        switch (id) {
            case GamePreferences.THEME_LUXURY:
                return CoinConfig.THEME_LUXURY_PRICE;
            case GamePreferences.THEME_GALAXY:
                return CoinConfig.THEME_GALAXY_PRICE;
            case GamePreferences.THEME_ANCIENT_EGYPT:
                return CoinConfig.THEME_EGYPT_PRICE;
            case GamePreferences.THEME_NEON_RETRO:
                return CoinConfig.THEME_NEON_PRICE;
            default:
                return 0;
        }
    }

    // ── Consumable Charges & Bundles ──────────────────────────────────────────

    private static final String KEY_HINT_CHARGES = "hint_charges";
    private static final String KEY_UNDO_CHARGES = "undo_charges";

    public int getBalance() {
        return new CoinManager(context).getBalance();
    }

    public boolean deductCoins(int amount) {
        return new CoinManager(context).spend(amount, "shop_purchase");
    }

    public int getWinCount() {
        return getTotalWins();
    }

    public void incrementWinCount() {
        int wins = getWinCount() + 1;
        prefs.edit().putInt("total_wins", wins).apply();
    }

    public int getHintCharges() {
        return prefs.getInt(KEY_HINT_CHARGES, 0);
    }

    public void addHintCharges(int n) {
        prefs.edit().putInt(KEY_HINT_CHARGES, getHintCharges() + n).apply();
    }

    public boolean useHintCharge() {
        int c = getHintCharges();
        if (c <= 0) return false;
        prefs.edit().putInt(KEY_HINT_CHARGES, c - 1).apply();
        return true;
    }

    public int getUndoCharges() {
        return prefs.getInt(KEY_UNDO_CHARGES, 0);
    }

    public void addUndoCharges(int n) {
        prefs.edit().putInt(KEY_UNDO_CHARGES, getUndoCharges() + n).apply();
    }

    public boolean useUndoCharge() {
        int c = getUndoCharges();
        if (c <= 0) return false;
        prefs.edit().putInt(KEY_UNDO_CHARGES, c - 1).apply();
        return true;
    }

    /** Call this when a consumable pack is purchased from the shop. */
    public boolean purchaseConsumable(String itemId, int coinCost, int quantity) {
        if (getBalance() < coinCost) return false;
        deductCoins(coinCost);
        if (itemId.startsWith("hint_pack")) addHintCharges(quantity);
        else if (itemId.startsWith("undo_pack")) addUndoCharges(quantity);
        return true;
    }

    public boolean purchaseStarterBundle(int coinCost) {
        if (getBalance() < coinCost) return false;
        deductCoins(coinCost);
        // grant frame_silver and dice_elegant (or walnut/steel) as purchases
        addPurchasedItem("frame_silver");
        addPurchasedItem("dice_elegant");
        addPurchasedItem("dice_walnut");
        // 50 coins bonus
        new CoinManager(context).earn(50, "starter_bundle_bonus");
        addHintCharges(2); // bonus charges
        prefs.edit()
                .putBoolean("item_purchased_frame_silver", true)
                .putBoolean("item_purchased_dice_elegant", true)
                .putBoolean("item_purchased_starter_bundle", true)
                .apply();
        return true;
    }

    public boolean hasStarterBundle() {
        return prefs.getBoolean("item_purchased_starter_bundle", false);
    }
}
