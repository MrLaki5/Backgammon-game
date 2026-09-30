package com.royalbackgammon.core.bot

/**
 * Bot difficulty levels with associated search parameters.
 */
enum class BotDifficulty(
    val lookaheadRolls: Int,
    val noise: Double,
    val borneOffWeight: Double,
    val barPenalty: Double,
    val progressWeight: Double,
    val blotPenalty: Double,
    val pointWeight: Double,
    val stackLimit: Double,
    val homeWeight: Double,
    val primeWeight: Double,
    val opponentPrimeWeight: Double,
    val directShotPenalty: Double,
    val nodeBudget: Int
) {
    EASY(
        lookaheadRolls = 0, noise = 70.0, borneOffWeight = 82.0,
        barPenalty = 62.0, progressWeight = 1.8, blotPenalty = 10.0,
        pointWeight = 12.0, stackLimit = 22.0, homeWeight = 3.0,
        primeWeight = 3.0, opponentPrimeWeight = 5.0, directShotPenalty = 1.5,
        nodeBudget = 80
    ),
    MEDIUM(
        lookaheadRolls = 1, noise = 7.0, borneOffWeight = 106.0,
        barPenalty = 92.0, progressWeight = 3.0, blotPenalty = 26.0,
        pointWeight = 32.0, stackLimit = 42.0, homeWeight = 6.5,
        primeWeight = 10.0, opponentPrimeWeight = 10.0, directShotPenalty = 4.5,
        nodeBudget = 520
    ),
    HARD(
        lookaheadRolls = 1, noise = 2.0, borneOffWeight = 116.0,
        barPenalty = 105.0, progressWeight = 3.4, blotPenalty = 36.0,
        pointWeight = 40.0, stackLimit = 50.0, homeWeight = 8.5,
        primeWeight = 15.0, opponentPrimeWeight = 13.0, directShotPenalty = 6.0,
        nodeBudget = 1800
    ),
    ROYAL(
        lookaheadRolls = 1, noise = 0.35, borneOffWeight = 126.0,
        barPenalty = 118.0, progressWeight = 3.8, blotPenalty = 44.0,
        pointWeight = 48.0, stackLimit = 58.0, homeWeight = 10.0,
        primeWeight = 20.0, opponentPrimeWeight = 15.0, directShotPenalty = 8.0,
        nodeBudget = 3600
    );
}
