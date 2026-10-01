package com.royalbackgammon.core.logic

import com.royalbackgammon.core.model.GameState
import com.royalbackgammon.core.model.Player
import com.royalbackgammon.core.variant.RuleFamily
import com.royalbackgammon.core.variant.Variant

/**
 * Converts between internal matrix positions (0–27) and "real" positions
 * relative to a player's direction of travel (1–24, 0=bar, 100=borne off).
 *
 * Board indexing:
 *  - Matrix 0..11  → White real 12..1 (descending)
 *  - Matrix 12..23 → White real 13..24 (ascending)
 *  - Matrix 24 = White bar (real 0)
 *  - Matrix 25 = Red bar (real 0 for Red)
 *  - Matrix 26 = Red bear-off (real 100 for Red)
 *  - Matrix 27 = White bear-off (real 100 for White)
 *
 * Hitting family: Red sees the board mirrored, Red's real = 25 − White's real.
 * Running family: Red travels the same circle from the diagonally opposite head,
 * Red's real = White's real rotated by 12 points.
 */
object PositionMapper {

    @JvmStatic
    @JvmOverloads
    fun toReal(matrixPos: Int, player: Int, variant: Variant = Variant.STANDARD): Int {
        if (player == Player.RED) {
            return when (matrixPos) {
                GameState.RED_BAR -> 0
                GameState.RED_BEAR_OFF -> 100
                else -> {
                    val white = toReal(matrixPos, Player.WHITE)
                    if (variant.family == RuleFamily.RUNNING && matrixPos < 24) rotate(white)
                    else 25 - white
                }
            }
        }
        return when (matrixPos) {
            GameState.WHITE_BAR -> 0
            GameState.WHITE_BEAR_OFF -> 100
            in 0..11 -> 12 - matrixPos
            else -> matrixPos + 1  // 12..23 → 13..24
        }
    }

    @JvmStatic
    @JvmOverloads
    fun toMatrix(realPos: Int, player: Int, variant: Variant = Variant.STANDARD): Int {
        if (player == Player.RED) {
            return when (realPos) {
                0 -> GameState.RED_BAR
                100 -> GameState.RED_BEAR_OFF
                else -> toMatrix(
                    if (variant.family == RuleFamily.RUNNING) rotate(realPos) else 25 - realPos,
                    Player.WHITE
                )
            }
        }
        return when (realPos) {
            0 -> GameState.WHITE_BAR
            100 -> GameState.WHITE_BEAR_OFF
            in 1..12 -> 12 - realPos
            else -> realPos - 1  // 13..24 → 12..23
        }
    }

    /** Half-turn rotation of the 24-point circle; its own inverse. */
    private fun rotate(real: Int): Int = (real + 11) % 24 + 1
}
