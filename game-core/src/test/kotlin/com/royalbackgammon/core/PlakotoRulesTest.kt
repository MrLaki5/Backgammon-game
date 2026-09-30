package com.royalbackgammon.core

import com.royalbackgammon.core.dice.RandomDiceRoller
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.logic.MoveExecutor
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.variant.Variant
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PlakotoRulesTest {

    private val v = Variant.PLAKOTO

    private fun emptyBoard() = Array(28) { BoardField() }

    private fun m(real: Int, player: Int) = PositionMapper.toMatrix(real, player, v)

    private fun put(board: Array<BoardField>, player: Int, real: Int, count: Int) {
        board[m(real, player)].chipCount = count
        board[m(real, player)].owner = player
    }

    private fun dice(a: Int, b: Int): Array<Die> =
        if (a == b) arrayOf(Die(a), Die(a), Die(a), Die(a))
        else arrayOf(Die(a), Die(b), Die(0, used = true), Die(0, used = true))

    private fun state(board: Array<BoardField>, player: Int, d: Array<Die>) =
        GameState(board = board, dice = d, currentPlayer = player, turnState = GameState.STATE_MOVE, variant = v)

    private fun total(board: Array<BoardField>, player: Int) = board.sumOf { it.checkersOf(player) }

    @Test
    fun `all fifteen start on opposite corners`() {
        val b = GameState.newGame(v).board
        assertEquals(15, b[11].checkersOf(Player.WHITE))
        assertEquals(15, b[23].checkersOf(Player.RED))
        assertEquals(1, PositionMapper.toReal(11, Player.WHITE, v))
        assertEquals(1, PositionMapper.toReal(23, Player.RED, v))
    }

    @Test
    fun `landing on a lone opposing checker pins it`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 5, 1); put(b, Player.WHITE, 1, 14)
        b[m(8, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.RED }
        put(b, Player.RED, 1, 14)
        val s = state(b, Player.WHITE, dice(3, 1))

        val move = BackgammonRules.calculateLegalMoves(b, Player.WHITE, s.dice, v)
            .first { it.from == m(5, Player.WHITE) && it.to == m(8, Player.WHITE) }
        val result = MoveExecutor.applyMove(s, move)

        assertFalse(result.hit)
        val target = s.board[m(8, Player.WHITE)]
        assertEquals(Player.WHITE, target.owner)
        assertEquals(1, target.chipCount)
        assertEquals(Player.RED, target.pinned)
        assertEquals(0, s.board[GameState.RED_BAR].chipCount)
        assertEquals(15, total(s.board, Player.RED))
        assertEquals(15, total(s.board, Player.WHITE))
    }

    @Test
    fun `pinned checker cannot move and a pinning checker cannot be pinned`() {
        val b = emptyBoard()
        // Red real 10 holds a white checker pinning a red one
        b[m(10, Player.RED)].let { it.chipCount = 1; it.owner = Player.WHITE; it.pinned = Player.RED }
        put(b, Player.RED, 1, 14)
        put(b, Player.WHITE, 1, 14)
        // A red checker two points behind could only land on the pinning white checker
        put(b, Player.RED, 8, 1)
        b[m(1, Player.RED)].chipCount = 13

        val redMoves = BackgammonRules.calculateLegalMoves(b, Player.RED, dice(2, 1), v)
        assertNull("pinned red checker is frozen", redMoves.find { it.from == m(10, Player.RED) })
        assertNull("cannot land on a pinning checker", redMoves.find { it.to == m(10, Player.RED) })
    }

    @Test
    fun `two opposing checkers block the point`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 5, 1); put(b, Player.WHITE, 1, 14)
        b[m(8, Player.WHITE)].let { it.chipCount = 2; it.owner = Player.RED }
        put(b, Player.RED, 1, 13)
        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(3, 1), v)
        assertNull(moves.find { it.to == m(8, Player.WHITE) })
    }

    @Test
    fun `moving the pinning checker away releases the pinned one`() {
        val b = emptyBoard()
        b[m(8, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE; it.pinned = Player.RED }
        put(b, Player.WHITE, 1, 14)
        put(b, Player.RED, 1, 14)
        val s = state(b, Player.WHITE, dice(2, 1))
        MoveExecutor.applyMove(s, Move(2, m(8, Player.WHITE), m(10, Player.WHITE)))
        val released = s.board[m(8, Player.WHITE)]
        assertEquals(Player.RED, released.owner)
        assertEquals(1, released.chipCount)
        assertEquals(Player.NONE, released.pinned)
    }

    @Test
    fun `own pinned checker outside home prevents bearing off`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 20, 14)
        b[m(10, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.RED; it.pinned = Player.WHITE }
        put(b, Player.RED, 20, 14)
        assertEquals(GamePhase.PLAYING, BackgammonRules.gamePhase(b, Player.WHITE, v))
    }

    // ---- Mother rule ----

    private fun motherBoard(pinnerStillAtStart: Boolean): Array<BoardField> {
        val b = emptyBoard()
        // Red's mother (last checker on red real 1) is pinned by White
        b[m(1, Player.RED)].let { it.chipCount = 1; it.owner = Player.WHITE; it.pinned = Player.RED }
        put(b, Player.RED, 10, 14)
        if (pinnerStillAtStart) {
            put(b, Player.WHITE, 1, 2); put(b, Player.WHITE, 15, 12)
        } else {
            put(b, Player.WHITE, 15, 14)
        }
        return b
    }

    @Test
    fun `pinning the opposing mother wins double`() {
        val r = BackgammonRules.gameResult(motherBoard(pinnerStillAtStart = false), v)
        assertEquals(GameResult(Player.WHITE, WinType.MOTHER_PINNED, 2), r)
    }

    @Test
    fun `mother rule is waived while the pinner still has its own mother at start`() {
        assertNull(BackgammonRules.gameResult(motherBoard(pinnerStillAtStart = true), v))
    }

    @Test
    fun `engine ends the game on a mother pin`() {
        val b = emptyBoard()
        put(b, Player.RED, 1, 1); put(b, Player.RED, 10, 14)
        put(b, Player.WHITE, 21, 1); put(b, Player.WHITE, 15, 14)
        val engine = GameEngine(state(b, Player.WHITE, dice(3, 1)))
        val pin = engine.legalMoves.first { it.to == m(1, Player.RED) }
        engine.makeMove(pin.from, pin.to)
        assertTrue(engine.isGameOver())
        assertEquals(WinType.MOTHER_PINNED, engine.result!!.winType)
    }

    @Test
    fun `both mothers pinned is a draw`() {
        val b = emptyBoard()
        b[m(1, Player.RED)].let { it.chipCount = 14; it.owner = Player.WHITE; it.pinned = Player.RED }
        b[m(1, Player.WHITE)].let { it.chipCount = 14; it.owner = Player.RED; it.pinned = Player.WHITE }
        val r = BackgammonRules.gameResult(b, v)
        assertEquals(GameResult(Player.NONE, WinType.DRAW, 0), r)
        val engine = GameEngine(state(b, Player.WHITE, dice(6, 5)))
        assertTrue(engine.isGameOver())
    }

    @Test
    fun `plakoto never scores backgammon`() {
        val b = emptyBoard()
        b[GameState.WHITE_BEAR_OFF].let { it.chipCount = 15; it.owner = Player.WHITE }
        put(b, Player.RED, 3, 15)
        assertEquals(GameResult(Player.WHITE, WinType.GAMMON, 2), BackgammonRules.gameResult(b, v))
    }

    // ---- Random full games ----

    @Test
    fun `random plakoto games finish and conserve checkers`() {
        repeat(40) { seed ->
            val rnd = Random(seed)
            val engine = GameEngine(GameState.newGame(v), RandomDiceRoller(Random(seed * 17 + 3)))
            var turns = 0
            while (!engine.isGameOver()) {
                if (turns++ >= 3000) {
                    val b = engine.state.board
                    fail("seed $seed too long: " + (0 until 28).joinToString(" ") { i -> "$i:${b[i].chipCount}/${b[i].owner}/${b[i].pinned}" } +
                        " white=" + (0 until 24).filter { b[it].checkersOf(1) > 0 }.map { PositionMapper.toReal(it, 1, v) } +
                        " red=" + (0 until 24).filter { b[it].checkersOf(2) > 0 }.map { PositionMapper.toReal(it, 2, v) })
                }
                if (engine.state.turnState != GameState.STATE_MOVE) { engine.rollDice(); continue }
                while (engine.legalMoves.isNotEmpty() && !engine.isGameOver()) {
                    val move = engine.legalMoves[rnd.nextInt(engine.legalMoves.size)]
                    assertTrue("seed $seed moved a pinned checker",
                        engine.state.board[move.from].owner == engine.state.currentPlayer)
                    engine.makeMove(move.from, move.to)
                    assertEquals(15, total(engine.state.board, Player.WHITE))
                    assertEquals(15, total(engine.state.board, Player.RED))
                    assertEquals(0, engine.state.board[GameState.WHITE_BAR].chipCount + engine.state.board[GameState.RED_BAR].chipCount)
                }
                if (!engine.isGameOver()) engine.endTurn()
            }
            assertNotNull(engine.result)
        }
    }
}
