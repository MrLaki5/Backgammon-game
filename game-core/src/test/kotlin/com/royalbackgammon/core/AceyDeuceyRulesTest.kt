package com.royalbackgammon.core

import com.royalbackgammon.core.dice.DiceRoller
import com.royalbackgammon.core.dice.RandomDiceRoller
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.variant.Variant
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class AceyDeuceyRulesTest {

    private val v = Variant.ACEY_DEUCEY

    private fun m(real: Int, player: Int) = PositionMapper.toMatrix(real, player, v)

    private fun dice(a: Int, b: Int): Array<Die> =
        if (a == b) arrayOf(Die(a), Die(a), Die(a), Die(a))
        else arrayOf(Die(a), Die(b), Die(0, used = true), Die(0, used = true))

    private fun state(board: Array<BoardField>, dice: Array<Die>, player: Int = Player.WHITE) =
        GameState(board = board, dice = dice, currentPlayer = player,
            turnState = GameState.STATE_MOVE, variant = v)

    @Test
    fun `every checker starts on the bar`() {
        val s = GameState.newGame(v)
        assertEquals(15, s.board[GameState.WHITE_BAR].chipCount)
        assertEquals(15, s.board[GameState.RED_BAR].chipCount)
        assertEquals(GamePhase.PLAYING, BackgammonRules.gamePhase(s.board, Player.WHITE, v))
    }

    @Test
    fun `checkers enter from the bar and entering comes first`() {
        val s = GameState.newGame(v)
        val moves = BackgammonRules.calculateLegalMoves(s.board, Player.WHITE, dice(3, 5), v)
        assertTrue(moves.isNotEmpty())
        assertTrue(moves.all { it.from == GameState.WHITE_BAR })
        assertTrue(moves.any { it.to == m(3, Player.WHITE) })
        assertTrue(moves.any { it.to == m(5, Player.WHITE) })
    }

    @Test
    fun `a checker may move on before the rest have entered`() {
        val b = Array(28) { BoardField() }
        b[GameState.WHITE_BAR].let { it.chipCount = 14; it.owner = Player.WHITE }
        b[m(3, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }
        b[GameState.RED_BAR].let { it.chipCount = 15; it.owner = Player.RED }

        val s = state(b, dice(4, 2))
        val engine = GameEngine(s)
        // The bar is still first, but after entering, the entered checker can be played on
        val enter = engine.legalMoves.first { it.from == GameState.WHITE_BAR }
        engine.makeMove(enter.from, enter.to)
        assertTrue(engine.legalMoves.any { it.from != GameState.WHITE_BAR })
    }

    @Test
    fun `a hit checker must enter before anything else moves`() {
        val b = Array(28) { BoardField() }
        b[GameState.WHITE_BAR].let { it.chipCount = 1; it.owner = Player.WHITE }
        b[m(8, Player.WHITE)].let { it.chipCount = 14; it.owner = Player.WHITE }
        b[m(20, Player.RED)].let { it.chipCount = 15; it.owner = Player.RED }

        val free = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(4, 2), v,
            TurnContext(hitCheckersOnBar = 0))
        assertTrue("an unentered checker does not block the rest",
            free.any { it.from == m(8, Player.WHITE) })

        val forced = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(4, 2), v,
            TurnContext(hitCheckersOnBar = 1))
        assertTrue("a hit checker must come in first",
            forced.all { it.from == GameState.WHITE_BAR })
    }

    @Test
    fun `hitting still sends a checker back to the bar`() {
        val b = Array(28) { BoardField() }
        b[m(3, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.WHITE }
        b[m(6, Player.WHITE)].let { it.chipCount = 1; it.owner = Player.RED }
        b[m(20, Player.WHITE)].let { it.chipCount = 14; it.owner = Player.WHITE }
        b[m(20, Player.RED)].let { it.chipCount = 14; it.owner = Player.RED }

        val engine = GameEngine(state(b, dice(3, 1)))
        val hit = engine.legalMoves.first { it.from == m(3, Player.WHITE) && it.to == m(6, Player.WHITE) }
        engine.makeMove(hit.from, hit.to)
        assertEquals(1, engine.state.board[GameState.RED_BAR].chipCount)
        assertEquals(1, engine.state.hitsOnBar(Player.RED))
    }

    @Test
    fun `rolling one-two adds a named double and another roll`() {
        val b = Array(28) { BoardField() }
        b[m(1, Player.WHITE)].let { it.chipCount = 15; it.owner = Player.WHITE }
        b[m(1, Player.RED)].let { it.chipCount = 15; it.owner = Player.RED }
        val s = GameState(board = b, dice = dice(0, 0), currentPlayer = Player.WHITE,
            turnState = GameState.STATE_ROLL, variant = v)
        val engine = GameEngine(s, FixedRoller(listOf(1, 2, 4, 4)))

        engine.rollDice()
        assertTrue(engine.state.bonusDoublePending)

        while (engine.legalMoves.isNotEmpty()) {
            engine.makeMove(engine.legalMoves[0].from, engine.legalMoves[0].to)
        }
        assertTrue("the named double is still owed", engine.needsBonusDouble())

        engine.playBonusDouble(5)
        assertEquals(4, engine.state.dice.count { !it.used && it.value == 5 })
        assertFalse(engine.needsBonusDouble())

        while (engine.legalMoves.isNotEmpty()) {
            engine.makeMove(engine.legalMoves[0].from, engine.legalMoves[0].to)
        }
        engine.endTurn()
        assertEquals("the same player rolls again", Player.WHITE, engine.state.currentPlayer)
        assertEquals(GameState.STATE_ROLL, engine.state.turnState)
    }

    @Test
    fun `plain doubles do not grant an extra roll`() {
        val b = GameState.newGame(v).board
        val s = GameState(board = b, dice = dice(0, 0), currentPlayer = Player.WHITE,
            turnState = GameState.STATE_ROLL, variant = v)
        val engine = GameEngine(s, FixedRoller(listOf(4, 4)))
        engine.rollDice()
        assertFalse(engine.state.extraTurnPending)
        while (engine.legalMoves.isNotEmpty()) {
            engine.makeMove(engine.legalMoves[0].from, engine.legalMoves[0].to)
        }
        engine.endTurn()
        assertEquals(Player.RED, engine.state.currentPlayer)
    }

    @Test
    fun `the loser pays one point per checker left on the board`() {
        val b = Array(28) { BoardField() }
        b[GameState.WHITE_BEAR_OFF].let { it.chipCount = 15; it.owner = Player.WHITE }
        b[GameState.RED_BEAR_OFF].let { it.chipCount = 11; it.owner = Player.RED }
        b[m(20, Player.RED)].let { it.chipCount = 4; it.owner = Player.RED }

        val result = BackgammonRules.gameResult(b, v)!!
        assertEquals(Player.WHITE, result.winner)
        assertEquals(WinType.SINGLE, result.winType)
        assertEquals(4, result.points)

        b[GameState.RED_BEAR_OFF].chipCount = 0
        b[m(20, Player.RED)].chipCount = 15
        assertEquals(15, BackgammonRules.gameResult(b, v)!!.points)
    }

    @Test
    fun `random acey-deucey games finish`() {
        repeat(20) { seed ->
            val rnd = Random(seed)
            val engine = GameEngine(GameState.newGame(v), RandomDiceRoller(Random(seed * 23 + 11)))
            var turns = 0
            while (!engine.isGameOver()) {
                assertTrue("seed $seed too long", turns++ < 4000)
                if (engine.state.turnState != GameState.STATE_MOVE) { engine.rollDice(); continue }
                while (engine.legalMoves.isNotEmpty() && !engine.isGameOver()) {
                    val move = engine.legalMoves[rnd.nextInt(engine.legalMoves.size)]
                    engine.makeMove(move.from, move.to)
                }
                if (engine.isGameOver()) break
                if (engine.needsBonusDouble()) {
                    engine.playBonusDouble(rnd.nextInt(1, 7))
                    continue
                }
                engine.endTurn()
            }
            val result = engine.result!!
            assertTrue(result.points in 1..15)
        }
    }

    private class FixedRoller(values: List<Int>) : DiceRoller {
        private val queue = ArrayDeque(values)
        override fun rollOne(): Int = if (queue.isEmpty()) 3 else queue.removeFirst()
    }
}
