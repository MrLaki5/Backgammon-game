package com.royalbackgammon.core.logic

import com.royalbackgammon.core.dice.DiceRoller
import com.royalbackgammon.core.dice.RandomDiceRoller
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.variant.OpeningRoll

/**
 * High-level game engine that manages the full game lifecycle.
 * Coordinates dice rolling, move calculation, and state transitions.
 *
 * @property state The current game state (mutable).
 * @property diceRoller Injectable dice source.
 */
class GameEngine(
    val state: GameState = GameState.newGame(),
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {

    /**
     * Currently available legal moves for the active player.
     * Recalculated after dice rolls and after each move.
     */
    var legalMoves: List<Move> = emptyList()
        private set

    /** Scored result once the game is over, null while ongoing. */
    var result: GameResult? = null
        private set

    init {
        result = BackgammonRules.gameResult(state.board, state.variant)
        // If engine is created with a mid-turn state, calculate legal moves immediately
        if (state.turnState == GameState.STATE_MOVE && result == null) {
            recalculateLegalMoves()
        }
    }

    /**
     * Rolls dice for the current turn state. Updates [state.dice] and
     * transitions the FSM appropriately for initial rolls.
     * A tied opening roll returns to [GameState.STATE_INITIAL_ROLL_P1]. Under
     * [OpeningRoll.STARTER_REROLLS] the opening roll only picks the starter, who then rolls
     * both dice from [GameState.STATE_ROLL].
     * Returns the rolled values.
     */
    fun rollDice(): Array<Die> {
        return when (state.turnState) {
            GameState.STATE_INITIAL_ROLL_P1 -> {
                val value = diceRoller.rollOne()
                state.dice[0] = Die(value)
                state.dice[1] = Die(state.dice[1].value, state.dice[1].used)
                state.turnState = GameState.STATE_INITIAL_ROLL_P2
                state.switchPlayer()
                state.dice
            }
            GameState.STATE_INITIAL_ROLL_P2 -> {
                val value = diceRoller.rollOne()
                state.dice[0] = Die(state.dice[0].value)
                state.dice[1] = Die(value)
                val whiteRoll = state.dice[0].value
                if (whiteRoll == value) {
                    // Tie: both players roll again
                    state.currentPlayer = Player.WHITE
                    state.turnState = GameState.STATE_INITIAL_ROLL_P1
                    return state.dice
                }
                state.currentPlayer = if (whiteRoll > value) Player.WHITE else Player.RED
                when (state.variant.openingRoll) {
                    OpeningRoll.PLAY_OPENING_DICE -> {
                        state.turnState = GameState.STATE_MOVE
                        recalculateLegalMoves()
                    }
                    OpeningRoll.STARTER_REROLLS -> {
                        for (i in 0 until 4) state.dice[i] = Die(0, used = true)
                        state.turnState = GameState.STATE_ROLL
                    }
                }
                state.dice
            }
            GameState.STATE_ROLL -> {
                val rolled = diceRoller.rollTurn()
                for (i in 0 until 4) state.dice[i] = rolled[i]
                if (state.variant.aceyDeuceyRoll && isAceyDeucey(state.dice)) {
                    state.bonusDoublePending = true
                    state.extraTurnPending = true
                }
                state.turnState = GameState.STATE_MOVE
                recalculateLegalMoves()
                state.dice
            }
            else -> state.dice // Already in move state
        }
    }

    /**
     * Attempts to apply a move from [from] to [to].
     * Returns the [MoveResult]. After a successful move, recalculates legal moves.
     * If no more moves are available, ends the turn automatically.
     */
    fun makeMove(from: Int, to: Int): MoveResult {
        val move = BackgammonRules.isLegalMove(from, to, legalMoves)
            ?: return MoveResult(applied = false, from = from, to = to)
        return applyMove(move)
    }

    /**
     * Applies a [Move] object directly (used by bot).
     * Returns the [MoveResult].
     */
    fun applyMove(move: Move): MoveResult {
        val moveResult = MoveExecutor.applyMove(state, move)
        if (!finishIfWon()) recalculateLegalMoves()
        return moveResult
    }

    /** True once the 1-2 has been played and the player still has to name a double. */
    fun needsBonusDouble(): Boolean =
        state.bonusDoublePending && legalMoves.isEmpty() && state.turnState == GameState.STATE_MOVE

    /** Acey-deucey: plays the named double [value] (1–6) as four dice. */
    fun playBonusDouble(value: Int) {
        require(value in 1..6) { "A named double must be 1-6" }
        check(state.bonusDoublePending) { "No bonus double is pending" }
        for (i in 0 until 4) state.dice[i] = Die(value)
        state.bonusDoublePending = false
        recalculateLegalMoves()
    }

    /**
     * Ends the current player's turn and switches to the other player's roll phase.
     * Should be called when [legalMoves] is empty after dice have been rolled.
     */
    fun endTurn() {
        if (state.extraTurnPending && !state.bonusDoublePending) {
            // Acey-deucey: the same player rolls again
            state.extraTurnPending = false
            state.turnState = GameState.STATE_ROLL
            legalMoves = emptyList()
            return
        }
        state.passTurn()
        state.turnState = GameState.STATE_ROLL
        legalMoves = emptyList()
    }

    /**
     * Returns true if the current turn has no more moves available
     * (dice were rolled but no legal moves exist, or all dice have been consumed).
     */
    fun isTurnComplete(): Boolean = legalMoves.isEmpty() && state.turnState == GameState.STATE_MOVE

    /**
     * Returns true if the game has ended.
     */
    fun isGameOver(): Boolean = result != null

    private fun isAceyDeucey(dice: Array<Die>): Boolean {
        val values = dice.filter { it.value > 0 }.map { it.value }.sorted()
        return values == listOf(1, 2)
    }

    private fun finishIfWon(): Boolean {
        val gameResult = BackgammonRules.gameResult(state.board, state.variant) ?: return false
        state.winner = gameResult.winner
        result = gameResult
        legalMoves = emptyList()
        return true
    }

    private fun recalculateLegalMoves() {
        legalMoves = BackgammonRules.calculateLegalMoves(
            state.board, state.currentPlayer, state.dice, state.variant, state.turnContext()
        )
    }
}
