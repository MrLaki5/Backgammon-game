package games.mrlaki5.backgammon.Monetization;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.Monetization.ads.AdManager;
import games.mrlaki5.backgammon.Monetization.iap.BillingProvider;
import games.mrlaki5.backgammon.Monetization.iap.Product;
import games.mrlaki5.backgammon.Monetization.iap.PurchaseCallback;

/**
 * Manages premium/purchased features:
 * - Ad removal
 * - Locked theme unlocking
 *
 * Persists purchase state locally and syncs with billing provider on startup.
 * No coins, no gambling, no pay-to-win mechanics (product differentiation).
 */
public class PremiumManager {

    private static final String PREFS_NAME = "premium_prefs";
    private static final String KEY_ADS_REMOVED = "ads_removed";
    private static final String KEY_OWNED_THEMES = "owned_themes";

    /** Themes that are free by default. */
    private static final Set<Integer> FREE_THEMES = new HashSet<>(Arrays.asList(
            GamePreferences.THEME_ROYAL  // Royal theme is always free
    ));

    /** Maps theme index to IAP SKU. */
    private static final String[] THEME_SKUS = {
            null,                           // 0: Royal — free
            Product.SKU_THEME_POP_ART,      // 1: Pop Art
            Product.SKU_THEME_CYBERPUNK,    // 2: Cyberpunk
            Product.SKU_THEME_LUXURY        // 3: Persian Luxury
    };

    private final SharedPreferences prefs;
    private final BillingProvider billingProvider;
    private AdManager adManager; // Set after initialization

    public PremiumManager(Context context, BillingProvider billingProvider) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.billingProvider = billingProvider;
    }

    public void setAdManager(AdManager adManager) {
        this.adManager = adManager;
        // Sync ad removal state
        if (isAdsRemoved()) {
            adManager.setAdsRemoved(true);
        }
    }

    /**
     * Connects to billing and restores purchases. Call on app startup.
     */
    public void initialize(Activity activity) {
        billingProvider.connect(activity, new BillingProvider.BillingConnectionCallback() {
            @Override
            public void onConnected() {
                restorePurchases();
            }

            @Override
            public void onDisconnected() {}

            @Override
            public void onError(String error) {}
        });
    }

    /**
     * Restores purchases from the store (handles reinstalls/new devices).
     */
    public void restorePurchases() {
        billingProvider.queryPurchases(new BillingProvider.PurchaseRestoreCallback() {
            @Override
            public void onPurchasesRestored(List<String> ownedSkus) {
                for (String sku : ownedSkus) {
                    applyPurchase(sku);
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    /**
     * Initiates purchase of "Remove Ads".
     */
    public void purchaseRemoveAds(Activity activity, PurchaseCallback callback) {
        billingProvider.purchase(activity, Product.SKU_REMOVE_ADS, new PurchaseCallback() {
            @Override
            public void onPurchaseSuccess(String sku) {
                applyPurchase(sku);
                if (callback != null) callback.onPurchaseSuccess(sku);
            }

            @Override
            public void onPurchaseFailed(String sku, String error) {
                if (callback != null) callback.onPurchaseFailed(sku, error);
            }

            @Override
            public void onPurchaseCancelled(String sku) {
                if (callback != null) callback.onPurchaseCancelled(sku);
            }
        });
    }

    /**
     * Initiates purchase of a theme pack.
     */
    public void purchaseTheme(Activity activity, int themeIndex, PurchaseCallback callback) {
        if (themeIndex < 0 || themeIndex >= THEME_SKUS.length) return;
        String sku = THEME_SKUS[themeIndex];
        if (sku == null) return; // Free theme

        billingProvider.purchase(activity, sku, new PurchaseCallback() {
            @Override
            public void onPurchaseSuccess(String s) {
                applyPurchase(s);
                if (callback != null) callback.onPurchaseSuccess(s);
            }

            @Override
            public void onPurchaseFailed(String s, String error) {
                if (callback != null) callback.onPurchaseFailed(s, error);
            }

            @Override
            public void onPurchaseCancelled(String s) {
                if (callback != null) callback.onPurchaseCancelled(s);
            }
        });
    }

    // --- Query state ---

    /**
     * Returns true if ads have been removed via purchase.
     */
    public boolean isAdsRemoved() {
        return prefs.getBoolean(KEY_ADS_REMOVED, false);
    }

    /**
     * Returns true if the given theme is unlocked (free or purchased).
     */
    public boolean isThemeUnlocked(int themeIndex) {
        if (FREE_THEMES.contains(themeIndex)) return true;
        Set<String> owned = prefs.getStringSet(KEY_OWNED_THEMES, new HashSet<>());
        return owned.contains(String.valueOf(themeIndex));
    }

    /**
     * Returns the SKU for a locked theme (for showing price), or null if free.
     */
    public String getThemeSku(int themeIndex) {
        if (themeIndex < 0 || themeIndex >= THEME_SKUS.length) return null;
        return THEME_SKUS[themeIndex];
    }

    /**
     * Disconnects billing. Call in onDestroy.
     */
    public void destroy() {
        billingProvider.disconnect();
    }

    // --- Internal ---

    private void applyPurchase(String sku) {
        if (Product.SKU_REMOVE_ADS.equals(sku)) {
            prefs.edit().putBoolean(KEY_ADS_REMOVED, true).apply();
            if (adManager != null) adManager.setAdsRemoved(true);
        } else {
            // It's a theme purchase — find which theme
            for (int i = 0; i < THEME_SKUS.length; i++) {
                if (sku.equals(THEME_SKUS[i])) {
                    Set<String> owned = new HashSet<>(
                            prefs.getStringSet(KEY_OWNED_THEMES, new HashSet<>()));
                    owned.add(String.valueOf(i));
                    prefs.edit().putStringSet(KEY_OWNED_THEMES, owned).apply();
                    break;
                }
            }
        }
    }
}
