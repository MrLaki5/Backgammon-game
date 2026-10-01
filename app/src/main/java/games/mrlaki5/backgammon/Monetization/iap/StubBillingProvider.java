package games.mrlaki5.backgammon.Monetization.iap;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stub billing provider for development/testing.
 * Simulates purchases without connecting to any real store.
 */
public class StubBillingProvider implements BillingProvider {

    private static final String TAG = "StubBilling";
    private boolean connected = false;
    private final Set<String> ownedSkus = new HashSet<>();

    @Override
    public void connect(Activity activity, BillingConnectionCallback callback) {
        Log.d(TAG, "Billing connected (stub)");
        connected = true;
        if (callback != null) callback.onConnected();
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public void queryProducts(List<String> skus, ProductQueryCallback callback) {
        List<Product> products = new ArrayList<>();
        for (String sku : skus) {
            boolean owned = ownedSkus.contains(sku);
            switch (sku) {
                case Product.SKU_REMOVE_ADS:
                    products.add(new Product(sku, "Remove Ads",
                            "Remove all interstitial ads permanently", "29,000 T", owned));
                    break;
                case Product.SKU_THEME_CYBERPUNK:
                    products.add(new Product(sku, "Cyberpunk Theme",
                            "Unlock the Cyberpunk board theme", "19,000 T", owned));
                    break;
                case Product.SKU_THEME_LUXURY:
                    products.add(new Product(sku, "Persian Luxury Theme",
                            "Unlock the Persian Luxury board theme", "19,000 T", owned));
                    break;
                case Product.SKU_THEME_POP_ART:
                    products.add(new Product(sku, "Pop Art Theme",
                            "Unlock the Pop Art board theme", "14,000 T", owned));
                    break;
            }
        }
        if (callback != null) callback.onProductsLoaded(products);
    }

    @Override
    public void purchase(Activity activity, String sku, PurchaseCallback callback) {
        Log.d(TAG, "Purchase: " + sku + " (stub — auto-success)");
        ownedSkus.add(sku);
        if (callback != null) callback.onPurchaseSuccess(sku);
    }

    @Override
    public void queryPurchases(PurchaseRestoreCallback callback) {
        if (callback != null) callback.onPurchasesRestored(new ArrayList<>(ownedSkus));
    }

    @Override
    public void consume(String purchaseToken) {
        // Non-consumable items — no-op
    }

    @Override
    public boolean isReady() {
        return connected;
    }
}
