package com.royalbackgammon.core.model

/**
 * Player identifiers used throughout the engine.
 */
object Player {
    const val NONE = 0
    const val WHITE = 1  // Moves from low real positions toward high (bearing off at 25+)
    const val RED = 2    // Moves in reverse direction

    fun opponent(player: Int): Int = if (player == WHITE) RED else WHITE
}
