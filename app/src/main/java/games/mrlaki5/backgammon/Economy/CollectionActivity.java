package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.LocaleHelper;
import games.mrlaki5.backgammon.R;

/**
 * The treasury.
 *
 * The shop answers "what can I have"; this answers "what did I have the chance
 * at", which is the half that gives a collection its weight. Every season is
 * listed in order with its whole set, and each piece is in one of three states:
 * held, still on sale, or closed before it was bought. The third is the point —
 * a set you can no longer get is what makes the one you did get worth owning,
 * and a collection screen that only shows your wins is a trophy case, not a
 * record.
 */
public class CollectionActivity extends AppCompatActivity {

    private SeasonManager seasonManager;
    private PlayerProfileManager profile;
    private final List<ShopItem> catalogue = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySelectedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_collection);

        seasonManager = new SeasonManager(this);
        profile = PlayerProfileManager.getInstance(this);

        View back = findViewById(R.id.btnCollectionBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        catalogue.clear();
        catalogue.addAll(SeasonCatalogue.all(this));
        bind();
    }

    private void bind() {
        LinearLayout list = findViewById(R.id.collectionSeasons);
        if (list == null) return;
        list.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        Season active = seasonManager.current();
        int held = 0;
        int total = 0;

        for (Season season : Season.values()) {
            String[] ids = season.itemIds();
            total += ids.length;
            for (String id : ids) {
                if (profile.isItemPurchased(id)) {
                    held++;
                }
            }
            list.addView(seasonSection(inflater, list, season, season == active));
        }

        TextView summary = findViewById(R.id.collectionTotal);
        if (summary != null) {
            summary.setText(getString(R.string.collection_total, held, total));
        }
    }

    private View seasonSection(LayoutInflater inflater, ViewGroup parent,
                               Season season, boolean isActive) {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sectionParams.bottomMargin = (int) (18 * density);
        section.setLayoutParams(sectionParams);

        // Season heading: seal, name, and whether it is open or closed.
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView seal = new ImageView(this);
        seal.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (34 * density), (int) (34 * density)));
        seal.setImageResource(season.sealDrawable());
        if (!isActive) {
            seal.setColorFilter(drained());
        }
        heading.addView(seal);

        TextView name = new TextView(this, null, 0, R.style.RoyalText_Display);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.setMarginStart((int) (10 * density));
        name.setLayoutParams(nameParams);
        name.setTextSize(15f);
        name.setText(season.nameRes);
        heading.addView(name);

        TextView state = new TextView(this, null, 0, R.style.RoyalText_Chip);
        state.setText(isActive
                ? getString(R.string.collection_season_open, seasonManager.daysRemaining())
                : getString(R.string.collection_season_closed));
        state.setBackgroundResource(isActive
                ? R.drawable.bg_chip_turquoise : R.drawable.bg_chip_royal);
        state.setTextColor(getResources().getColor(
                isActive ? R.color.turquoise : R.color.ink_muted));
        state.setTextSize(10f);
        heading.addView(state);

        section.addView(heading);

        // The set itself.
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scrollParams.topMargin = (int) (8 * density);
        scroll.setLayoutParams(scrollParams);
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (String id : season.itemIds()) {
            row.addView(pieceTile(inflater, row, season, id, isActive));
        }
        scroll.addView(row);
        section.addView(scroll);
        return section;
    }

    private View pieceTile(LayoutInflater inflater, ViewGroup parent,
                           Season season, String itemId, boolean seasonOpen) {
        View tile = inflater.inflate(R.layout.item_collection_piece, parent, false);
        ShopItem item = find(itemId);
        boolean owned = profile.isItemPurchased(itemId);

        ImageView art = tile.findViewById(R.id.pieceArt);
        ImageView seal = tile.findViewById(R.id.pieceSeal);
        ImageView lock = tile.findViewById(R.id.pieceLock);
        TextView name = tile.findViewById(R.id.pieceName);
        TextView state = tile.findViewById(R.id.pieceState);

        if (item != null) {
            art.setImageResource(item.getIconRes() != 0
                    ? item.getIconRes()
                    : ShopArt.medallion(item.getCategory(), item.getRarity()));
            name.setText(item.getTitle());
            tile.setBackgroundResource(ShopArt.card(item.getRarity()));
        }

        if (owned) {
            seal.setImageResource(season.sealDrawable());
            seal.setVisibility(View.VISIBLE);
            state.setText(R.string.collection_piece_held);
            state.setTextColor(getResources().getColor(R.color.turquoise));
        } else if (item != null && item.isEarnOnly()) {
            // A banner has no price whether the season is open or shut. Showing
            // one here, even a struck-through one, would suggest it was ever
            // for sale — and the fact that it never was is the point of it.
            state.setText(R.string.detail_earn_only);
            state.setTextColor(getResources().getColor(R.color.gold_bright));
            if (!seasonOpen) {
                art.setColorFilter(drained());
                art.setAlpha(0.55f);
                lock.setVisibility(View.VISIBLE);
                state.setTextColor(getResources().getColor(R.color.ink_muted));
                name.setTextColor(getResources().getColor(R.color.ink_muted));
            }
        } else if (seasonOpen) {
            state.setText(item == null
                    ? "" : getString(R.string.collection_piece_price, item.getPrice()));
            state.setTextColor(getResources().getColor(R.color.gold_bright));
        } else {
            // Closed and never held. Shown, not hidden — that is the record.
            art.setColorFilter(drained());
            art.setAlpha(0.55f);
            lock.setVisibility(View.VISIBLE);
            state.setText(R.string.collection_piece_missed);
            state.setTextColor(getResources().getColor(R.color.ink_muted));
            name.setTextColor(getResources().getColor(R.color.ink_muted));
        }
        return tile;
    }

    private ShopItem find(String id) {
        for (ShopItem item : catalogue) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Colour drained out, relief kept — a closed season is cold, not flat. */
    private static ColorMatrixColorFilter drained() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.12f);
        return new ColorMatrixColorFilter(matrix);
    }
}
