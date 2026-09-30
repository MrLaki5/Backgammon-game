package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

/**
 * A legal move that a player can make.
 * @property dieValue The die value consumed by this move.
 * @property from Source board index (0–27).
 * @property to Destination board index (0–27).
 */
@Serializable
data class Move(
    val dieValue: Int,
    val from: Int,
    val to: Int
)
