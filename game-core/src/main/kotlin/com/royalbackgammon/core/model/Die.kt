package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a single die in the game.
 * @property value The rolled number (1–6), or 0 if inactive.
 * @property used Whether this die has already been consumed by a move.
 */
@Serializable
data class Die(
    var value: Int = 0,
    var used: Boolean = false
)
