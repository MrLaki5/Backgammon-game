package com.royalbackgammon.core.dice

/**
 * Deterministic dice roller for testing.
 * Returns values from the provided sequence in order, cycling if exhausted.
 */
class FixedDiceRoller(private val values: List<Int>) : DiceRoller {
    private var index = 0

    constructor(vararg values: Int) : this(values.toList())

    override fun rollOne(): Int {
        val value = values[index % values.size]
        index++
        return value
    }
}
