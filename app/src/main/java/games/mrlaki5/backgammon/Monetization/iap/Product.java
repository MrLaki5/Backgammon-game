package games.mrlaki5.backgammon.Monetization.iap;

/**
 * Represents a purchasable product (in-app purchase item).
 *
 * Products available (from spec — NO coins/gambling-like items):
 * - Remove Ads (one-time)
 * - Theme packs (VIP themes, one-time each)
 */
public class Product {

    /** Well-known product SKUs. */
    public static final String SKU_REMOVE_ADS = "remove_ads";
    public static final String SKU_THEME_CYBERPUNK = "theme_cyberpunk";
    public static final String SKU_THEME_LUXURY = "theme_luxury";
    public static final String SKU_THEME_POP_ART = "theme_pop_art";

    private final String sku;
    private final String title;
    private final String description;
    private final String price;        // Formatted price string from store
    private final boolean purchased;

    public Product(String sku, String title, String description, String price, boolean purchased) {
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.price = price;
        this.purchased = purchased;
    }

    public String getSku() { return sku; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public boolean isPurchased() { return purchased; }
}
