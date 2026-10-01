package com.royalbackgammon.core

import com.royalbackgammon.core.dice.RandomDiceRoller
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.variant.Variant
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class NardyRulesTest {

    private val v = Variant.NARDY
    private val whiteHead = BackgammonRules.headIndex(Player.WHITE, v)
    private val redHead = BackgammonRules.headIndex(Player.RED, v)

    private fun emptyBoard() = Array(28) { BoardField() }

    private fun put(board: Array<BoardField>, player: Int, real: Int, count: Int) {
        val idx = PositionMapper.toMatrix(real, player, v)
        board[idx].chipCount = count
        board[idx].owner = player
    }

    private fun dice(a: Int, b: Int): Array<Die> =
        if (a == b) arrayOf(Die(a), Die(a), Die(a), Die(a))
        else arrayOf(Die(a), Die(b), Die(0, used = true), Die(0, used = true))

    private fun matrix(real: Int, player: Int) = PositionMapper.toMatrix(real, player, v)

    // ---- Geometry ----

    @Test
    fun `heads sit on diagonally opposite corners`() {
        assertEquals(11, whiteHead)
        assertEquals(12, redHead)
        val state = GameState.newGame(v)
        assertEquals(15, state.board[whiteHead].chipCount)
        assertEquals(15, state.board[redHead].chipCount)
        assertEquals(30, state.board.sumOf { it.chipCount })
    }

    @Test
    fun `red path is white path rotated by twelve`() {
        for (real in 1..24) {
            val m = matrix(real, Player.RED)
            assertEquals(real, PositionMapper.toReal(m, Player.RED, v))
            val whiteReal = PositionMapper.toReal(m, Player.WHITE, v)
            assertEquals((real + 11) % 24 + 1, whiteReal)
        }
        assertEquals(setOf(0, 1, 2, 3, 4, 5), (19..24).map { matrix(it, Player.RED) }.toSet())
    }

    // ---- Head rule ----

    @Test
    fun `only one checker leaves the head per turn`() {
        val state = GameState.newGame(v)
        val first = BackgammonRules.calculateLegalMoves(
            state.board, Player.WHITE, dice(5, 3), v, TurnContext(0, firstTurn = true)
        )
        assertTrue(first.isNotEmpty())
        assertTrue(first.all { it.from == whiteHead })

        val afterOne = BackgammonRules.calculateLegalMoves(
            state.board, Player.WHITE, dice(5, 3), v, TurnContext(1, firstTurn = true)
        )
        assertTrue(afterOne.none { it.from == whiteHead })
    }

    @Test
    fun `first turn 6-6 lets a second checker leave the head`() {
        val b = GameState.newGame(v).board
        b[whiteHead].chipCount = 14
        put(b, Player.WHITE, 7, 1)
        val d = arrayOf(Die(6, used = true), Die(6), Die(6), Die(6))

        val firstTurn = BackgammonRules.calculateLegalMoves(b, Player.WHITE, d, v, TurnContext(1, true))
        assertNotNull(firstTurn.find { it.from == whiteHead })

        val laterTurn = BackgammonRules.calculateLegalMoves(b, Player.WHITE, d, v, TurnContext(1, false))
        assertNull(laterTurn.find { it.from == whiteHead })
    }

    @Test
    fun `first turn 5-5 allows only one checker off the head`() {
        val b = GameState.newGame(v).board
        b[whiteHead].chipCount = 14
        put(b, Player.WHITE, 6, 1)
        val d = arrayOf(Die(5, used = true), Die(5), Die(5), Die(5))
        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, d, v, TurnContext(1, true))
        assertNull(moves.find { it.from == whiteHead })
    }

    @Test
    fun `engine counts head moves and resets them each turn`() {
        val engine = GameEngine(GameState.newGame(v), FixedTurnRoller(listOf(6, 2), listOf(5, 3), listOf(4, 1)))
        engine.rollDice(); engine.rollDice(); engine.rollDice()   // opening 6 vs 2, White rerolls 5-3
        assertEquals(Player.WHITE, engine.state.currentPlayer)
        val m = engine.legalMoves.first { it.from == whiteHead }
        engine.makeMove(m.from, m.to)
        assertEquals(1, engine.state.headMovesThisTurn)
        assertTrue(engine.legalMoves.none { it.from == whiteHead })
        while (engine.legalMoves.isNotEmpty()) engine.makeMove(engine.legalMoves[0].from, engine.legalMoves[0].to)
        engine.endTurn()
        assertEquals(0, engine.state.headMovesThisTurn)
        assertEquals(1, engine.state.turnsPlayed)
    }

    // ---- Contact ----

    @Test
    fun `a single opposing checker blocks the point`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 5, 1)
        put(b, Player.WHITE, 1, 14)
        b[matrix(8, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.RED }
        put(b, Player.RED, 1, 14)

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(3, 3), v, TurnContext(1, false))
        assertNull(moves.find { it.to == matrix(8, Player.WHITE) })
    }

    // ---- Six-point block ----

    private fun primeBoard(redAhead: Boolean): Array<BoardField> {
        val b = emptyBoard()
        // White owns red reals 5..9; a checker on red real 4 can complete the block on red real 10
        for (redReal in 5..9) b[matrix(redReal, Player.RED)].let { it.chipCount = 1; it.owner = Player.WHITE }
        b[matrix(4, Player.RED)].let { it.chipCount = 1; it.owner = Player.WHITE }
        put(b, Player.WHITE, 1, 15 - 6)
        put(b, Player.RED, 1, if (redAhead) 14 else 15)
        if (redAhead) b[matrix(15, Player.RED)].let { it.chipCount = 1; it.owner = Player.RED }
        return b
    }

    @Test
    fun `six-point block trapping every opponent checker is illegal`() {
        val b = primeBoard(redAhead = false)
        val target = matrix(10, Player.RED)
        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(6, 6), v, TurnContext(1, false))
        assertNull(moves.find { it.from == matrix(4, Player.RED) && it.to == target })
    }

    @Test
    fun `six-point block is legal when an opponent checker is past it`() {
        val b = primeBoard(redAhead = true)
        val target = matrix(10, Player.RED)
        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(6, 6), v, TurnContext(1, false))
        assertNotNull(moves.find { it.from == matrix(4, Player.RED) && it.to == target })
    }

    // ---- Bear-off and scoring ----

    @Test
    fun `red bears off from its own home quadrant`() {
        val b = emptyBoard()
        put(b, Player.RED, 20, 5); put(b, Player.RED, 24, 10)
        put(b, Player.WHITE, 3, 15)
        assertEquals(GamePhase.BEARING_OFF, BackgammonRules.gamePhase(b, Player.RED, v))
        val moves = BackgammonRules.calculateLegalMoves(b, Player.RED, dice(6, 1), v, TurnContext.NONE)
        assertNotNull(moves.find { it.from == matrix(20, Player.RED) && it.to == GameState.RED_BEAR_OFF && it.dieValue == 6 })
        assertNotNull(moves.find { it.from == matrix(24, Player.RED) && it.to == GameState.RED_BEAR_OFF && it.dieValue == 1 })
    }

    @Test
    fun `nardy scores mars as 2 and never backgammon`() {
        val b = emptyBoard()
        b[GameState.WHITE_BEAR_OFF].let { it.chipCount = 15; it.owner = Player.WHITE }
        put(b, Player.RED, 1, 15)   // still on the head
        val r = BackgammonRules.gameResult(b, v)!!
        assertEquals(WinType.GAMMON, r.winType)
        assertEquals(2, r.points)

        b[GameState.RED_BEAR_OFF].let { it.chipCount = 1; it.owner = Player.RED }
        b[matrix(1, Player.RED)].chipCount = 14
        assertEquals(1, BackgammonRules.gameResult(b, v)!!.points)
    }

    // ---- Full random games keep every invariant ----

    @Test
    fun `random nardy games finish and respect all rules`() {
        repeat(40) { seed -> playRandomGame(Variant.NARDY, seed) }
    }

    @Test
    fun `random standard games still finish`() {
        repeat(20) { seed -> playRandomGame(Variant.STANDARD, seed) }
    }

    private fun playRandomGame(variant: Variant, seed: Int) {
        val rnd = Random(seed)
        val engine = GameEngine(GameState.newGame(variant), RandomDiceRoller(Random(seed * 31 + 7)))
        var turns = 0
        while (!engine.isGameOver()) {
            assertTrue("seed $seed: game too long", turns < 2000)
            if (engine.state.turnState != GameState.STATE_MOVE) {
                engine.rollDice()
                continue
            }
            var headMoves = 0
            val headLimit = if (engine.state.turnsPlayed < 2 && engine.state.dice.all { it.value == engine.state.dice[0].value } &&
                engine.state.dice[0].value in setOf(3, 4, 6)) 2 else 1
            while (engine.legalMoves.isNotEmpty()) {
                val move = engine.legalMoves[rnd.nextInt(engine.legalMoves.size)]
                if (variant == Variant.NARDY) {
                    val dst = engine.state.board[move.to]
                    assertTrue("seed $seed: landed on opponent", move.to >= 24 || dst.chipCount == 0 || dst.owner == engine.state.currentPlayer)
                    if (move.from == BackgammonRules.headIndex(engine.state.currentPlayer, variant)) headMoves++
                    assertTrue("seed $seed: head rule", headMoves <= headLimit)
                }
                engine.makeMove(move.from, move.to)
                assertEquals(30, engine.state.board.sumOf { it.chipCount })
                if (engine.isGameOver()) break
            }
            if (!engine.isGameOver()) engine.endTurn()
            turns++
        }
        val result = engine.result!!
        assertEquals(engine.state.winner, result.winner)
        assertEquals(15, engine.state.board[GameState.bearOffIndex(result.winner)].chipCount)
    }

    /** Opening single dice from [opening], then full-turn pairs from [turns]. */
    private class FixedTurnRoller(private val opening: List<Int>, vararg turns: List<Int>) :
        com.royalbackgammon.core.dice.DiceRoller {
        private val values = ArrayDeque(opening + turns.flatMap { it })
        override fun rollOne(): Int = values.removeFirst()
    }
}
