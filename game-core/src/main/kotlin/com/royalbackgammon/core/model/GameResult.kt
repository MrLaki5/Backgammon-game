package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

/** Outcome of a finished game; [winner] is [Player.NONE] for a [WinType.DRAW]. */
@Serializable
data class GameResult(val winner: Int, val winType: WinType, val points: Int)
