package com.royalbackgammon.core.scoring

import com.royalbackgammon.core.model.GameResult
import com.royalbackgammon.core.model.Player
import com.royalbackgammon.core.variant.Variant
import kotlinx.serialization.Serializable

/** Running score of a match played to [targetPoints] (1 = single game). */
@Serializable
data class MatchState(
    val variant: Variant = Variant.STANDARD,
    /** Variants played in turn (Tavli); empty means every game uses [variant]. */
    val rotation: List<Variant> = emptyList(),
    val targetPoints: Int = 1,
    var whiteScore: Int = 0,
    var redScore: Int = 0,
    var gamesPlayed: Int = 0
) {
    init {
        require(targetPoints >= 1) { "targetPoints must be >= 1" }
    }

    fun record(result: GameResult) {
        check(!isOver()) { "Match already finished" }
        when (result.winner) {
            Player.WHITE -> whiteScore += result.points
            Player.RED -> redScore += result.points
            else -> Unit  // draw: counts as a game, no points
        }
        gamesPlayed++
    }

    /** Variant of the game about to be played. */
    fun currentVariant(): Variant =
        if (rotation.isEmpty()) variant else rotation[gamesPlayed % rotation.size]

    fun scoreOf(player: Int): Int = if (player == Player.WHITE) whiteScore else redScore

    fun isOver(): Boolean = whiteScore >= targetPoints || redScore >= targetPoints

    fun winner(): Int = when {
        whiteScore >= targetPoints -> Player.WHITE
        redScore >= targetPoints -> Player.RED
        else -> Player.NONE
    }
}
