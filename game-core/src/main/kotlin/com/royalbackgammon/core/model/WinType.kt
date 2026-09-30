package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class WinType {
    /** Loser has borne off at least one checker. */
    SINGLE,
    /** Loser has borne off no checkers (Mars in Tavla). */
    GAMMON,
    /** Gammon and loser still has a checker on the bar or in the winner's home board. */
    BACKGAMMON,
    /** Plakoto: the loser's last checker on its starting point was pinned. Scores as gammon. */
    MOTHER_PINNED,
    /** Plakoto: both mothers pinned. No winner, no points. */
    DRAW
}
