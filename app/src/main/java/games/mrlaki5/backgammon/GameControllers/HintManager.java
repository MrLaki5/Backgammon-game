package games.mrlaki5.backgammon.GameControllers;

import android.content.Context;
import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinConfig;

public class HintManager {
    public interface HintCallback {
        void onHintGranted();
        void onHintDenied(String reason); // "no_charges" | "no_coins"
    }

    private final Context context;
    private final PlayerProfileManager profileManager;

    public HintManager(Context context, PlayerProfileManager profileManager) {
        this.context = context;
        this.profileManager = profileManager;
    }

    /**
     * Attempt to grant a hint. Uses a charge if available, else deducts coins.
     * Calls callback.onHintGranted() or callback.onHintDenied(reason).
     */
    public void requestHint(HintCallback callback) {
        if (profileManager.useHintCharge()) {
            callback.onHintGranted();
            return;
        }
        if (profileManager.getBalance() >= CoinConfig.HINT_COST) {
            profileManager.deductCoins(CoinConfig.HINT_COST);
            callback.onHintGranted();
        } else {
            callback.onHintDenied("no_coins");
        }
    }

    public int getHintCharges() {
        return profileManager.getHintCharges();
    }
}