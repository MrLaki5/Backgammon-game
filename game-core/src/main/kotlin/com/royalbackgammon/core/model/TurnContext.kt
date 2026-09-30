package com.royalbackgammon.core.model

/**
 * Per-turn facts that the board alone cannot tell, needed by the running family's head rule.
 * @property headMovesThisTurn Checkers already moved off the head during the current turn.
 * @property firstTurn True during the player's first turn of the game.
 * @property hitCheckersOnBar Acey-deucey: checkers waiting on the bar because they were hit.
 *   Those must be entered first; checkers that have simply never entered may wait.
 */
data class TurnContext(
    val headMovesThisTurn: Int = 0,
    val firstTurn: Boolean = false,
    val hitCheckersOnBar: Int = 0
) {
    companion object {
        @JvmField
        val NONE = TurnContext()
    }
}
