package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

/**
 * Outcome of applying a move to the game state.
 */
@Serializable
data class MoveResult(
    val applied: Boolean,
    val from: Int,
    val to: Int,
    val hit: Boolean = false
)
