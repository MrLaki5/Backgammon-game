# Shop Overhaul — Royal Backgammon

**Scope:** Redesign the entire coin shop to be visually professional, psychologically compelling, and economically balanced. Every change is in the `app/` module only — DO NOT touch `game-core/`.

---

## Economy Analysis (Read First)

Current daily coin income for an active player:
- 5 games/day, 3 wins at mixed difficulty: ~110 coins
- Daily challenge: 25 coins
- First game of day: 5 coins
- Login bonus: ~20 coins average
- **Total: ~160 coins/day**

Shop prices MUST reflect this. Target:
- **Common**: 50–200 coins (≤ 2 days)
- **Rare**: 300–700 coins (2–5 days)
- **Epic**: 800–2000 coins (6–14 days)
- **Legendary**: 2500–5000 coins (15–32 days)

---

## Phase 1 — ShopItem.java (complete rewrite)

File: `app/src/main/java/games/mrlaki5/backgammon/Economy/ShopItem.java`

```java
package games.mrlaki5.backgammon.Economy;

public class ShopItem {

    public enum Category {
        AVATAR_FRAME,
        DICE_SKIN,
        TITLE,
        THEME,
        RENTAL
    }

    public enum Rarity {
        COMMON, RARE, EPIC, LEGENDARY;

        /** Color int for this rarity (use Color.parseColor in views). */
        public String hexColor() {
            switch (this) {
                case COMMON:    return "#9E9E9E";
                case RARE:      return "#2196F3";
                case EPIC:      return "#9C27B0";
                case LEGENDARY: return "#FFB300";
                default:        return "#9E9E9E";
            }
        }

        public String label() {
            switch (this) {
                case COMMON:    return "معمولی";
                case RARE:      return "نادر";
                case EPIC:      return "حماسی";
                case LEGENDARY: return "افسانه‌ای";
                default:        return "";
            }
        }
    }

    private final String id;
    private final String title;
    private final String description;
    private final int price;
    private final Category category;
    private final Rarity rarity;
    private final String iconEmoji;
    private final int unlockRequirement; // 0 = no requirement; else min total wins
    private final boolean isRental;      // true = 24h rental, not permanent
    private final String badge;          // null | "NEW" | "HOT" | "OFFER"

    public ShopItem(String id, String title, String description, int price,
                    Category category, Rarity rarity, String iconEmoji,
                    int unlockRequirement, boolean isRental, String badge) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.rarity = rarity;
        this.iconEmoji = iconEmoji;
        this.unlockRequirement = unlockRequirement;
        this.isRental = isRental;
        this.badge = badge;
    }

    // Convenience constructors
    public static ShopItem permanent(String id, String title, String description, int price,
                                     Category category, Rarity rarity, String iconEmoji) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, 0, false, null);
    }

    public static ShopItem withUnlock(String id, String title, String description, int price,
                                      Category category, Rarity rarity, String iconEmoji,
                                      int minWins) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, minWins, false, null);
    }

    public static ShopItem rental(String id, String title, String description, int price,
                                  Category originalCategory, Rarity rarity, String iconEmoji) {
        return new ShopItem(id, title, description, price, Category.RENTAL, rarity, iconEmoji, 0, true, "اجاره");
    }

    public static ShopItem badged(String id, String title, String description, int price,
                                   Category category, Rarity rarity, String iconEmoji, String badge) {
        return new ShopItem(id, title, description, price, category, rarity, iconEmoji, 0, false, badge);
    }

    public String getId()              { return id; }
    public String getTitle()           { return title; }
    public String getDescription()     { return description; }
    public int getPrice()              { return price; }
    public Category getCategory()      { return category; }
    public Rarity getRarity()          { return rarity; }
    public String getIconEmoji()       { return iconEmoji; }
    public int getUnlockRequirement()  { return unlockRequirement; }
    public boolean isRental()          { return isRental; }
    public String getBadge()           { return badge; }
    public boolean isFree()            { return price <= 0; }
    public boolean hasUnlockReq()      { return unlockRequirement > 0; }
}
```

---

## Phase 2 — CoinShopActivity.java (complete catalog rebuild)

File: `app/src/main/java/games/mrlaki5/backgammon/Economy/CoinShopActivity.java`

Replace `buildShopCatalog()` with the full catalog below. Keep all other methods (onCreate, setupCategoryTabs, filterItems, updateBalance) unchanged.

Add import at top:
```java
import games.mrlaki5.backgammon.Database.PlayerProfileManager;
```

**New buildShopCatalog() method:**

```java
private void buildShopCatalog() {
    allItems.clear();

    // ═══════════════════════════════════════════
    // AVATAR FRAMES — 8 items
    // ═══════════════════════════════════════════
    allItems.add(ShopItem.permanent(
        "frame_default", "فریم کلاسیک", "حاشیه چوبی ساده",
        0, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.COMMON, "🪵"));

    allItems.add(ShopItem.permanent(
        "frame_bronze", "فریم برنز", "حاشیه برنز عتیقه صیقلی",
        150, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.COMMON, "🥉"));

    allItems.add(ShopItem.withUnlock(
        "frame_silver", "فریم نقره", "حاشیه نقره استرلینگ درخشان",
        400, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.RARE, "🥈", 10));

    allItems.add(ShopItem.withUnlock(
        "frame_carpet", "فریم فرش ایرانی", "نقوش سنتی فرش دستباف اصفهان",
        550, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.RARE, "🟥", 20));

    allItems.add(ShopItem.withUnlock(
        "frame_gold", "فریم طلا", "طلای ۲۴ عیار ایرانی با نقش شاهانه",
        1000, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.EPIC, "🥇", 50));

    allItems.add(ShopItem.withUnlock(
        "frame_peacock", "فریم طاووس", "نقوش طاووس فارسی با جواهرات",
        1500, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.EPIC, "🦚", 75));

    allItems.add(new ShopItem(
        "frame_diamond", "تاج الماس", "تاج سلطنتی با الماس‌های درخشان",
        3000, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.LEGENDARY, "💎", 150, false, "پرطرفدار"));

    allItems.add(new ShopItem(
        "frame_sultan", "فریم سلطان", "فریم ویژه سلاطین تخته‌نرد",
        5000, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.LEGENDARY, "👑", 300, false, null));

    // ═══════════════════════════════════════════
    // DICE SKINS — 6 items
    // ═══════════════════════════════════════════
    allItems.add(ShopItem.permanent(
        "dice_default", "تاس استخوانی", "تاس کلاسیک سفید",
        0, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.COMMON, "🎲"));

    allItems.add(ShopItem.permanent(
        "dice_walnut", "تاس گردو", "تاس ماهونی تیره طبیعی",
        200, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.COMMON, "🟫"));

    allItems.add(ShopItem.withUnlock(
        "dice_ruby", "تاس یاقوت", "تاس یاقوت قرمز آتشین",
        500, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.RARE, "🔴", 15));

    allItems.add(ShopItem.withUnlock(
        "dice_marble", "تاس مرمر", "تاس مرمر ابروباد زیبا",
        700, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.RARE, "⬜", 30));

    allItems.add(new ShopItem(
        "dice_crystal", "تاس کریستال", "تاس بلور شفاف با درخش نور",
        1400, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.EPIC, "🔷", 100, false, "جدید"));

    allItems.add(new ShopItem(
        "dice_dragon", "تاس اژدها", "تاس اسطوره‌ای با نقش اژدها",
        3500, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.LEGENDARY, "🐉", 200, false, null));

    // ═══════════════════════════════════════════
    // TITLES — 6 items (Farsi)
    // ═══════════════════════════════════════════
    allItems.add(ShopItem.permanent(
        "title_beginner", "نوآموز", "عنوان شروع بازیکن",
        0, ShopItem.Category.TITLE, ShopItem.Rarity.COMMON, "🌱"));

    allItems.add(ShopItem.withUnlock(
        "title_sharp", "تیزهوش", "بازیکن باهوش تخته‌نرد",
        100, ShopItem.Category.TITLE, ShopItem.Rarity.COMMON, "🧩", 5));

    allItems.add(ShopItem.withUnlock(
        "title_tactician", "تاکتیسین", "خبره استراتژی و تاکتیک",
        350, ShopItem.Category.TITLE, ShopItem.Rarity.RARE, "⚔️", 25));

    allItems.add(ShopItem.withUnlock(
        "title_master", "استاد تخته", "استاد شناخته‌شده تخته‌نرد",
        600, ShopItem.Category.TITLE, ShopItem.Rarity.RARE, "🎓", 60));

    allItems.add(new ShopItem(
        "title_king", "شاه‌باز", "بازیکن سلطنتی تخته‌نرد",
        1200, ShopItem.Category.TITLE, ShopItem.Rarity.EPIC, "♔", 100, false, null));

    allItems.add(new ShopItem(
        "title_sultan", "سلطان تخته", "عنوان افسانه‌ای — فقط برای نخبگان",
        2500, ShopItem.Category.TITLE, ShopItem.Rarity.LEGENDARY, "🏆", 250, false, null));

    // ═══════════════════════════════════════════
    // THEMES — 4 items
    // ═══════════════════════════════════════════
    allItems.add(ShopItem.permanent(
        "theme_royal", "تخته سلطنتی", "تخته کلاسیک — پیش‌فرض",
        0, ShopItem.Category.THEME, ShopItem.Rarity.COMMON, "♟️"));

    allItems.add(ShopItem.badged(
        "theme_pop_art", "تخته پاپ آرت", "تخته کمیک‌بوک رنگارنگ",
        600, ShopItem.Category.THEME, ShopItem.Rarity.RARE, "🎨", "پرطرفدار"));

    allItems.add(ShopItem.permanent(
        "theme_cyberpunk", "تخته سایبرپانک", "تخته آینده‌نگر نئون‌دار",
        1000, ShopItem.Category.THEME, ShopItem.Rarity.EPIC, "⚡"));

    allItems.add(new ShopItem(
        "theme_luxury", "تخته فاخر ایرانی", "عاج و طلا — بهترین تجربه",
        2000, ShopItem.Category.THEME, ShopItem.Rarity.LEGENDARY, "🏛️", 0, false, "برتر"));

    // ═══════════════════════════════════════════
    // RENTALS — 24-hour try-before-you-buy
    // ═══════════════════════════════════════════
    allItems.add(ShopItem.rental(
        "rental_cyberpunk", "سایبرپانک — ۲۴ ساعت", "تخته نئون‌دار را امتحان کنید",
        80, ShopItem.Category.THEME, ShopItem.Rarity.EPIC, "⚡"));

    allItems.add(ShopItem.rental(
        "rental_luxury", "تخته فاخر — ۲۴ ساعت", "تجربه لوکس را امتحان کنید",
        120, ShopItem.Category.THEME, ShopItem.Rarity.LEGENDARY, "🏛️"));

    allItems.add(ShopItem.rental(
        "rental_diamond", "تاج الماس — ۲۴ ساعت", "فریم افسانه‌ای برای یک روز",
        50, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.LEGENDARY, "💎"));

    allItems.add(ShopItem.rental(
        "rental_sultan", "فریم سلطان — ۲۴ ساعت", "فریم سلطانی برای یک روز",
        80, ShopItem.Category.AVATAR_FRAME, ShopItem.Rarity.LEGENDARY, "👑"));

    allItems.add(ShopItem.rental(
        "rental_dragon", "تاس اژدها — ۲۴ ساعت", "تاس افسانه‌ای برای یک روز",
        60, ShopItem.Category.DICE_SKIN, ShopItem.Rarity.LEGENDARY, "🐉"));
}
```

Also update `setupCategoryTabs()` to add the RENTAL tab:
- The existing `rbCategoryThemes` RadioButton id becomes the THEMES tab (change text to `@string/board_theme`)
- Add a new `rbCategoryRental` RadioButton after it with text `@string/shop_category_rental`
- In the `OnCheckedChangeListener` add: `else if (checkedId == R.id.rbCategoryRental) { filterItems(ShopItem.Category.RENTAL); }`

---

## Phase 3 — ShopAdapter.java (visual rarity system)

File: `app/src/main/java/games/mrlaki5/backgammon/Economy/ShopAdapter.java`

Replace the entire `getView()` method with:

```java
@Override
public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
        convertView = LayoutInflater.from(context)
                .inflate(R.layout.item_shop_card, parent, false);
    }

    ShopItem item = getItem(position);

    // Find views
    TextView tvIcon      = convertView.findViewById(R.id.tvShopItemIcon);
    TextView tvTitle     = convertView.findViewById(R.id.tvShopItemTitle);
    TextView tvDesc      = convertView.findViewById(R.id.tvShopItemDesc);
    TextView tvRarity    = convertView.findViewById(R.id.tvShopItemRarity);
    TextView tvBadge     = convertView.findViewById(R.id.tvShopItemBadge);
    TextView tvUnlock    = convertView.findViewById(R.id.tvShopItemUnlock);
    View     rarityBar   = convertView.findViewById(R.id.viewRarityBar);
    Button   btnAction   = convertView.findViewById(R.id.btnShopItemAction);

    // Content
    tvIcon.setText(item.getIconEmoji());
    tvTitle.setText(item.getTitle());
    tvDesc.setText(item.getDescription());

    // Rarity
    int rarityColor = android.graphics.Color.parseColor(item.getRarity().hexColor());
    tvRarity.setText(item.getRarity().label());
    tvRarity.setTextColor(rarityColor);
    if (rarityBar != null) rarityBar.setBackgroundColor(rarityColor);

    // Badge
    if (tvBadge != null) {
        if (item.getBadge() != null) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(item.getBadge());
        } else {
            tvBadge.setVisibility(View.GONE);
        }
    }

    // Unlock requirement
    int playerWins = profileManager.getTotalWins();
    boolean isLocked = item.hasUnlockReq() && playerWins < item.getUnlockRequirement();
    if (tvUnlock != null) {
        if (isLocked) {
            tvUnlock.setVisibility(View.VISIBLE);
            tvUnlock.setText("🔒 نیاز به " + item.getUnlockRequirement() + " برد");
        } else {
            tvUnlock.setVisibility(View.GONE);
        }
    }

    // Owned / equip state
    boolean isOwned   = item.isFree() || profileManager.isItemPurchased(item.getId());
    boolean isEquipped = !isLocked && isItemEquipped(item);

    if (isLocked) {
        btnAction.setText("قفل");
        btnAction.setEnabled(false);
        btnAction.setAlpha(0.5f);
    } else if (isEquipped) {
        btnAction.setText(R.string.shop_item_equipped);
        btnAction.setBackgroundResource(R.drawable.neuro_secondary_button);
        btnAction.setEnabled(false);
        btnAction.setAlpha(1f);
    } else if (isOwned) {
        btnAction.setText(R.string.shop_item_equip);
        btnAction.setBackgroundResource(R.drawable.neuro_secondary_button);
        btnAction.setEnabled(true);
        btnAction.setAlpha(1f);
        btnAction.setOnClickListener(v -> {
            equipItem(item);
            notifyDataSetChanged();
            if (actionListener != null) actionListener.onItemAction();
        });
    } else {
        // Rental vs purchase label
        String priceLabel = item.isRental()
                ? "اجاره " + item.getPrice() + " 🪙"
                : item.getPrice() + " 🪙";
        btnAction.setText(priceLabel);
        btnAction.setBackgroundResource(R.drawable.neuro_primary_button);
        btnAction.setEnabled(true);
        btnAction.setAlpha(1f);
        btnAction.setOnClickListener(v -> {
            if (coinManager.spend(item.getPrice(), "shop_" + item.getId())) {
                profileManager.addPurchasedItem(item.getId());
                equipItem(item);
                Toast.makeText(context, R.string.shop_purchase_success, Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                if (actionListener != null) actionListener.onItemAction();
            } else {
                Toast.makeText(context, R.string.insufficient_coins, Toast.LENGTH_SHORT).show();
            }
        });
    }

    return convertView;
}
```

---

## Phase 4 — item_shop_card.xml (visual redesign)

File: `app/src/main/res/layout/item_shop_card.xml`

Complete replacement:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="5dp">

    <!-- Card background -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_pause_dialog"
        android:elevation="4dp"
        android:orientation="vertical"
        android:padding="0dp">

        <!-- Rarity color bar (top) -->
        <View
            android:id="@+id/viewRarityBar"
            android:layout_width="match_parent"
            android:layout_height="3dp"
            android:background="#9E9E9E" />

        <!-- Card body -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="10dp">

            <!-- Icon -->
            <TextView
                android:id="@+id/tvShopItemIcon"
                android:layout_width="56dp"
                android:layout_height="56dp"
                android:background="@drawable/bg_pause_button"
                android:gravity="center"
                android:text="🖼️"
                android:textSize="28sp" />

            <!-- Rarity label -->
            <TextView
                android:id="@+id/tvShopItemRarity"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:fontFamily="sans-serif-medium"
                android:letterSpacing="0.05"
                android:text="معمولی"
                android:textColor="#9E9E9E"
                android:textSize="9sp"
                android:textAllCaps="false" />

            <!-- Title -->
            <TextView
                android:id="@+id/tvShopItemTitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="3dp"
                android:fontFamily="sans-serif-medium"
                android:gravity="center"
                android:maxLines="1"
                android:textColor="@color/text_primary"
                android:textSize="13sp"
                android:textStyle="bold" />

            <!-- Description -->
            <TextView
                android:id="@+id/tvShopItemDesc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="2dp"
                android:gravity="center"
                android:maxLines="2"
                android:textColor="@color/accent_slate"
                android:textSize="10sp" />

            <!-- Unlock requirement (hidden when not needed) -->
            <TextView
                android:id="@+id/tvShopItemUnlock"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:gravity="center"
                android:textColor="#FF7043"
                android:textSize="10sp"
                android:visibility="gone" />

            <!-- Buy / Equip button -->
            <Button
                android:id="@+id/btnShopItemAction"
                android:layout_width="match_parent"
                android:layout_height="34dp"
                android:layout_marginTop="8dp"
                android:background="@drawable/neuro_primary_button"
                android:fontFamily="sans-serif-medium"
                android:minHeight="0dp"
                android:paddingStart="6dp"
                android:paddingEnd="6dp"
                android:text="200 🪙"
                android:textAllCaps="false"
                android:textColor="@color/surface_base"
                android:textSize="11sp"
                android:textStyle="bold" />

        </LinearLayout>
    </LinearLayout>

    <!-- Badge overlay (top-right corner) -->
    <TextView
        android:id="@+id/tvShopItemBadge"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="top|end"
        android:layout_margin="3dp"
        android:background="#E65100"
        android:fontFamily="sans-serif-black"
        android:paddingStart="5dp"
        android:paddingTop="2dp"
        android:paddingEnd="5dp"
        android:paddingBottom="2dp"
        android:text="جدید"
        android:textColor="#FFFFFF"
        android:textSize="8sp"
        android:textStyle="bold"
        android:visibility="gone" />

</FrameLayout>
```

---

## Phase 5 — activity_coin_shop.xml (add RENTAL tab)

File: `app/src/main/res/layout/activity_coin_shop.xml`

After the existing `rbCategoryThemes` RadioButton, add:

```xml
<RadioButton
    android:id="@+id/rbCategoryRental"
    android:layout_width="wrap_content"
    android:layout_height="32dp"
    android:background="@drawable/bg_pause_button"
    android:button="@null"
    android:gravity="center"
    android:paddingStart="12dp"
    android:paddingEnd="12dp"
    android:text="@string/shop_category_rental"
    android:textColor="@color/text_primary"
    android:textSize="12sp"
    android:textStyle="bold" />
```

Also change `rbCategoryThemes` text from `@string/theme_rental` to `@string/board_theme`.

---

## Phase 6 — PlayerProfileManager.java (add getTotalWins)

File: `app/src/main/java/games/mrlaki5/backgammon/Database/PlayerProfileManager.java`

The `ShopAdapter` calls `profileManager.getTotalWins()`. Verify this method exists. If it doesn't, add it:

```java
/** Returns the total wins ever recorded for unlock requirement checks. */
public int getTotalWins() {
    return prefs.getInt("total_wins", 0);
}
```

Also ensure `recordGameResult()` increments this key when won:
```java
if (won) {
    int wins = prefs.getInt("total_wins", 0);
    prefs.edit().putInt("total_wins", wins + 1).apply();
}
```

---

## Phase 7 — strings.xml additions

File: `app/src/main/res/values/strings.xml`

Add inside `<resources>`:
```xml
<string name="shop_category_rental">اجاره‌ای (۲۴ ساعت)</string>
<string name="shop_rarity_common">معمولی</string>
<string name="shop_rarity_rare">نادر</string>
<string name="shop_rarity_epic">حماسی</string>
<string name="shop_rarity_legendary">افسانه‌ای</string>
<string name="shop_locked_format">🔒 %1$d برد لازم</string>
<string name="shop_rental_button">اجاره %1$d 🪙</string>
<string name="shop_rental_active">در حال اجاره</string>
```

File: `app/src/main/res/values-fa/strings.xml` — same strings (already Farsi above).

---

## Phase 8 — CoinConfig.java (rebalance shop constants)

File: `app/src/main/java/games/mrlaki5/backgammon/Economy/CoinConfig.java`

Replace the `// === Spending ===` section:

```java
// === Spending ===
public static final int HINT_COST         = 20;
public static final int UNDO_COST         = 30;

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
// Themes
public static final int THEME_POP_ART_PRICE  = 600;
public static final int THEME_CYBERPUNK_PRICE = 1000;
public static final int THEME_LUXURY_PRICE   = 2000;
// Rentals (24h)
public static final int RENTAL_CYBERPUNK_PRICE = 80;
public static final int RENTAL_LUXURY_PRICE    = 120;
public static final int RENTAL_DIAMOND_PRICE   = 50;
public static final int RENTAL_SULTAN_PRICE    = 80;
public static final int RENTAL_DRAGON_PRICE    = 60;
```

---

## Build & Commit

```
./gradlew compileFossDebugJavaWithJavac
./gradlew :app:test
git add app/
git commit -m "feat: overhaul coin shop with rarity system, 24 items, unlock requirements, rental category"
git push origin master
```

---

## Rules

1. Do NOT modify anything under `game-core/`
2. Do NOT add any external dependencies or libraries
3. Do NOT change existing drawable resource names — reuse `@drawable/bg_pause_dialog`, `@drawable/bg_pause_button`, `@drawable/neuro_primary_button`, `@drawable/neuro_secondary_button`
4. `ShopAdapter.isItemEquipped()` and `equipItem()` are already correct — do not break them
5. The `filterItems(ShopItem.Category.RENTAL)` in CoinShopActivity filters by `RENTAL` category — this is already handled if you add the tab correctly
6. Rental items must show "اجاره‌ای ۲۴ ساعت" in the button, not the normal price format
