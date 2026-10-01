package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.R;

/**
 * One season's set, on a page of its own.
 *
 * The shop used to sell a season as a row of small cards with a combined price
 * printed under them, which tells a player what they would be charged but not
 * what they would be getting. This page leads with the set standing in its
 * niche at the size the art was drawn for, lists the seven pieces with their
 * own artwork rather than seven identical glyphs, and states the two numbers
 * that actually decide a purchase: what the pieces cost one at a time, and what
 * they cost together.
 *
 * The banner is in the list and has no price, on purpose. A set where every
 * piece can be bought is a set nobody earned.
 */
public class BundleActivity extends AppCompatActivity {

    private SeasonManager seasonManager;
    private PlayerProfileManager profile;
    private CoinManager coins;
    private Season season;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bundle);

        seasonManager = new SeasonManager(this);
        profile = PlayerProfileManager.getInstance(this);
        coins = new CoinManager(this);
        season = seasonManager.current();

        ImageButton back = findViewById(R.id.btnBundleBack);
        back.setOnClickListener(v -> finish());

        ((ImageView) findViewById(R.id.bundleHero)).setImageResource(season.heroDrawable());
        ((TextView) findViewById(R.id.bundleSeasonName)).setText(season.nameRes);
        ((TextView) findViewById(R.id.bundleStory)).setText(season.storyRes);
        ((TextView) findViewById(R.id.bundleDaysLeft)).setText(
                getString(R.string.bundle_days_left, seasonManager.daysRemaining()));

        render();
    }

    private void render() {
        ((TextView) findViewById(R.id.bundleCoins))
                .setText(String.valueOf(coins.getBalance()));

        List<ShopItem> set = SeasonCatalogue.ofSeason(this, season);
        LinearLayout list = findViewById(R.id.bundleItems);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ShopItem item : set) {
            View row = inflater.inflate(R.layout.item_bundle_row, list, false);
            ((ImageView) row.findViewById(R.id.rowArt)).setImageResource(
                    item.getIconRes() != 0 ? item.getIconRes() : season.sealDrawable());
            ((TextView) row.findViewById(R.id.rowTitle)).setText(item.getTitle());

            TextView rarity = row.findViewById(R.id.rowRarity);
            rarity.setText(item.getRarity().label(this));
            rarity.setTextColor(android.graphics.Color.parseColor(item.getRarity().hexColor()));

            TextView state = row.findViewById(R.id.rowState);
            if (profile.isItemPurchased(item.getId())) {
                state.setText(R.string.detail_held);
                state.setTextColor(getResources().getColor(R.color.turquoise));
            } else if (item.isEarnOnly()) {
                state.setText(R.string.detail_earn_only);
                state.setTextColor(getResources().getColor(R.color.gold));
            } else {
                state.setText(String.valueOf(item.getPrice()));
                state.setTextColor(getResources().getColor(R.color.ink_parchment));
            }

            row.setOnClickListener(v -> ItemDetailSheet.show(this, item, season, this::render));
            list.addView(row);
        }

        int separately = SeasonCatalogue.separatePrice(this, season);
        int setPrice = season.bundlePrice;
        ((TextView) findViewById(R.id.bundleSeparately))
                .setText(getString(R.string.bundle_separately, separately));
        ((TextView) findViewById(R.id.bundleSetPrice))
                .setText(getString(R.string.bundle_set_price, setPrice));
        ((TextView) findViewById(R.id.bundleSaving))
                .setText(getString(R.string.bundle_saving, Math.max(0, separately - setPrice)));

        Button buy = findViewById(R.id.btnBundleBuy);
        if (seasonManager.ownsWholeBundle()) {
            buy.setText(R.string.bundle_owned);
            buy.setEnabled(false);
        } else {
            int held = seasonManager.bundleOwnedCount();
            int total = seasonManager.purchasableCount();
            buy.setText(held > 0
                    ? getString(R.string.bundle_partial, held, total)
                    : getString(R.string.bundle_buy_set));
            buy.setEnabled(true);
            buy.setOnClickListener(v -> buySet(setPrice));
        }
    }

    private void buySet(int price) {
        if (!coins.spend(price, "bundle_" + season.number)) {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
            return;
        }
        seasonManager.grantBundle();
        render();
    }

    public static void open(Context context) {
        context.startActivity(new Intent(context, BundleActivity.class));
    }
}
