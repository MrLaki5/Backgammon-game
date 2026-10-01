package games.mrlaki5.backgammon.Economy;

import android.content.Context;

import games.mrlaki5.backgammon.R;

public class ShopItem {

    public enum Category {
        ALL,
        CHECKERS,
        AVATAR_FRAME,
        DICE_SKIN,
        TITLE,
        THEME,
        COSMETIC,
        CONSUMABLE,
        BUNDLE,
        /** The playing surface itself: khatam, monabbat, mina. */
        BOARD,
        /** What the dice do when they land — dust, sparks, a ripple of light. */
        DICE_EFFECT,
        /** The banner that unfurls on a win. */
        BANNER,
        /** The set of sounds a move, a hit and a win make. */
        SOUND_SET
    }

    /**
     * How a piece is come by.
     *
     * A catalogue where everything has a price is a catalogue where nothing is
     * worth having. EARN marks the pieces that coins cannot buy at any amount —
     * those are the ones a collection is actually judged on.
     */
    public enum Acquisition {
        /** Coins, and nothing else. */
        BUY,
        /** Play, and nothing else. No price is ever shown for these. */
        EARN,
        /** Either: buy it now, or unlock it by playing. */
        BOTH
    }

    public enum Rarity {
        COMMON, RARE, EPIC, LEGENDARY;

        public String hexColor() {
            switch (this) {
                // Each is its material's lit edge: bronze, silver, turquoise, gold.
                case COMMON:    return "#D8A26A";
                case RARE:      return "#E6ECF2";
                case EPIC:      return "#8CE6D6";
                case LEGENDARY: return "#F7D488";
                default:        return "#D8A26A";
            }
        }

        /** Locale-aware rarity label — pass a Context (e.g. from the RecyclerView item's view). */
        public String label(Context context) {
            switch (this) {
                case COMMON:    return context.getString(R.string.shop_rarity_common);
                case RARE:      return context.getString(R.string.shop_rarity_rare);
                case EPIC:      return context.getString(R.string.shop_rarity_epic);
                case LEGENDARY: return context.getString(R.string.shop_rarity_legendary);
                default:        return "";
            }
        }
    }

    public final String id;
    public final String title;
    public final String description;
    public final int price;
    public final Category category;
    public final Rarity rarity;
    public final String iconEmoji;
    public final int iconRes;
    public final int nameRes;
    public final int unlockRequirement;
    public final String badge;
    public final int quantity;
    public final boolean isConsumable;

    /**
     * The longer text the detail sheet shows: what the craft is, and why this
     * piece looks the way it does. The short description stays for the card.
     * Empty when the piece has no story to tell.
     */
    public final String story;
    /** How this piece is obtained. */
    public final Acquisition acquisition;
    /**
     * The Journey chapter or challenge that grants an EARN/BOTH piece, as
     * {@link SeasonCatalogue} names it. Empty for a piece that is only bought.
     */
    public final String earnKey;
    /**
     * For BUNDLE items only: the ids of every piece this bundle grants on
     * purchase. Empty for non-bundle items. When a player buys the bundle every
     * id here is added to their owned-items list, so they can equip each piece
     * without a separate purchase.
     */
    public String[] bundleContents = new String[0];

    public ShopItem(String id, String title, String description, int price,
                    Category category, Rarity rarity, String iconEmoji,
                    int iconRes, int nameRes, int unlockRequirement,
                    String badge, int quantity, boolean isConsumable) {
        this(id, title, description, price, category, rarity, iconEmoji, iconRes,
                nameRes, unlockRequirement, badge, quantity, isConsumable,
                "", Acquisition.BUY, "");
    }

    public ShopItem(String id, String title, String description, int price,
                    Category category, Rarity rarity, String iconEmoji,
                    int iconRes, int nameRes, int unlockRequirement,
                    String badge, int quantity, boolean isConsumable,
                    String story, Acquisition acquisition, String earnKey) {
        this.story = story == null ? "" : story;
        this.acquisition = acquisition == null ? Acquisition.BUY : acquisition;
        this.earnKey = earnKey == null ? "" : earnKey;
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.rarity = rarity;
        this.iconEmoji = iconEmoji;
        this.iconRes = iconRes;
        this.nameRes = nameRes;
        this.unlockRequirement = unlockRequirement;
        this.badge = badge;
        this.quantity = quantity;
        this.isConsumable = isConsumable;
    }

    public ShopItem(String id, String title, String description, int price,
                    Category category, Rarity rarity, String iconEmoji,
                    int unlockRequirement, String badge) {
        this(id, title, description, price, category, rarity, iconEmoji,
                0, 0, unlockRequirement, badge, 0, category == Category.CONSUMABLE);
    }

    /**
     * A product with its own rendered artwork rather than a rarity medallion —
     * used where the thing itself is worth showing, like a set of checkers.
     */
    public static ShopItem crafted(String id, String title, String description, int price,
                                   Category category, Rarity rarity, int iconRes,
                                   int unlockRequirement) {
        return new ShopItem(id, title, description, price, category, rarity, "",
                iconRes, 0, unlockRequirement, null, 0, false);
    }

    /**
     * A crafted piece that also carries its story and how it is obtained.
     * This is the form every season piece uses.
     */
    public static ShopItem crafted(String id, String title, String description, String story,
                                   int price, Category category, Rarity rarity, int iconRes,
                                   int unlockRequirement, Acquisition acquisition, String earnKey) {
        return new ShopItem(id, title, description, price, category, rarity, "",
                iconRes, 0, unlockRequirement, null, 0, false,
                story, acquisition, earnKey);
    }

    /**
     * A piece coins cannot buy. Price is forced to zero so no screen can
     * accidentally offer it for sale.
     */
    public static ShopItem earned(String id, String title, String description, String story,
                                  Category category, Rarity rarity, int iconRes, String earnKey) {
        return new ShopItem(id, title, description, 0, category, rarity, "",
                iconRes, 0, 0, null, 0, false,
                story, Acquisition.EARN, earnKey);
    }

    public static ShopItem permanent(String id, String title, String description, int price,
                                     Category category, Rarity rarity, String iconEmoji) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, 0, null);
    }

    public static ShopItem withUnlock(String id, String title, String description, int price,
                                      Category category, Rarity rarity, String iconEmoji,
                                      int minWins) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, minWins, null);
    }

    public static ShopItem badged(String id, String title, String description, int price,
                                   Category category, Rarity rarity, String iconEmoji, String badge) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, 0, badge);
    }

    // Consumable factory methods
    public static ShopItem hintPack3(Context context) {
        return new ShopItem("hint_pack_3", context.getString(R.string.shop_hint_pack_3),
                context.getString(R.string.shop_hint_pack_3_desc), 50,
                Category.CONSUMABLE, Rarity.COMMON, "💡",
                R.drawable.ic_shop_hint, R.string.shop_hint_pack_3,
                0, null, 3, true);
    }

    public static ShopItem hintPack10(Context context) {
        return new ShopItem("hint_pack_10", context.getString(R.string.shop_hint_pack_10),
                context.getString(R.string.shop_hint_pack_10_desc), 150,
                Category.CONSUMABLE, Rarity.RARE, "💡",
                R.drawable.ic_shop_hint, R.string.shop_hint_pack_10,
                0, context.getString(R.string.shop_badge_discount), 10, true);
    }

    public static ShopItem undoPack3(Context context) {
        return new ShopItem("undo_pack_3", context.getString(R.string.shop_undo_pack_3),
                context.getString(R.string.shop_undo_pack_3_desc), 65,
                Category.CONSUMABLE, Rarity.COMMON, "↩️",
                R.drawable.ic_shop_undo, R.string.shop_undo_pack_3,
                0, null, 3, true);
    }

    public static ShopItem starterBundle(Context context) {
        return new ShopItem("starter_bundle", context.getString(R.string.shop_starter_bundle),
                context.getString(R.string.shop_starter_bundle_desc), 340,
                Category.BUNDLE, Rarity.EPIC, "🎁",
                R.drawable.ic_shop_bundle, R.string.shop_starter_bundle,
                0, context.getString(R.string.shop_badge_special), 0, false);
    }

    /**
     * A themed bundle: several individual pieces sold together at a discount.
     * Purchasing it grants every id in {@code contents} to the player's owned
     * collection, so they can equip each piece without a second purchase.
     */
    public static ShopItem themedBundle(String id, String title, String description,
                                        String story, int price, Rarity rarity,
                                        int iconRes, String badge, String[] contents) {
        ShopItem item = new ShopItem(id, title, description, price,
                Category.BUNDLE, rarity, "", iconRes, 0, 0, badge, 0, false,
                story, Acquisition.BUY, "");
        item.bundleContents = contents;
        return item;
    }

    public String getId()              { return id; }
    public String getTitle()           { return title; }
    public String getDescription()     { return description; }
    public int getPrice()              { return price; }
    public Category getCategory()      { return category; }
    public Rarity getRarity()          { return rarity; }
    public String getIconEmoji()       { return iconEmoji; }
    public int getIconRes()            { return iconRes; }
    public int getNameRes()            { return nameRes; }
    public int getUnlockRequirement()  { return unlockRequirement; }
    public String getBadge()           { return badge; }
    public int getQuantity()           { return quantity; }
    public boolean isConsumable()      { return isConsumable; }
    public boolean isFree()            { return price <= 0; }
    public boolean hasUnlockReq()      { return unlockRequirement > 0; }
    public String getStory()           { return story; }
    public Acquisition getAcquisition(){ return acquisition; }
    public String getEarnKey()         { return earnKey; }

    /** True when no amount of coins buys this piece. */
    public boolean isEarnOnly()        { return acquisition == Acquisition.EARN; }
    /** True when the shop may show a price for this piece. */
    public boolean isPurchasable()     { return acquisition != Acquisition.EARN; }
}
