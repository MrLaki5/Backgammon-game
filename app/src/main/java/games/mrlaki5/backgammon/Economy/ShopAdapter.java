package games.mrlaki5.backgammon.Economy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.R;

/**
 * RecyclerView adapter for rendering shop items with buy/equip state handling,
 * consumable charges, starter bundles, unlock progress, and category filtering.
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onItemAction();
    }

    private final Context context;
    private final List<ShopItem> allItems;
    private final List<ShopItem> displayedItems;
    private final PlayerProfileManager profileManager;
    private final CoinManager coinManager;
    private final OnItemActionListener actionListener;
    private ShopItem.Category activeFilter = ShopItem.Category.ALL;

    public ShopAdapter(Context context, List<ShopItem> items,
                       PlayerProfileManager profileManager, CoinManager coinManager,
                       OnItemActionListener listener) {
        this.context = context;
        this.allItems = new ArrayList<>(items);
        this.displayedItems = new ArrayList<>(items);
        this.profileManager = profileManager;
        this.coinManager = coinManager;
        this.actionListener = listener;
    }

    public void setAllItems(List<ShopItem> items) {
        this.allItems.clear();
        this.allItems.addAll(items);
        filter(activeFilter);
    }

    public void filter(ShopItem.Category cat) {
        activeFilter = cat;
        displayedItems.clear();
        if (cat == null || cat == ShopItem.Category.ALL) {
            displayedItems.addAll(allItems);
        } else if (cat == ShopItem.Category.COSMETIC) {
            for (ShopItem item : allItems) {
                if (item.getCategory() == ShopItem.Category.AVATAR_FRAME ||
                    item.getCategory() == ShopItem.Category.DICE_SKIN ||
                    item.getCategory() == ShopItem.Category.TITLE ||
                    item.getCategory() == ShopItem.Category.THEME) {
                    displayedItems.add(item);
                }
            }
        } else {
            for (ShopItem item : allItems) {
                if (item.getCategory() == cat) {
                    displayedItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public ShopItem getItem(int position) {
        return displayedItems.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = displayedItems.get(position);

        // Tapping the card body opens the piece rather than buying it. A card
        // has room for a name and a price; wanting something takes more than
        // that, so the detail sheet is where the art, the craft and the way it
        // is come by actually get shown. The action button still buys.
        if (holder.itemView.getContext() instanceof android.app.Activity) {
            final android.app.Activity host = (android.app.Activity) holder.itemView.getContext();
            View body = holder.itemView.findViewById(R.id.shopCardBody);
            View target = body != null ? body : holder.itemView;
            target.setOnClickListener(v -> {
                Season season = Season.of(item.getId());
                if (season == null) {
                    season = new SeasonManager(context).current();
                }
                ItemDetailSheet.show(host, item, season, () -> {
                    notifyDataSetChanged();
                    if (actionListener != null) {
                        actionListener.onItemAction();
                    }
                });
            });
        }

        if (holder.ivLock != null) {
            holder.ivLock.setImageResource(R.drawable.ic_lock_theme);
        }

        // The product is a struck medallion in the metal its rarity earns. The
        // emoji this used to show rendered differently on every handset and
        // matched nothing else on the screen.
        holder.tvIcon.setText("");
        holder.tvIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        holder.tvIcon.setBackgroundResource(item.getIconRes() != 0
                ? item.getIconRes()
                : ShopArt.medallion(item.getCategory(), item.getRarity()));

        // The card is cast in that same metal, so the two are one object.
        holder.itemView.findViewById(R.id.shopCardBody)
                .setBackgroundResource(ShopArt.card(item.getRarity()));

        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());

        // Consumable quantity badge ("×3", "×10")
        if (item.isConsumable() && item.getQuantity() > 0 && holder.tvQuantityBadge != null) {
            holder.tvQuantityBadge.setVisibility(View.VISIBLE);
            holder.tvQuantityBadge.setText("×" + item.getQuantity());
        } else if (holder.tvQuantityBadge != null) {
            holder.tvQuantityBadge.setVisibility(View.GONE);
        }

        // Rarity styling
        int rarityColor = Color.parseColor(item.getRarity().hexColor());
        holder.tvRarity.setText(item.getRarity().label(context));
        holder.tvRarity.setTextColor(rarityColor);
        if (holder.rarityBar != null) {
            // The frame already carries the material, so the bar would only
            // repeat it; it stays for the older layouts that still use one.
            holder.rarityBar.setVisibility(View.GONE);
        }

        // Badge (NEW, HOT, etc.)
        if (holder.tvBadge != null) {
            if (item.getBadge() != null) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText(item.getBadge());
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }
        }

        // Unlock requirement & Win-progress ProgressBar
        int playerWins = profileManager.getTotalWins();
        boolean isLocked = item.hasUnlockReq() && playerWins < item.getUnlockRequirement();
        if (holder.layoutUnlock != null) {
            if (isLocked) {
                holder.layoutUnlock.setVisibility(View.VISIBLE);
                if (holder.tvUnlock != null) {
                    holder.tvUnlock.setVisibility(View.VISIBLE);
                    holder.tvUnlock.setText(context.getString(R.string.shop_locked_format, item.getUnlockRequirement()));
                }
            } else {
                holder.layoutUnlock.setVisibility(View.GONE);
            }
        }

        if (holder.unlockProgress != null) {
            if (item.getUnlockRequirement() > 0 && !profileManager.isItemPurchased(item.getId())) {
                int pct = Math.min(100, (int) (playerWins * 100f / item.getUnlockRequirement()));
                holder.unlockProgress.setVisibility(View.VISIBLE);
                holder.unlockProgress.setProgress(pct);
            } else {
                holder.unlockProgress.setVisibility(View.GONE);
            }
        }

        // Action / purchase / equip button binding
        if (item.isConsumable()) {
            holder.btnAction.setText(item.getPrice() + " 🪙");
            holder.btnAction.setBackgroundResource(R.drawable.neuro_primary_button);
            holder.btnAction.setEnabled(true);
            holder.btnAction.setAlpha(1f);
            holder.btnAction.setOnClickListener(v -> {
                boolean ok = profileManager.purchaseConsumable(item.getId(), item.getPrice(), item.getQuantity());
                Toast.makeText(context, ok ? R.string.purchase_successful : R.string.not_enough_coins,
                        Toast.LENGTH_SHORT).show();
                if (ok) {
                    notifyItemChanged(holder.getAdapterPosition());
                    if (actionListener != null) actionListener.onItemAction();
                }
            });
        } else if (item.getCategory() == ShopItem.Category.BUNDLE) {
            boolean bundleOwned = item.getId().equals("starter_bundle")
                    ? profileManager.hasStarterBundle()
                    : profileManager.isItemPurchased(item.getId());
            if (bundleOwned) {
                holder.btnAction.setText(R.string.shop_already_purchased);
                holder.btnAction.setBackgroundResource(R.drawable.neuro_secondary_button);
                holder.btnAction.setEnabled(false);
                holder.btnAction.setAlpha(0.7f);
            } else {
                holder.btnAction.setText(item.getPrice() + " 🪙");
                holder.btnAction.setBackgroundResource(R.drawable.neuro_primary_button);
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1f);
                holder.btnAction.setOnClickListener(v -> {
                    if (item.getId().equals("starter_bundle")) {
                        boolean ok = profileManager.purchaseStarterBundle(item.getPrice());
                        Toast.makeText(context, ok ? R.string.purchase_successful : R.string.not_enough_coins,
                                Toast.LENGTH_SHORT).show();
                        if (ok) {
                            notifyItemChanged(holder.getAdapterPosition());
                            if (actionListener != null) actionListener.onItemAction();
                        }
                    } else {
                        if (coinManager.spend(item.getPrice(), "bundle_" + item.getId())) {
                            profileManager.addPurchasedItem(item.getId());
                            for (String contentId : item.bundleContents) {
                                profileManager.addPurchasedItem(contentId);
                            }
                            Toast.makeText(context, R.string.purchase_successful, Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            if (actionListener != null) actionListener.onItemAction();
                        } else {
                            Toast.makeText(context, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            boolean isOwned = item.isFree() || profileManager.isItemPurchased(item.getId());
            boolean isEquipped = !isLocked && isItemEquipped(item);

            if (isLocked) {
                holder.btnAction.setText(R.string.shop_locked_short);
                holder.btnAction.setEnabled(false);
                holder.btnAction.setAlpha(0.5f);
            } else if (isEquipped) {
                holder.btnAction.setText(R.string.shop_item_equipped);
                holder.btnAction.setBackgroundResource(R.drawable.neuro_secondary_button);
                holder.btnAction.setEnabled(false);
                holder.btnAction.setAlpha(1f);
            } else if (isOwned) {
                holder.btnAction.setText(R.string.shop_item_equip);
                holder.btnAction.setBackgroundResource(R.drawable.neuro_secondary_button);
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1f);
                holder.btnAction.setOnClickListener(v -> {
                    equipItem(item);
                    notifyDataSetChanged();
                    if (actionListener != null) actionListener.onItemAction();
                });
            } else {
                holder.btnAction.setText(item.getPrice() + " 🪙");
                holder.btnAction.setBackgroundResource(R.drawable.neuro_primary_button);
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1f);
                holder.btnAction.setOnClickListener(v -> {
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
        }
    }

    private boolean isItemEquipped(ShopItem item) {
        switch (item.getCategory()) {
            case AVATAR_FRAME:
                return item.getId().equals(profileManager.getActiveFrame());
            case DICE_SKIN:
                return item.getId().equals(profileManager.getActiveDice());
            case TITLE:
                return item.getId().equals(profileManager.getActiveTitle());
            default:
                return false;
        }
    }

    private void equipItem(ShopItem item) {
        switch (item.getCategory()) {
            case AVATAR_FRAME:
                profileManager.setActiveFrame(item.getId());
                break;
            case DICE_SKIN:
                profileManager.setActiveDice(item.getId());
                break;
            case TITLE:
                profileManager.setActiveTitle(item.getId());
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon;
        final TextView tvQuantityBadge;
        final TextView tvTitle;
        final TextView tvDesc;
        final TextView tvRarity;
        final TextView tvBadge;
        final TextView tvUnlock;
        final View layoutUnlock;
        final ImageView ivLock;
        final View rarityBar;
        final ProgressBar unlockProgress;
        final Button btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvShopItemIcon);
            tvQuantityBadge = itemView.findViewById(R.id.tvShopItemQuantityBadge);
            tvTitle = itemView.findViewById(R.id.tvShopItemTitle);
            tvDesc = itemView.findViewById(R.id.tvShopItemDesc);
            tvRarity = itemView.findViewById(R.id.tvShopItemRarity);
            tvBadge = itemView.findViewById(R.id.tvShopItemBadge);
            tvUnlock = itemView.findViewById(R.id.tvShopItemUnlock);
            layoutUnlock = itemView.findViewById(R.id.layoutShopItemUnlock);
            ivLock = itemView.findViewById(R.id.ivShopItemLock);
            rarityBar = itemView.findViewById(R.id.viewRarityBar);
            unlockProgress = itemView.findViewById(R.id.shopItemUnlockProgress);
            btnAction = itemView.findViewById(R.id.btnShopItemAction);
        }
    }
}
