package com.royalbackgammon.core.dice

import kotlin.random.Random

/**
 * Standard dice roller using Kotlin's Random.
 * Accepts an optional [Random] instance for reproducible sequences.
 */
class RandomDiceRoller(private val random: Random = Random.Default) : DiceRoller {
    override fun rollOne(): Int = random.nextInt(1, 7)
}
