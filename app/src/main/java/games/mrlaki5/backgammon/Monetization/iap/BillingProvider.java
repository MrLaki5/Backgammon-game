package games.mrlaki5.backgammon.Monetization.iap;

import android.app.Activity;

import java.util.List;

/**
 * Abstraction for in-app billing. Each app store (Bazaar, Myket) has its own SDK.
 * Implementations:
 * - BazaarBillingProvider: Uses Cafe Bazaar IAB SDK
 * - MyketBillingProvider: Uses Myket IAP SDK
 * - StubBillingProvider: For development/testing
 *
 * Important: NO coin/gambling system. Only one-time purchases:
 * - Remove ads
 * - Premium theme packs
 */
public interface BillingProvider {

    /**
     * Connects to the billing service. Must be called before any other operations.
     */
    void connect(Activity activity, BillingConnectionCallback callback);

    /**
     * Disconnects from the billing service. Call in onDestroy.
     */
    void disconnect();

    /**
     * Queries available products from the store.
     */
    void queryProducts(List<String> skus, ProductQueryCallback callback);

    /**
     * Initiates a purchase flow for the given SKU.
     */
    void purchase(Activity activity, String sku, PurchaseCallback callback);

    /**
     * Queries previously purchased items (to restore purchases on reinstall).
     */
    void queryPurchases(PurchaseRestoreCallback callback);

    /**
     * Consumes a purchase (only for consumable items — not used in this app
     * since all items are non-consumable, but required by some SDKs).
     */
    void consume(String purchaseToken);

    /**
     * Returns true if the billing service is connected and ready.
     */
    boolean isReady();

    // --- Inner callback interfaces ---

    interface BillingConnectionCallback {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    interface ProductQueryCallback {
        void onProductsLoaded(List<Product> products);
        void onError(String error);
    }

    interface PurchaseRestoreCallback {
        void onPurchasesRestored(List<String> ownedSkus);
        void onError(String error);
    }
}
