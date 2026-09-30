package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.content.SharedPreferences;
import games.mrlaki5.backgammon.Analytics.GameAnalytics;

/**
 * Manages the in-game coin economy (Gold Coins).
 * Coins are EARNED through gameplay only — never purchased with real money.
 * Persistence: SharedPreferences. No backend needed.
 */
public class CoinManager {

    private static final String PREFS_NAME = "coin_economy_prefs";
    private static final String KEY_BALANCE = "coin_balance";
    private static final String KEY_TOTAL_EARNED = "total_coins_earned";
    private static final String KEY_TOTAL_SPENT = "total_coins_spent";

    private final SharedPreferences prefs;

    public CoinManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized int getBalance() {
        return prefs.getInt(KEY_BALANCE, 0);
    }

    /**
     * Awards coins to the player.
     * @param amount positive number of coins
     * @param source identifier for analytics (e.g., "game_win", "daily_challenge", "rewarded_ad")
     * @return new balance
     */
    public synchronized int earn(int amount, String source) {
        if (amount <= 0) return getBalance();
        int newBalance = getBalance() + amount;
        int totalEarned = prefs.getInt(KEY_TOTAL_EARNED, 0) + amount;
        prefs.edit()
                .putInt(KEY_BALANCE, newBalance)
                .putInt(KEY_TOTAL_EARNED, totalEarned)
                .commit();
        GameAnalytics.get().trackCoinEarned(amount, source, newBalance);
        return newBalance;
    }

    /**
     * Spends coins. Returns true if successful, false if insufficient balance.
     * @param amount positive number of coins to spend
     * @param item identifier for analytics (e.g., "hint", "undo", "theme_rental")
     */
    public synchronized boolean spend(int amount, String item) {
        if (amount <= 0) return false;
        int balance = getBalance();
        if (balance < amount) return false;
        int newBalance = balance - amount;
        int totalSpent = prefs.getInt(KEY_TOTAL_SPENT, 0) + amount;
        prefs.edit()
                .putInt(KEY_BALANCE, newBalance)
                .putInt(KEY_TOTAL_SPENT, totalSpent)
                .commit();
        GameAnalytics.get().trackCoinSpent(amount, item, newBalance);
        return true;
    }

    public synchronized boolean canAfford(int amount) {
        return getBalance() >= amount;
    }

    public synchronized int getTotalEarned() {
        return prefs.getInt(KEY_TOTAL_EARNED, 0);
    }

    public synchronized int getTotalSpent() {
        return prefs.getInt(KEY_TOTAL_SPENT, 0);
    }
}
