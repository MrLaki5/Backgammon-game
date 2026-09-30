package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

/**
 * Represents the state of a single point (triangle) on the board.
 * @property chipCount Number of checkers on this point.
 * @property owner Player who owns the checkers (0 = empty).
 * @property pinned Player whose single checker is pinned underneath (Plakoto), 0 if none.
 *   It is not counted in [chipCount] and cannot move until the stack above leaves.
 */
@Serializable
data class BoardField @JvmOverloads constructor(
    var chipCount: Int = 0,
    var owner: Int = 0,
    var pinned: Int = 0
) {
    /** Checkers of [player] on this field, including a pinned one. */
    fun checkersOf(player: Int): Int =
        (if (owner == player) chipCount else 0) + (if (pinned == player) 1 else 0)

    companion object {
        const val NOBODY = 0
        const val WHITE = 1
        const val RED = 2
    }
}
