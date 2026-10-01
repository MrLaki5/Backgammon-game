package games.mrlaki5.backgammon.Database;

import android.content.Context;

/**
 * Records game results to both the legacy scores table (for backward compatibility)
 * and the new leaderboard/profile system.
 *
 * Call after every completed game (vs_bot, pass_and_play, or online).
 */
public class GameResultRecorder {

    private final DbHelper dbHelper;

    public GameResultRecorder(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    /**
     * Records a game result for both players.
     *
     * @param winnerName display name of the winner
     * @param loserName display name of the loser
     * @param gameMode "vs_bot", "pass_and_play", or "online"
     */
    public void recordResult(String winnerName, String loserName, String gameMode) {
        // Get or create profiles
        PlayerProfile winner = dbHelper.getOrCreateProfile(winnerName);
        PlayerProfile loser = dbHelper.getOrCreateProfile(loserName);

        int winnerEloBefore = winner.getElo();
        int loserEloBefore = loser.getElo();

        // Update ELO ratings
        winner.recordWin(loserEloBefore);
        loser.recordLoss(winnerEloBefore);

        // Persist updated profiles
        dbHelper.saveProfile(winner);
        dbHelper.saveProfile(loser);

        // Record leaderboard entries
        dbHelper.recordGameResult(winner.getId(), loser.getId(),
                true, winnerEloBefore, winner.getElo(), gameMode);
        dbHelper.recordGameResult(loser.getId(), winner.getId(),
                false, loserEloBefore, loser.getElo(), gameMode);
    }

    /**
     * Records a game result using profile IDs directly (for online games where
     * profiles are already loaded).
     */
    public void recordResult(PlayerProfile winner, PlayerProfile loser, String gameMode) {
        int winnerEloBefore = winner.getElo();
        int loserEloBefore = loser.getElo();

        winner.recordWin(loserEloBefore);
        loser.recordLoss(winnerEloBefore);

        dbHelper.saveProfile(winner);
        dbHelper.saveProfile(loser);

        dbHelper.recordGameResult(winner.getId(), loser.getId(),
                true, winnerEloBefore, winner.getElo(), gameMode);
        dbHelper.recordGameResult(loser.getId(), winner.getId(),
                false, loserEloBefore, loser.getElo(), gameMode);
    }
}
