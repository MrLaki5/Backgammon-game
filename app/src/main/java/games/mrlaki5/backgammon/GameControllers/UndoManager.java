package games.mrlaki5.backgammon.GameControllers;

import games.mrlaki5.backgammon.Database.PlayerProfileManager;
import games.mrlaki5.backgammon.Economy.CoinConfig;

public class UndoManager {
    public interface UndoCallback {
        void onUndoGranted();
        void onUndoDenied(String reason); // "no_charges" | "no_coins"
    }

    private final PlayerProfileManager profileManager;
    private int undoCount = 0; // per-game undo counter

    public UndoManager(PlayerProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public void reset() {
        undoCount = 0;
    }

    /**
     * Attempt an undo. Uses a free charge if available; otherwise applies
     * escalating coin cost based on how many undos have been done this game.
     */
    public void requestUndo(UndoCallback callback) {
        if (profileManager.useUndoCharge()) {
            undoCount++;
            callback.onUndoGranted();
            return;
        }
        int cost = getUndoCost();
        if (profileManager.getBalance() >= cost) {
            profileManager.deductCoins(cost);
            undoCount++;
            callback.onUndoGranted();
        } else {
            callback.onUndoDenied("no_coins");
        }
    }

    public int getUndoCost() {
        if (undoCount == 0) return CoinConfig.UNDO_COST_1;
        if (undoCount == 1) return CoinConfig.UNDO_COST_2;
        return CoinConfig.UNDO_COST_3;
    }

    public int getUndoCount() {
        return undoCount;
    }
}