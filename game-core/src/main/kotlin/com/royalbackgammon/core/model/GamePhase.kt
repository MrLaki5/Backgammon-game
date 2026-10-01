package com.royalbackgammon.core.model

/**
 * The phase of the game for a given player.
 */
enum class GamePhase {
    /** Normal play — checkers are spread across the board. */
    PLAYING,
    /** All checkers are in home board — bearing off is allowed. */
    BEARING_OFF,
    /** All checkers have been borne off — this player has won. */
    FINISHED
}
