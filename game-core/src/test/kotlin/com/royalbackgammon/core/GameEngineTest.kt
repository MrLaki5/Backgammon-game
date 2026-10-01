package com.royalbackgammon.core

import com.royalbackgammon.core.dice.FixedDiceRoller
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.model.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun `initial roll phase determines who goes first`() {
        // Player 1 rolls 3, Player 2 rolls 5 → Red goes first
        val roller = FixedDiceRoller(3, 5)
        val engine = GameEngine(GameState.newGame(), roller)

        assertEquals(GameState.STATE_INITIAL_ROLL_P1, engine.state.turnState)

        engine.rollDice()  // P1 rolls 3
        assertEquals(GameState.STATE_INITIAL_ROLL_P2, engine.state.turnState)

        engine.rollDice()  // P2 rolls 5
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertEquals(Player.RED, engine.state.currentPlayer)  // Red has higher roll
    }

    @Test
    fun `initial roll with P1 higher means white goes first`() {
        val roller = FixedDiceRoller(6, 2)
        val engine = GameEngine(GameState.newGame(), roller)

        engine.rollDice()  // P1 rolls 6
        engine.rollDice()  // P2 rolls 2

        assertEquals(Player.WHITE, engine.state.currentPlayer)
    }

    @Test
    fun `makeMove applies a valid move`() {
        val roller = FixedDiceRoller(5, 3)
        val engine = GameEngine(GameState.newGame(), roller)

        engine.rollDice()  // P1 rolls 5
        engine.rollDice()  // P2 rolls 3 → White goes first (5 > 3)

        assertTrue(engine.legalMoves.isNotEmpty())
        val move = engine.legalMoves[0]
        val result = engine.makeMove(move.from, move.to)
        assertTrue(result.applied)
    }

    @Test
    fun `makeMove rejects illegal destination`() {
        val roller = FixedDiceRoller(5, 3)
        val engine = GameEngine(GameState.newGame(), roller)

        engine.rollDice()
        engine.rollDice()

        // Try a clearly illegal move
        val result = engine.makeMove(0, 1)
        assertFalse(result.applied)
    }

    @Test
    fun `endTurn switches player and sets roll state`() {
        val roller = FixedDiceRoller(5, 3, 6, 1)
        val engine = GameEngine(GameState.newGame(), roller)

        engine.rollDice()  // initial P1
        engine.rollDice()  // initial P2, White plays

        assertEquals(Player.WHITE, engine.state.currentPlayer)
        engine.endTurn()
        assertEquals(Player.RED, engine.state.currentPlayer)
        assertEquals(GameState.STATE_ROLL, engine.state.turnState)
    }

    @Test
    fun `full turn cycle - roll, move all, end turn`() {
        val roller = FixedDiceRoller(6, 1, 3, 2)
        val engine = GameEngine(GameState.newGame(), roller)

        // Initial rolls
        engine.rollDice()  // P1: 6
        engine.rollDice()  // P2: 1, White starts

        assertEquals(Player.WHITE, engine.state.currentPlayer)
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertTrue(engine.legalMoves.isNotEmpty())

        // Make moves until none left
        var moveCount = 0
        while (engine.legalMoves.isNotEmpty() && moveCount < 10) {
            val move = engine.legalMoves[0]
            engine.makeMove(move.from, move.to)
            moveCount++
        }

        assertTrue(engine.isTurnComplete())
        engine.endTurn()

        assertEquals(Player.RED, engine.state.currentPlayer)
        assertEquals(GameState.STATE_ROLL, engine.state.turnState)

        // Red rolls
        engine.rollDice()  // 3, 2
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertTrue(engine.legalMoves.isNotEmpty())
    }

    @Test
    fun `game detects winner`() {
        // Set up a state where White is about to bear off last checker
        val board = Array(28) { BoardField() }
        board[GameState.WHITE_BEAR_OFF].chipCount = 14; board[GameState.WHITE_BEAR_OFF].owner = Player.WHITE
        board[23].chipCount = 1; board[23].owner = Player.WHITE  // real 24, one left
        board[0].chipCount = 15; board[0].owner = Player.RED

        val state = GameState(
            board = board,
            dice = arrayOf(Die(1), Die(0, used = true), Die(0, used = true), Die(0, used = true)),
            currentPlayer = Player.WHITE,
            turnState = GameState.STATE_MOVE
        )

        val engine = GameEngine(state, FixedDiceRoller(1))

        // Calculate legal moves manually since we set up mid-turn
        val move = engine.legalMoves.find { it.from == 23 && it.to == GameState.WHITE_BEAR_OFF }
        assertNotNull("Should find a bear-off move", move)

        engine.makeMove(move!!.from, move.to)
        assertTrue(engine.isGameOver())
        assertEquals(Player.WHITE, engine.state.winner)
    }

    @Test
    fun `doubles dice provides 4 moves`() {
        val roller = FixedDiceRoller(4, 3, 4, 4)  // initial: 4,3 → White; then doubles: 4,4
        val engine = GameEngine(GameState.newGame(), roller)

        engine.rollDice()  // P1: 4
        engine.rollDice()  // P2: 3, White starts

        // Use all moves for initial turn
        while (engine.legalMoves.isNotEmpty()) {
            val move = engine.legalMoves[0]
            engine.makeMove(move.from, move.to)
        }
        engine.endTurn()

        // Red rolls: next two values from roller are 4, 4 → doubles
        engine.rollDice()

        // With doubles, all 4 dice should have value 4
        val activeDice = engine.state.dice.filter { !it.used && it.value > 0 }
        assertEquals(4, activeDice.size)
        assertTrue(activeDice.all { it.value == 4 })
    }
}
