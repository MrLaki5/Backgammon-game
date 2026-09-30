package com.royalbackgammon.core.bot

import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.MoveExecutor
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Bot AI strategy using minimax/expectimax search with difficulty-scaled evaluation.
 * Pure Kotlin, no Android dependencies.
 */
class BotStrategy(
    private val difficulty: BotDifficulty = BotDifficulty.MEDIUM,
    private val random: Random = Random.Default
) {

    /**
     * Chooses the best move from [legalMoves] for the given [state].
     * Returns null if no moves are available.
     */
    fun chooseMove(state: GameState, legalMoves: List<Move>): Move? {
        if (legalMoves.isEmpty()) return null

        var best = legalMoves[0]
        var bestScore = -Double.MAX_VALUE
        val budget = SearchBudget(difficulty.nodeBudget)

        for (move in legalMoves) {
            val copy = state.deepCopy()
            MoveExecutor.applyMove(copy, move)
            var score = continueTurnOrRoll(
                copy, state.currentPlayer, difficulty.lookaheadRolls, budget
            )
            score += tacticalBonus(state, move)
            score += random.nextDouble() * difficulty.noise

            if (score > bestScore) {
                bestScore = score
                best = move
            }
        }
        return best
    }

    private fun continueTurnOrRoll(
        state: GameState, rootPlayer: Int, rollsRemaining: Int, budget: SearchBudget
    ): Double {
        if (!budget.tryVisit()) return evaluateBoard(state, rootPlayer)

        val moves = BackgammonRules.calculateLegalMoves(
            state.board, state.currentPlayer, state.dice, state.variant, state.turnContext()
        )

        if (moves.isNotEmpty()) {
            val maximizing = state.currentPlayer == rootPlayer
            var best = if (maximizing) -Double.MAX_VALUE else Double.MAX_VALUE
            for (move in moves) {
                val next = state.deepCopy()
                MoveExecutor.applyMove(next, move)
                val score = continueTurnOrRoll(next, rootPlayer, rollsRemaining, budget)
                best = if (maximizing) max(best, score) else min(best, score)
            }
            return best
        }

        // No moves — check for end of game
        if (rollsRemaining <= 0 ||
            BackgammonRules.gamePhase(state.board, rootPlayer) == GamePhase.FINISHED ||
            BackgammonRules.gamePhase(state.board, Player.opponent(rootPlayer)) == GamePhase.FINISHED
        ) {
            return evaluateBoard(state, rootPlayer)
        }

        // Simulate opponent's turn
        val nextTurn = state.deepCopy()
        nextTurn.passTurn()
        return expectedRollValue(nextTurn, rootPlayer, rollsRemaining - 1, budget)
    }

    private fun expectedRollValue(
        state: GameState, rootPlayer: Int, rollsRemaining: Int, budget: SearchBudget
    ): Double {
        var expected = 0.0
        for (outcome in ROLL_OUTCOMES) {
            val rolled = state.deepCopy()
            rolled.dice[0] = Die(outcome.first)
            rolled.dice[1] = Die(outcome.second)
            if (outcome.first == outcome.second) {
                rolled.dice[2] = Die(outcome.first)
                rolled.dice[3] = Die(outcome.first)
            } else {
                rolled.dice[2] = Die(0, used = true)
                rolled.dice[3] = Die(0, used = true)
            }
            expected += outcome.probability * continueTurnOrRoll(
                rolled, rootPlayer, rollsRemaining, budget
            )
        }
        return expected
    }

    private fun tacticalBonus(state: GameState, move: Move): Double {
        val board = state.board
        val player = state.currentPlayer
        val opponent = Player.opponent(player)
        val dst = move.to
        var bonus = 0.0

        // Bearing off bonus
        if (dst == GameState.RED_BEAR_OFF || dst == GameState.WHITE_BEAR_OFF) {
            bonus += if (difficulty == BotDifficulty.ROYAL) 190.0 else 135.0
        }
        // Hitting an opponent blot
        if (dst in 0..25 && board[dst].owner == opponent && board[dst].chipCount == 1) {
            bonus += if (difficulty >= BotDifficulty.HARD) 165.0 else 95.0
        }
        // Escaping a lone checker
        if (board[move.from].chipCount == 1) {
            bonus += 32.0
        }
        return bonus
    }

    private fun evaluateBoard(state: GameState, player: Int): Double {
        val board = state.board
        val opponent = Player.opponent(player)
        var score = 0.0

        for (i in board.indices) {
            val chips = board[i].chipCount
            val owner = board[i].owner
            if (chips <= 0 || owner == Player.NONE) continue

            val sign = if (owner == player) 1.0 else -1.0
            val real = normalizedReal(i, owner)
            val home = real in 19..24
            val bar = (owner == Player.WHITE && i == GameState.WHITE_BAR) ||
                    (owner == Player.RED && i == GameState.RED_BAR)
            val borneOff = (owner == Player.WHITE && i == GameState.WHITE_BEAR_OFF) ||
                    (owner == Player.RED && i == GameState.RED_BEAR_OFF)

            when {
                borneOff -> score += sign * chips * difficulty.borneOffWeight
                bar -> score -= sign * chips * difficulty.barPenalty
                else -> {
                    score += sign * chips * real * difficulty.progressWeight
                    if (chips == 1) score -= sign * blotPenalty(board, i, owner)
                    if (chips == 2) score += sign * difficulty.pointWeight
                    if (chips >= 3) score += sign * min(
                        difficulty.stackLimit,
                        difficulty.pointWeight * 0.45 + chips * 4.0
                    )
                    if (home) score += sign * chips * difficulty.homeWeight
                }
            }
        }

        score += madePointRun(board, player) * difficulty.primeWeight
        score -= madePointRun(board, opponent) * difficulty.opponentPrimeWeight
        return score
    }

    private fun blotPenalty(board: Array<BoardField>, field: Int, owner: Int): Double {
        val opponent = Player.opponent(owner)
        val real = PositionMapper.toReal(field, owner)
        var penalty = difficulty.blotPenalty
        for (i in 0 until 26) {
            if (board[i].owner != opponent || board[i].chipCount <= 0) continue
            val oppReal = PositionMapper.toReal(i, opponent)
            val oppRealAgainstOwner = 25 - oppReal
            val distance = real - oppRealAgainstOwner
            if (distance in 1..6) {
                penalty += (7 - distance) * difficulty.directShotPenalty
            }
        }
        return penalty
    }

    private fun normalizedReal(field: Int, owner: Int): Int {
        val real = PositionMapper.toReal(field, owner)
        return if (real == 100) 25 else real
    }

    private fun madePointRun(board: Array<BoardField>, player: Int): Int {
        var best = 0
        var current = 0
        for (real in 1..24) {
            val field = PositionMapper.toMatrix(real, player)
            if (board[field].owner == player && board[field].chipCount >= 2) {
                current++
                best = max(best, current)
            } else {
                current = 0
            }
        }
        return best
    }

    private class SearchBudget(private var remaining: Int) {
        fun tryVisit(): Boolean {
            if (remaining <= 0) return false
            remaining--
            return true
        }
    }

    companion object {
        private val ROLL_OUTCOMES: Array<RollOutcome> = buildRollOutcomes()

        private fun buildRollOutcomes(): Array<RollOutcome> {
            val outcomes = mutableListOf<RollOutcome>()
            for (first in 1..6) {
                for (second in first..6) {
                    val prob = if (first == second) 1.0 / 36.0 else 2.0 / 36.0
                    outcomes.add(RollOutcome(first, second, prob))
                }
            }
            return outcomes.toTypedArray()
        }
    }

    private data class RollOutcome(val first: Int, val second: Int, val probability: Double)
}
