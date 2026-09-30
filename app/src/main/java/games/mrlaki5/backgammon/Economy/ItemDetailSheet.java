package games.mrlaki5.backgammon.Economy;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.R;

/**
 * A single piece, shown at the size it was drawn.
 *
 * A shop card has room for a name, a price and about four words, which is
 * enough to browse a list and not nearly enough to want any particular thing in
 * it. This is where a piece gets to be an object: the art large, the same art
 * standing on the board of its own season, what the craft is, and one honest
 * line about how it is come by — including, for a banner, that it is not for
 * sale and no amount of coins will change that.
 */
public final class ItemDetailSheet {

    private ItemDetailSheet() {}

    public interface OnChanged {
        void changed();
    }

    public static void show(Activity activity, ShopItem item, Season season, OnChanged onChanged) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sheet_item_detail);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        PlayerProfileManager profile = PlayerProfileManager.getInstance(activity);
        CoinManager coins = new CoinManager(activity);

        ImageView art = dialog.findViewById(R.id.detailArt);
        ImageView board = dialog.findViewById(R.id.detailBoard);
        art.setImageResource(item.getIconRes() != 0 ? item.getIconRes() : season.sealDrawable());

        // A board previews on its own; everything else previews standing on the
        // board of the season it belongs to, which is the whole argument for a set.
        if (item.getCategory() == ShopItem.Category.BOARD) {
            board.setVisibility(View.GONE);
        } else {
            board.setVisibility(View.VISIBLE);
            board.setImageResource(boardOf(season));
        }

        TextView rarity = dialog.findViewById(R.id.detailRarity);
        rarity.setText(item.getRarity().label(activity));
        rarity.setTextColor(Color.parseColor(item.getRarity().hexColor()));

        ((TextView) dialog.findViewById(R.id.detailTitle)).setText(item.getTitle());
        ((TextView) dialog.findViewById(R.id.detailDesc)).setText(item.getDescription());

        TextView story = dialog.findViewById(R.id.detailStory);
        story.setText(item.getStory().isEmpty() ? item.getDescription() : item.getStory());

        TextView how = dialog.findViewById(R.id.detailHow);
        Button action = dialog.findViewById(R.id.btnDetailAction);
        boolean held = profile.isItemPurchased(item.getId());

        if (held) {
            how.setText(R.string.detail_held);
            action.setText(R.string.detail_held);
            action.setEnabled(false);
        } else if (item.isEarnOnly()) {
            how.setText(R.string.detail_earn_only);
            action.setText(R.string.detail_earn_only);
            action.setEnabled(false);
        } else {
            how.setText(item.hasUnlockReq()
                    ? activity.getString(R.string.detail_buy_or_earn)
                    : activity.getString(R.string.detail_how));
            action.setText(String.valueOf(item.getPrice()));
            action.setEnabled(true);
            action.setOnClickListener(v -> {
                if (!coins.spend(item.getPrice(), item.getId())) {
                    Toast.makeText(activity, R.string.not_enough_coins,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                profile.addPurchasedItem(item.getId());
                dialog.dismiss();
                if (onChanged != null) {
                    onChanged.changed();
                }
            });
        }

        dialog.findViewById(R.id.btnDetailClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static int boardOf(Season season) {
        switch (season) {
            case NEYSHABUR: return R.drawable.board_mina;
            case HARBOUR:   return R.drawable.board_nacre;
            case CARAVAN:   return R.drawable.board_monabbat;
            case ISFAHAN:
            default:        return R.drawable.board_khatam;
        }
    }
}
