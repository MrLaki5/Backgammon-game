package com.royalbackgammon.core.dice

import com.royalbackgammon.core.model.Die
import com.royalbackgammon.core.model.GameState

/**
 * Interface for dice rolling. Allows injection of deterministic implementations
 * for testing, and server-authoritative implementations for online play.
 */
interface DiceRoller {
    /**
     * Rolls a single die, returning a value 1–6.
     */
    fun rollOne(): Int

    /**
     * Rolls dice for a full turn. Returns an array of 4 Die objects:
     * - Normal roll: indices 0,1 have values; 2,3 are inactive (used=true).
     * - Doubles: all 4 have the same value.
     */
    fun rollTurn(): Array<Die> {
        val d1 = rollOne()
        val d2 = rollOne()
        return if (d1 == d2) {
            arrayOf(Die(d1), Die(d1), Die(d1), Die(d1))
        } else {
            arrayOf(Die(d1), Die(d2), Die(0, used = true), Die(0, used = true))
        }
    }

    /**
     * Rolls dice for the initial phase (each player rolls one die).
     * Updates the given game state's dice array.
     */
    fun rollInitial(state: GameState) {
        val value = rollOne()
        when (state.turnState) {
            GameState.STATE_INITIAL_ROLL_P1 -> {
                state.dice[0] = Die(value)
                state.dice[1] = Die(state.dice[1].value, state.dice[1].used)
            }
            GameState.STATE_INITIAL_ROLL_P2 -> {
                state.dice[1] = Die(value)
            }
        }
    }
}
