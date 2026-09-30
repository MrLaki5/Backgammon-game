package games.mrlaki5.backgammon.Journey;

import android.content.Context;
import android.content.SharedPreferences;

import games.mrlaki5.backgammon.Economy.CoinManager;

/** Tracks how far the player has come in the journey and pays the chapter rewards. */
public class JourneyManager {

    /** The piece the last completed chapter handed over, or null. */
    private String lastGranted;

    private static final String PREFS_NAME = "journey_prefs";
    private static final String KEY_COMPLETED = "completed_stages";

    private final SharedPreferences prefs;
    private final Context context;

    public JourneyManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** The piece the last call to completeStage handed over, or null. */
    public String getLastGrantedItem() {
        return lastGranted;
    }

    /** Number of chapters finished; also the index of the chapter now open. */
    public int getCompletedCount() {
        return Math.min(prefs.getInt(KEY_COMPLETED, 0), JourneyStage.values().length);
    }

    public boolean isUnlocked(int stageIndex) {
        return stageIndex <= getCompletedCount();
    }

    public boolean isCompleted(int stageIndex) {
        return stageIndex < getCompletedCount();
    }

    public boolean isJourneyFinished() {
        return getCompletedCount() >= JourneyStage.values().length;
    }

    /**
     * Marks [stageIndex] finished and pays its reward, but only for the chapter that was open —
     * replaying an earlier chapter pays nothing.
     * Returns the coins paid, 0 if nothing changed.
     */
    public int completeStage(int stageIndex, CoinManager coinManager) {
        if (stageIndex != getCompletedCount() || stageIndex >= JourneyStage.values().length) {
            return 0;
        }
        prefs.edit().putInt(KEY_COMPLETED, stageIndex + 1).apply();
        int reward = JourneyStage.values()[stageIndex].getRewardCoins();
        if (coinManager != null && reward > 0) {
            coinManager.earn(reward, "journey_stage_" + stageIndex);
        }
        // Four chapters also hand over a season banner. Coins are the reward for
        // every chapter; a banner is the reward for this one, and it is the only
        // way one is ever obtained.
        lastGranted = games.mrlaki5.backgammon.Economy.SeasonRewards
                .grantForStage(context, stageIndex);
        return reward;
    }
}
