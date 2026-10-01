package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.TimeZone;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;

/**
 * Which season it is, what is still for sale, and what has closed for good.
 *
 * The clock is the device's calendar. This game has no server to ask, so the
 * season is computed from a fixed epoch in six-week steps: every player reaches
 * season two on the same day without a single request, and it keeps working on
 * a phone that has never been online.
 *
 * That has one honest consequence, and it is worth stating rather than hiding:
 * a player who moves their device clock can reach a future season early or step
 * back into a closed one. Guarding against that needs a server, and a server is
 * exactly what this game has chosen not to have. What the clock cannot do is
 * take away something already bought — purchases are recorded on purchase, so a
 * retired item stays owned no matter what the calendar says afterwards.
 */
public final class SeasonManager {

    private static final String PREFS = "season_prefs";
    private static final String KEY_SEEN_SEASON = "seen_season";

    /** 21 March 2026 — Nowruz, which is where a Persian year starts. */
    private static final int EPOCH_YEAR = 2026;
    private static final int EPOCH_MONTH = Calendar.MARCH;
    private static final int EPOCH_DAY = 21;

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private final Context context;

    public SeasonManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Whole days since the epoch; negative before it, which clamps to season one. */
    private long daysSinceEpoch() {
        Calendar epoch = Calendar.getInstance(TimeZone.getDefault());
        epoch.set(EPOCH_YEAR, EPOCH_MONTH, EPOCH_DAY, 0, 0, 0);
        epoch.set(Calendar.MILLISECOND, 0);
        long elapsed = System.currentTimeMillis() - epoch.getTimeInMillis();
        return elapsed / DAY_MS;
    }

    /** The season on sale today. Seasons cycle, so the shop is never empty. */
    public Season current() {
        long days = Math.max(0, daysSinceEpoch());
        int index = (int) ((days / Season.LENGTH_DAYS) % Season.values().length);
        return Season.values()[index];
    }

    /** Days left before this season closes and its set is withdrawn. */
    public int daysRemaining() {
        long days = Math.max(0, daysSinceEpoch());
        return Season.LENGTH_DAYS - (int) (days % Season.LENGTH_DAYS);
    }

    /**
     * Whether a product is on the shelf today.
     *
     * Anything belonging to a season other than the current one is withdrawn —
     * that is the whole point. Everything outside the seasons is always for sale.
     */
    public boolean isOffered(String itemId) {
        Season owner = seasonOf(itemId);
        return owner == null || owner == current();
    }

    /** The season a product belongs to, or null if it is a staple. */
    public Season seasonOf(String itemId) {
        for (Season season : Season.values()) {
            if (season.contains(itemId)) {
                return season;
            }
        }
        return null;
    }

    /** What the current set costs bought piece by piece. */
    public int bundleFullPrice(java.util.List<ShopItem> catalogue) {
        Season season = current();
        int total = 0;
        for (ShopItem item : catalogue) {
            if (season.contains(item.getId())) {
                total += item.getPrice();
            }
        }
        return total;
    }

    /** How much of the current set the player already holds. */
    public int bundleOwnedCount() {
        PlayerProfileManager profile = PlayerProfileManager.getInstance(context);
        int owned = 0;
        for (String id : current().itemIds()) {
            if (profile.isItemPurchased(id)) {
                owned++;
            }
        }
        return owned;
    }

    /** Whole bundle means every piece that was ever on sale, not the banner. */
    public boolean ownsWholeBundle() {
        return bundleOwnedCount() >= purchasableCount();
    }

    /**
     * Records the set as bought.
     *
     * The banner is deliberately left out: it is the one piece of each season
     * that is never for sale, and a bundle that quietly handed it over would
     * make every banner in the game purchasable for the price of a set.
     */
    public void grantBundle() {
        PlayerProfileManager profile = PlayerProfileManager.getInstance(context);
        for (ShopItem item : SeasonCatalogue.ofSeason(context, current())) {
            if (item.isPurchasable()) {
                profile.addPurchasedItem(item.getId());
            }
        }
    }

    /** How many pieces of the set coins can actually buy. */
    public int purchasableCount() {
        int n = 0;
        for (ShopItem item : SeasonCatalogue.ofSeason(context, current())) {
            if (item.isPurchasable()) {
                n++;
            }
        }
        return n;
    }

    /** True the first time a player opens the shop in a new season. */
    public boolean isNewToPlayer() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SEEN_SEASON, 0) != current().number;
    }

    public void markSeen() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_SEEN_SEASON, current().number).apply();
    }
}
