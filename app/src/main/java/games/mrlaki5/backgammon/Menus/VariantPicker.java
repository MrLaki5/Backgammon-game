package games.mrlaki5.backgammon.Menus;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.royalbackgammon.core.variant.Variant;

import games.mrlaki5.backgammon.GamePreferences;
import games.mrlaki5.backgammon.R;

/**
 * Binds include_variant_picker: the variant buttons (plus the Tavli rotation), the match-length
 * cycler and the rule hint.
 */
public final class VariantPicker {

    private static final Variant[] VARIANTS = {
            Variant.STANDARD, Variant.TAVLA, Variant.PORTES,
            Variant.PLAKOTO, Variant.FEVGA, Variant.NARDY, Variant.ACEY_DEUCEY
    };
    private static final int[] BUTTON_IDS = {
            R.id.variantStandard, R.id.variantTavla, R.id.variantPortes,
            R.id.variantPlakoto, R.id.variantFevga, R.id.variantNardy, R.id.variantAcey
    };
    private static final int[] MATCH_LENGTHS = {1, 3, 5, 7};
    private static final int[] TAVLI_MATCH_LENGTHS = {5, 7};

    private final Context context;
    private final Button[] variantButtons = new Button[VARIANTS.length];
    private final Button tavliButton;
    private final Button matchButton;
    private final TextView hint;
    private Variant variant;
    private boolean tavli;
    private int matchLength;

    public VariantPicker(View root) {
        context = root.getContext();
        variant = GamePreferences.getVariant(context);
        tavli = GamePreferences.isTavliMatch(context);
        matchLength = GamePreferences.getMatchLength(context);

        for (int i = 0; i < VARIANTS.length; i++) {
            final Variant v = VARIANTS[i];
            variantButtons[i] = root.findViewById(BUTTON_IDS[i]);
            variantButtons[i].setOnClickListener(btn -> {
                variant = v;
                tavli = false;
                render();
            });
        }
        tavliButton = root.findViewById(R.id.variantTavli);
        tavliButton.setOnClickListener(btn -> {
            tavli = true;
            variant = Variant.TAVLI_ROTATION.get(0);
            if (matchLength < TAVLI_MATCH_LENGTHS[0]) {
                matchLength = TAVLI_MATCH_LENGTHS[0];
            }
            render();
        });
        matchButton = root.findViewById(R.id.matchLength);
        matchButton.setOnClickListener(btn -> {
            matchLength = next(tavli ? TAVLI_MATCH_LENGTHS : MATCH_LENGTHS, matchLength);
            render();
        });
        hint = root.findViewById(R.id.variantHint);
        render();
    }

    /** Persists the selection and adds the variant/match extras for GameActivity. */
    public void applyTo(Intent intent) {
        GamePreferences.saveVariantSelection(context, variant, matchLength, tavli);
        intent.putExtra(MenuActivity.EXTRA_VARIANT, variant.name());
        intent.putExtra(MenuActivity.EXTRA_MATCH_TARGET, matchLength);
        intent.putExtra(MenuActivity.EXTRA_TAVLI, tavli);
    }

    private void render() {
        for (int i = 0; i < VARIANTS.length; i++) {
            paint(variantButtons[i], !tavli && VARIANTS[i] == variant);
        }
        paint(tavliButton, tavli);
        matchButton.setText(matchLabel(context, matchLength));
        paint(matchButton, matchLength > 1);
        hint.setText(tavli ? R.string.variant_tavli_rules : ruleSummaryRes(variant));
    }

    private static void paint(Button button, boolean selected) {
        button.setBackgroundResource(selected
                ? R.drawable.bg_difficulty_selected : R.drawable.bg_difficulty_normal);
        button.setTextColor(selected ? 0xFFE6A100 : 0xFF7A8A99);
    }

    private static int next(int[] values, int current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    public static String matchLabel(Context context, int length) {
        return length <= 1
                ? context.getString(R.string.match_single)
                : context.getString(R.string.match_to_points, length);
    }

    public static int nameRes(Variant variant) {
        switch (variant) {
            case TAVLA: return R.string.variant_tavla;
            case PORTES: return R.string.variant_portes;
            case PLAKOTO: return R.string.variant_plakoto;
            case FEVGA: return R.string.variant_fevga;
            case NARDY: return R.string.variant_nardy;
            case ACEY_DEUCEY: return R.string.variant_acey;
            default: return R.string.variant_standard;
        }
    }

    private static int ruleSummaryRes(Variant variant) {
        switch (variant) {
            case TAVLA: return R.string.variant_tavla_rules;
            case PORTES: return R.string.variant_portes_rules;
            case PLAKOTO: return R.string.variant_plakoto_rules;
            case FEVGA: return R.string.variant_fevga_rules;
            case NARDY: return R.string.variant_nardy_rules;
            case ACEY_DEUCEY: return R.string.variant_acey_rules;
            default: return R.string.variant_standard_rules;
        }
    }
}
