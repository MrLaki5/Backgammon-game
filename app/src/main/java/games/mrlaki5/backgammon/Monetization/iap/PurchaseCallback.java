package games.mrlaki5.backgammon.Monetization.iap;

/**
 * Callback for purchase operations.
 */
public interface PurchaseCallback {
    /** Purchase completed successfully. */
    void onPurchaseSuccess(String sku);

    /** Purchase failed or was cancelled. */
    void onPurchaseFailed(String sku, String error);

    /** Purchase was cancelled by the user. */
    void onPurchaseCancelled(String sku);
}
