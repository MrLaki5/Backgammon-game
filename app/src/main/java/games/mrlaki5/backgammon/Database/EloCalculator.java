package games.mrlaki5.backgammon.Database;

import com.royalbackgammon.core.scoring.EloRating;

/**
 * Java bridge to the Kotlin EloRating utility in game-core.
 * Provides simple methods for PlayerProfile to call.
 */
public final class EloCalculator {

    private EloCalculator() {}

    /**
     * Calculates new ELO rating after a game result.
     *
     * @param currentRating Player's current ELO
     * @param opponentRating Opponent's ELO
     * @param won true if player won
     * @return New ELO rating
     */
    public static int newRating(int currentRating, int opponentRating, boolean won) {
        return EloRating.INSTANCE.calculate(currentRating, opponentRating, won, 50);
    }

    /**
     * Calculates new ELO with total games considered (for K-factor).
     */
    public static int newRating(int currentRating, int opponentRating,
                                 boolean won, int totalGames) {
        return EloRating.INSTANCE.calculate(currentRating, opponentRating, won, totalGames);
    }

    /**
     * Returns the rating change (positive for gain, negative for loss).
     */
    public static int ratingDelta(int currentRating, int opponentRating,
                                   boolean won, int totalGames) {
        return EloRating.INSTANCE.ratingDelta(currentRating, opponentRating, won, totalGames);
    }
}
