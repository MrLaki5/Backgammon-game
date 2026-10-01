package com.royalbackgammon.core

import com.royalbackgammon.core.dice.RandomDiceRoller
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.scoring.MatchState
import com.royalbackgammon.core.variant.Variant
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class FevgaRulesTest {

    private val v = Variant.FEVGA
    private val whiteHead = BackgammonRules.headIndex(Player.WHITE, v)

    private fun m(real: Int, player: Int) = PositionMapper.toMatrix(real, player, v)

    private fun dice(a: Int, b: Int): Array<Die> =
        if (a == b) arrayOf(Die(a), Die(a), Die(a), Die(a))
        else arrayOf(Die(a), Die(b), Die(0, used = true), Die(0, used = true))

    private fun startBoard(): Array<BoardField> = GameState.newGame(v).board

    @Test
    fun `fevga starts like nardy on opposite corners`() {
        val b = startBoard()
        assertEquals(15, b[whiteHead].chipCount)
        assertEquals(15, b[BackgammonRules.headIndex(Player.RED, v)].chipCount)
    }

    @Test
    fun `only the lead checker moves until it passes the opponent start`() {
        val b = startBoard()
        // Lead checker on own real 5, still short of the opponent's start (own real 13)
        b[whiteHead].chipCount = 14
        b[m(5, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(4, 2), v)
        assertTrue(moves.isNotEmpty())
        assertTrue("only the lead checker may move", moves.all { it.from == m(5, Player.WHITE) })
    }

    @Test
    fun `a checker exactly on the opponent start has not passed it`() {
        val b = startBoard()
        b[whiteHead].chipCount = 14
        b[BackgammonRules.headIndex(Player.RED, v)].chipCount = 0
        b[BackgammonRules.headIndex(Player.RED, v)].owner = Player.NONE
        b[m(13, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(3, 2), v)
        assertTrue(moves.all { it.from == m(13, Player.WHITE) })
    }

    @Test
    fun `once the lead is past the opponent start every checker may move`() {
        val b = startBoard()
        b[whiteHead].chipCount = 14
        b[m(14, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(4, 2), v)
        assertTrue("head checkers are free", moves.any { it.from == whiteHead })
        assertTrue("the lead may still move", moves.any { it.from == m(14, Player.WHITE) })
    }

    @Test
    fun `fevga has no one-per-turn head limit`() {
        val b = startBoard()
        b[whiteHead].chipCount = 13
        b[m(14, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }
        b[m(3, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }
        val state = GameState(board = b, dice = dice(2, 2), currentPlayer = Player.WHITE,
            turnState = GameState.STATE_MOVE, variant = v)
        val engine = GameEngine(state)

        val first = engine.legalMoves.first { it.from == whiteHead }
        engine.makeMove(first.from, first.to)
        assertTrue("a second checker may leave the head in the same turn",
            engine.legalMoves.any { it.from == whiteHead })
    }

    @Test
    fun `fevga scores mars as two and never backgammon`() {
        val b = Array(28) { BoardField() }
        b[GameState.WHITE_BEAR_OFF].let { it.chipCount = 15; it.owner = Player.WHITE }
        b[m(1, Player.RED)].let { it.chipCount = 15; it.owner = Player.RED }
        val r = BackgammonRules.gameResult(b, v)!!
        assertEquals(WinType.GAMMON, r.winType)
        assertEquals(2, r.points)
    }

    @Test
    fun `random fevga games finish and respect the lead rule`() {
        repeat(30) { seed ->
            val rnd = Random(seed)
            val engine = GameEngine(GameState.newGame(v), RandomDiceRoller(Random(seed * 13 + 5)))
            var turns = 0
            while (!engine.isGameOver()) {
                assertTrue("seed $seed too long", turns++ < 3000)
                if (engine.state.turnState != GameState.STATE_MOVE) { engine.rollDice(); continue }
                while (engine.legalMoves.isNotEmpty() && !engine.isGameOver()) {
                    val move = engine.legalMoves[rnd.nextInt(engine.legalMoves.size)]
                    val player = engine.state.currentPlayer
                    val leadReal = (0 until 24).filter {
                        engine.state.board[it].owner == player && engine.state.board[it].chipCount > 0
                    }.maxOf { PositionMapper.toReal(it, player, v) }
                    if (leadReal <= 13) {
                        assertEquals("seed $seed: only the lead checker may move",
                            leadReal, PositionMapper.toReal(move.from, player, v))
                    }
                    val dst = engine.state.board[move.to]
                    assertTrue("seed $seed: landed on the opponent",
                        move.to >= 24 || dst.chipCount == 0 || dst.owner == player)
                    engine.makeMove(move.from, move.to)
                    assertEquals(30, engine.state.board.sumOf { it.chipCount })
                }
                if (!engine.isGameOver()) engine.endTurn()
            }
            assertNotNull(engine.result)
        }
    }

    @Test
    fun `tavli rotates portes plakoto fevga`() {
        val match = MatchState(Variant.PORTES, Variant.TAVLI_ROTATION, targetPoints = 7)
        assertEquals(Variant.PORTES, match.currentVariant())
        match.record(GameResult(Player.WHITE, WinType.SINGLE, 1))
        assertEquals(Variant.PLAKOTO, match.currentVariant())
        match.record(GameResult(Player.RED, WinType.GAMMON, 2))
        assertEquals(Variant.FEVGA, match.currentVariant())
        match.record(GameResult(Player.WHITE, WinType.SINGLE, 1))
        assertEquals(Variant.PORTES, match.currentVariant())
        assertEquals(2, match.whiteScore)
        assertEquals(2, match.redScore)
    }
}
