package com.royalbackgammon.core.scoring

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Standard ELO rating calculator for backgammon matches.
 *
 * Uses the classic Elo formula:
 *   Expected score = 1 / (1 + 10^((opponentRating - playerRating) / 400))
 *   New rating = oldRating + K * (actualScore - expectedScore)
 *
 * K-factor varies by total games played (higher K for new players = faster convergence).
 */
object EloRating {

    const val DEFAULT_RATING = 1200
    const val K_NEW_PLAYER = 40      // First 30 games
    const val K_INTERMEDIATE = 24    // 30–100 games
    const val K_ESTABLISHED = 16     // 100+ games

    /**
     * Calculates the new rating after a game.
     *
     * @param playerRating Current ELO rating of the player.
     * @param opponentRating Current ELO rating of the opponent.
     * @param won True if the player won, false if lost.
     * @param totalGames Total games the player has played (for K-factor selection).
     * @return The new ELO rating.
     */
    fun calculate(
        playerRating: Int,
        opponentRating: Int,
        won: Boolean,
        totalGames: Int = 50
    ): Int {
        val expected = expectedScore(playerRating, opponentRating)
        val actual = if (won) 1.0 else 0.0
        val k = kFactor(totalGames)
        val newRating = playerRating + (k * (actual - expected)).roundToInt()
        // Never go below 100
        return maxOf(100, newRating)
    }

    /**
     * Calculates expected score (probability of winning) for a player.
     * Returns value between 0.0 and 1.0.
     */
    fun expectedScore(playerRating: Int, opponentRating: Int): Double {
        return 1.0 / (1.0 + 10.0.pow((opponentRating - playerRating) / 400.0))
    }

    /**
     * Returns the appropriate K-factor based on player experience.
     */
    fun kFactor(totalGames: Int): Int {
        return when {
            totalGames < 30 -> K_NEW_PLAYER
            totalGames < 100 -> K_INTERMEDIATE
            else -> K_ESTABLISHED
        }
    }

    /**
     * Calculates rating change (delta) without applying it.
     * Positive = gained rating, negative = lost rating.
     */
    fun ratingDelta(
        playerRating: Int,
        opponentRating: Int,
        won: Boolean,
        totalGames: Int = 50
    ): Int {
        return calculate(playerRating, opponentRating, won, totalGames) - playerRating
    }
}
