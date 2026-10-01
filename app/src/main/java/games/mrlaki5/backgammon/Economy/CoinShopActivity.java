package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.Menus.MenuActivity;
import games.mrlaki5.backgammon.Monetization.ads.AdCallback;
import games.mrlaki5.backgammon.Monetization.ads.AdManager;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdPlacement;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdTracker;
import games.mrlaki5.backgammon.Monetization.ads.RewardedAdUiHelper;
import games.mrlaki5.backgammon.R;

/**
 * Activity for the in-game Coin Shop offering Avatar Frames, Dice Skins, Titles, Themes,
 * Consumables, and Bundles using a 2-column RecyclerView with category tabs.
 */
public class CoinShopActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    private CoinManager coinManager;
    private PlayerProfileManager profileManager;
    private RewardedAdTracker rewardedAdTracker;
    private TextView tvCoinBalance;
    private RecyclerView rvShopItems;
    private ShopAdapter shopAdapter;
    private ShopItem.Category selectedCategory = ShopItem.Category.ALL;
    private SeasonManager seasonManager;
    private Button btnFreeCoinsShop;

    private final List<ShopItem> allItems = new ArrayList<>();

    private final android.os.Handler freeCoinsTickHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable freeCoinsTick = new Runnable() {
        @Override
        public void run() {
            refreshFreeCoinsButton();
            freeCoinsTickHandler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_coin_shop);

        coinManager = new CoinManager(this);
        profileManager = PlayerProfileManager.getInstance(this);
        rewardedAdTracker = new RewardedAdTracker(this);

        tvCoinBalance = findViewById(R.id.tvShopCoinBalance);
        rvShopItems = findViewById(R.id.shopRecyclerView);

        ImageButton btnBack = findViewById(R.id.btnShopBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        buildShopCatalog();
        setupRecyclerView();
        setupCategoryTabs();

        View treasury = findViewById(R.id.btnOpenCollection);
        if (treasury != null) {
            treasury.setOnClickListener(v ->
                    startActivity(new android.content.Intent(this, CollectionActivity.class)));
        }
        setupFreeCoinsButton();
        refreshCoinBalance();
    }

    private void setupFreeCoinsButton() {
        btnFreeCoinsShop = findViewById(R.id.btnFreeCoinsShop);
        if (btnFreeCoinsShop == null) return;

        refreshFreeCoinsButton();

        btnFreeCoinsShop.setOnClickListener(v -> {
            if (rewardedAdTracker.isDailyLimitReached(RewardedAdPlacement.FREE_COINS)) {
                Toast.makeText(this, R.string.ad_daily_limit_reached, Toast.LENGTH_SHORT).show();
                return;
            }
            if (rewardedAdTracker.getCooldownRemainingSeconds(RewardedAdPlacement.FREE_COINS) > 0) {
                refreshFreeCoinsButton();
                return;
            }

            AdManager adManager = MenuActivity.getSharedAdManager();
            if (adManager == null || !adManager.isRewardedAdReady()) {
                Toast.makeText(this, R.string.ad_not_ready, Toast.LENGTH_SHORT).show();
                if (adManager != null) adManager.preloadAds();
                return;
            }

            adManager.showRewardedAd(this, RewardedAdPlacement.FREE_COINS, new AdCallback() {
                @Override public void onAdLoaded() {}
                @Override public void onAdFailedToLoad(String error) {
                    runOnUiThread(() -> Toast.makeText(CoinShopActivity.this,
                            R.string.ad_not_ready, Toast.LENGTH_SHORT).show());
                }
                @Override public void onAdShown() {}
                @Override public void onAdDismissed() {}
                @Override public void onAdClicked() {}
                @Override public void onRewardEarned() {
                    runOnUiThread(() -> {
                        coinManager.earn(CoinConfig.REWARDED_AD_WATCH, "free_coins_shop");
                        rewardedAdTracker.recordShow(RewardedAdPlacement.FREE_COINS);
                        refreshCoinBalance();
                        refreshFreeCoinsButton();
                        RewardedAdUiHelper.showRewardDialog(CoinShopActivity.this,
                                CoinConfig.REWARDED_AD_WATCH, () -> {
                                    refreshCoinBalance();
                                    refreshFreeCoinsButton();
                                });
                    });
                }
            });
        });
    }

    private void refreshFreeCoinsButton() {
        if (btnFreeCoinsShop == null || rewardedAdTracker == null) return;
        RewardedAdUiHelper.refreshButton(this, btnFreeCoinsShop, rewardedAdTracker,
                RewardedAdPlacement.FREE_COINS, CoinConfig.REWARDED_AD_WATCH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindSeason();
        bindCollection();
        freeCoinsTickHandler.removeCallbacks(freeCoinsTick);
        freeCoinsTickHandler.post(freeCoinsTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        freeCoinsTickHandler.removeCallbacks(freeCoinsTick);
    }

    private void refreshCoinBalance() {
        if (tvCoinBalance != null && profileManager != null) {
            tvCoinBalance.setText(String.valueOf(profileManager.getBalance()));
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (shopAdapter != null) {
                    ShopItem item = shopAdapter.getItem(position);
                    if (item != null && item.getCategory() == ShopItem.Category.BUNDLE) {
                        return 2;
                    }
                }
                return 1;
            }
        });
        rvShopItems.setLayoutManager(glm);

        shopAdapter = new ShopAdapter(this, allItems, profileManager, coinManager, this::refreshCoinBalance);
        rvShopItems.setAdapter(shopAdapter);
    }

    private void setupCategoryTabs() {
        LinearLayout tabs = findViewById(R.id.shopCategoryTabs);
        if (tabs == null) return;
        tabs.removeAllViews();

        ShopItem.Category[] cats = {
                ShopItem.Category.ALL,
                ShopItem.Category.CHECKERS,
                ShopItem.Category.DICE_SKIN,
                ShopItem.Category.TITLE,
                ShopItem.Category.COSMETIC,
                ShopItem.Category.CONSUMABLE,
                ShopItem.Category.BUNDLE
        };
        int[] labelRes = {
                R.string.shop_category_all,
                R.string.shop_category_checkers,
                R.string.shop_category_dice,
                R.string.shop_category_title,
                R.string.shop_category_cosmetic,
                R.string.shop_category_consumable,
                R.string.shop_category_bundle
        };

        for (int i = 0; i < cats.length; i++) {
            final ShopItem.Category cat = cats[i];
            Button btn = new Button(new ContextThemeWrapper(this, R.style.ShopCategoryTabButton), null, 0);
            btn.setText(getString(labelRes[i]));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMarginEnd(4);
            btn.setLayoutParams(lp);
            btn.setPadding(dp(18), 0, dp(18), 0);
            btn.setGravity(android.view.Gravity.CENTER);
            btn.setSelected(i == 0);
            applyTabAppearance(btn, i == 0);

            btn.setOnClickListener(v -> {
                selectedCategory = cat;
                if (shopAdapter != null) {
                    shopAdapter.filter(cat);
                }
                updateTabSelection(tabs, btn);
                bindCollection();
            });
            tabs.addView(btn);
        }
    }

    /**
     * How much of the current shelf the player already holds.
     *
     * A list of prices is a catalogue; a count of what you own turns the same
     * list into a set worth completing, which is the whole point of collections.
     */
    /**
     * The season's set: what it is, what it costs, and how long it is still here.
     *
     * Nothing else in the shop can be missed, which is exactly why this one can.
     */
    private void bindSeason() {
        View card = findViewById(R.id.seasonBundle);
        if (card == null) {
            return;
        }
        if (seasonManager == null) {
            seasonManager = new SeasonManager(this);
        }
        Season season = seasonManager.current();

        // The card is a summary; the set has a page of its own, and tapping
        // anywhere on the card is how a player gets to it.
        card.setOnClickListener(v -> BundleActivity.open(this));

        ((android.widget.ImageView) findViewById(R.id.seasonHero))
                .setImageResource(season.heroDrawable());
        ((android.widget.ImageView) findViewById(R.id.seasonSeal))
                .setImageResource(season.sealDrawable());
        ((android.widget.TextView) findViewById(R.id.seasonName)).setText(season.nameRes);
        ((android.widget.TextView) findViewById(R.id.seasonStory)).setText(season.storyRes);

        int remaining = seasonManager.daysRemaining();
        ((android.widget.TextView) findViewById(R.id.seasonCountdown)).setText(
                remaining <= 1
                        ? getString(R.string.season_last_day)
                        : getString(R.string.season_days_left, remaining));

        // The pieces of the set, as their own artwork.
        LinearLayout contents = findViewById(R.id.seasonContents);
        contents.removeAllViews();
        for (String id : season.itemIds()) {
            ShopItem piece = findItem(id);
            if (piece == null) {
                continue;
            }
            android.widget.ImageView art = new android.widget.ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(38), dp(38));
            lp.setMarginEnd(dp(8));
            art.setLayoutParams(lp);
            art.setImageResource(piece.getIconRes() != 0
                    ? piece.getIconRes()
                    : ShopArt.medallion(piece.getCategory(), piece.getRarity()));
            contents.addView(art);
        }

        android.widget.TextView full = findViewById(R.id.seasonFullPrice);
        Button buy = findViewById(R.id.seasonBuy);
        int separately = seasonManager.bundleFullPrice(allItems);

        if (seasonManager.ownsWholeBundle()) {
            full.setText("");
            buy.setText(R.string.season_bundle_owned);
            buy.setEnabled(false);
        } else {
            full.setText(getString(R.string.season_bundle_saving, separately));
            buy.setText(getString(R.string.season_bundle_price, season.bundlePrice));
            buy.setEnabled(true);
            buy.setOnClickListener(v -> {
                if (!coinManager.spend(season.bundlePrice, "season_bundle")) {
                    android.widget.Toast.makeText(this, R.string.not_enough_coins,
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                seasonManager.grantBundle();
                refreshCoinBalance();
                bindSeason();
                bindCollection();
                if (shopAdapter != null) {
                    shopAdapter.notifyDataSetChanged();
                }
            });
        }
        seasonManager.markSeen();
    }

    /**
     * Takes last season's set off the shelves.
     *
     * Anything the player already bought stays in the list so they can still see
     * and equip it; anything they did not is gone, and does not come back. That
     * is what makes the seal on a retired piece worth anything.
     */
    private void withdrawClosedSeasons() {
        if (seasonManager == null) {
            seasonManager = new SeasonManager(this);
        }
        games.mrlaki5.backgammon.Database.PlayerProfileManager profile =
                games.mrlaki5.backgammon.Database.PlayerProfileManager.getInstance(this);

        java.util.Iterator<ShopItem> iterator = allItems.iterator();
        while (iterator.hasNext()) {
            ShopItem item = iterator.next();
            if (!seasonManager.isOffered(item.getId())
                    && !profile.isItemPurchased(item.getId())) {
                iterator.remove();
            }
        }
    }

    private ShopItem findItem(String id) {
        for (ShopItem item : allItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private void bindCollection() {
        android.widget.TextView label = findViewById(R.id.tvShopCollectionLabel);
        android.widget.TextView count = findViewById(R.id.tvShopCollectionCount);
        android.widget.ProgressBar bar = findViewById(R.id.shopCollectionProgress);
        if (label == null || count == null || bar == null || allItems == null) {
            return;
        }

        games.mrlaki5.backgammon.Database.PlayerProfileManager profile =
                games.mrlaki5.backgammon.Database.PlayerProfileManager.getInstance(this);

        int total = 0;
        int owned = 0;
        for (ShopItem item : allItems) {
            if (item.isConsumable()) {
                continue;  // stock, not something you collect
            }
            if (selectedCategory != ShopItem.Category.ALL
                    && item.getCategory() != selectedCategory) {
                continue;
            }
            total++;
            if (item.isFree() || profile.isItemPurchased(item.getId())) {
                owned++;
            }
        }

        label.setText(getString(R.string.shop_collection_label));
        count.setText(getString(R.string.shop_collection_count, owned, total));
        bar.setProgress(total == 0 ? 0 : Math.round(owned * 100F / total));
        View container = findViewById(R.id.shopCollectionBar);
        if (container != null) {
            container.setVisibility(total == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void applyTabAppearance(Button btn, boolean selected) {
        btn.setBackgroundResource(selected ? R.drawable.bg_shop_tab_selected : R.drawable.bg_shop_tab_unselected);
        btn.setTextColor(selected ? 0xFF1A1400 : 0xFFCFE3EE);
    }

    private void updateTabSelection(LinearLayout tabs, Button selected) {
        for (int i = 0; i < tabs.getChildCount(); i++) {
            View v = tabs.getChildAt(i);
            boolean isSel = (v == selected);
            v.setSelected(isSel);
            if (v instanceof Button) {
                applyTabAppearance((Button) v, isSel);
            }
        }
    }

    private void buildShopCatalog() {
        allItems.clear();

        // ═══════════════════════════════════════════
        // BUNDLES — each spans 2 columns in the grid
        // ═══════════════════════════════════════════
        allItems.add(ShopItem.starterBundle(this));

        // Nacre — checkers + dice + effect (2650 → 1900)
        allItems.add(ShopItem.themedBundle(
                "bundle_nacre",
                getString(R.string.bundle_nacre_title),
                getString(R.string.bundle_nacre_desc),
                getString(R.string.bundle_nacre_story),
                1900, ShopItem.Rarity.RARE,
                R.drawable.checkers_nacre,
                null,
                new String[]{"checkers_nacre", "dice_walnut", "effect_pearl_ripple"}));

        // Copper — checkers + dice + effect (2900 → 2100)
        allItems.add(ShopItem.themedBundle(
                "bundle_copper",
                getString(R.string.bundle_copper_title),
                getString(R.string.bundle_copper_desc),
                getString(R.string.bundle_copper_story),
                2100, ShopItem.Rarity.RARE,
                R.drawable.checkers_copper,
                getString(R.string.shop_badge_new),
                new String[]{"checkers_copper", "dice_copper", "effect_madder_thread"}));

        // Turquoise — checkers + dice + effect (5400 → 2800)
        allItems.add(ShopItem.themedBundle(
                "bundle_turquoise",
                getString(R.string.bundle_turquoise_title),
                getString(R.string.bundle_turquoise_desc),
                getString(R.string.bundle_turquoise_story),
                2800, ShopItem.Rarity.EPIC,
                R.drawable.checkers_turquoise,
                getString(R.string.shop_badge_discount),
                new String[]{"checkers_turquoise", "dice_turquoise", "effect_turquoise_spark"}));

        // Lapis — checkers + dice + effect (5700 → 3500)
        allItems.add(ShopItem.themedBundle(
                "bundle_lapis",
                getString(R.string.bundle_lapis_title),
                getString(R.string.bundle_lapis_desc),
                getString(R.string.bundle_lapis_story),
                3500, ShopItem.Rarity.EPIC,
                R.drawable.checkers_lajvard,
                null,
                new String[]{"checkers_lajvard", "dice_lajvard", "effect_lapis_night"}));

        // Saffron — checkers + dice + effect (6300 → 3800)
        allItems.add(ShopItem.themedBundle(
                "bundle_saffron",
                getString(R.string.bundle_saffron_title),
                getString(R.string.bundle_saffron_desc),
                getString(R.string.bundle_saffron_story),
                3800, ShopItem.Rarity.EPIC,
                R.drawable.checkers_saffron,
                null,
                new String[]{"checkers_saffron", "dice_saffron", "effect_saffron_haze"}));

        // Agate — checkers + dice + effect (6900 → 4400)
        allItems.add(ShopItem.themedBundle(
                "bundle_agate",
                getString(R.string.bundle_agate_title),
                getString(R.string.bundle_agate_desc),
                getString(R.string.bundle_agate_story),
                4400, ShopItem.Rarity.EPIC,
                R.drawable.checkers_agate,
                getString(R.string.shop_badge_popular),
                new String[]{"checkers_agate", "dice_agate", "effect_agate_ember"}));

        // Isfahan — board + frame + sound (5500 → 4000)
        allItems.add(ShopItem.themedBundle(
                "bundle_isfahan",
                getString(R.string.bundle_isfahan_title),
                getString(R.string.bundle_isfahan_desc),
                getString(R.string.bundle_isfahan_story),
                4000, ShopItem.Rarity.EPIC,
                R.drawable.board_haftrang,
                getString(R.string.shop_badge_popular),
                new String[]{"board_haftrang", "frame_isfahan", "sound_isfahan"}));

        // Caravanserai — board + frame + sound (5600 → 4200)
        allItems.add(ShopItem.themedBundle(
                "bundle_caravan",
                getString(R.string.bundle_caravan_title),
                getString(R.string.bundle_caravan_desc),
                getString(R.string.bundle_caravan_story),
                4200, ShopItem.Rarity.EPIC,
                R.drawable.board_pateh,
                getString(R.string.shop_badge_new),
                new String[]{"board_pateh", "frame_caravan", "sound_caravan"}));

        // Sound Market — 4 sound sets (4450 → 3000)
        allItems.add(ShopItem.themedBundle(
                "bundle_sound_pack",
                getString(R.string.bundle_sound_pack_title),
                getString(R.string.bundle_sound_pack_desc),
                getString(R.string.bundle_sound_pack_story),
                3000, ShopItem.Rarity.RARE,
                R.drawable.sound_isfahan,
                getString(R.string.shop_badge_discount),
                new String[]{"sound_neyshabur", "sound_shiraz", "sound_tabriz", "sound_yazd"}));

        // Artisan Boards — board_khatam + board_mina + board_nacre (9800 → 6800)
        allItems.add(ShopItem.themedBundle(
                "bundle_boards",
                getString(R.string.bundle_boards_title),
                getString(R.string.bundle_boards_desc),
                getString(R.string.bundle_boards_story),
                6800, ShopItem.Rarity.EPIC,
                R.drawable.board_khatam,
                null,
                new String[]{"board_khatam", "board_mina", "board_nacre"}));

        // Yazd Zari — board + frame + sound (5950 → 4500)
        allItems.add(ShopItem.themedBundle(
                "bundle_zari",
                getString(R.string.bundle_zari_title),
                getString(R.string.bundle_zari_desc),
                getString(R.string.bundle_zari_story),
                4500, ShopItem.Rarity.EPIC,
                R.drawable.board_zari,
                getString(R.string.shop_badge_special),
                new String[]{"board_zari", "frame_yazd", "sound_yazd"}));

        // Gold — checkers + dice (11000 → 7500)
        allItems.add(ShopItem.themedBundle(
                "bundle_gold",
                getString(R.string.bundle_gold_title),
                getString(R.string.bundle_gold_desc),
                getString(R.string.bundle_gold_story),
                7500, ShopItem.Rarity.LEGENDARY,
                R.drawable.checkers_gold,
                getString(R.string.shop_badge_special),
                new String[]{"checkers_gold", "dice_gold"}));

        // Tabriz Master — board_melileh + checkers_melileh + dice_melileh (9800 → 6500)
        allItems.add(ShopItem.themedBundle(
                "bundle_master",
                getString(R.string.bundle_master_title),
                getString(R.string.bundle_master_desc),
                getString(R.string.bundle_master_story),
                6500, ShopItem.Rarity.LEGENDARY,
                R.drawable.board_melileh,
                getString(R.string.shop_badge_special),
                new String[]{"board_melileh", "checkers_melileh", "dice_melileh"}));

        // ═══════════════════════════════════════════
        // CONSUMABLES — Hints & Undos
        // ═══════════════════════════════════════════
        allItems.add(ShopItem.hintPack3(this));
        allItems.add(ShopItem.hintPack10(this));
        allItems.add(ShopItem.undoPack3(this));

        // Checkers, dice and titles share one definition with the treasury
        // screen, because a season names its pieces by id and both screens have
        // to agree on what those ids mean.
        allItems.addAll(SeasonCatalogue.all(this));

        withdrawClosedSeasons();
    }
}
