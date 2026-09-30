package com.royalbackgammon.core

import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import org.junit.Assert.*
import org.junit.Test

class BackgammonRulesTest {

    @Test
    fun `new game is in PLAYING phase for both players`() {
        val state = GameState.newGame()
        assertEquals(GamePhase.PLAYING, BackgammonRules.gamePhase(state.board, Player.WHITE))
        assertEquals(GamePhase.PLAYING, BackgammonRules.gamePhase(state.board, Player.RED))
    }

    @Test
    fun `all checkers in home board means BEARING_OFF`() {
        val board = Array(28) { BoardField() }
        // Place all 15 white checkers in home (real positions 19-24 = matrix 18-23)
        board[18].chipCount = 3; board[18].owner = Player.WHITE
        board[19].chipCount = 3; board[19].owner = Player.WHITE
        board[20].chipCount = 3; board[20].owner = Player.WHITE
        board[21].chipCount = 2; board[21].owner = Player.WHITE
        board[22].chipCount = 2; board[22].owner = Player.WHITE
        board[23].chipCount = 2; board[23].owner = Player.WHITE

        assertEquals(GamePhase.BEARING_OFF, BackgammonRules.gamePhase(board, Player.WHITE))
    }

    @Test
    fun `all checkers borne off means FINISHED`() {
        val board = Array(28) { BoardField() }
        // White has all 15 in bear-off
        board[GameState.WHITE_BEAR_OFF].chipCount = 15
        board[GameState.WHITE_BEAR_OFF].owner = Player.WHITE
        // Red still playing
        board[0].chipCount = 15; board[0].owner = Player.RED

        assertEquals(GamePhase.FINISHED, BackgammonRules.gamePhase(board, Player.WHITE))
        assertEquals(GamePhase.PLAYING, BackgammonRules.gamePhase(board, Player.RED))
    }

    @Test
    fun `checkWinner detects white win`() {
        val board = Array(28) { BoardField() }
        board[GameState.WHITE_BEAR_OFF].chipCount = 15
        board[GameState.WHITE_BEAR_OFF].owner = Player.WHITE
        board[0].chipCount = 15; board[0].owner = Player.RED

        assertEquals(Player.WHITE, BackgammonRules.checkWinner(board))
    }

    @Test
    fun `checkWinner returns NONE when game is ongoing`() {
        val state = GameState.newGame()
        assertEquals(Player.NONE, BackgammonRules.checkWinner(state.board))
    }

    @Test
    fun `legal moves from starting position with dice 6 and 1`() {
        val state = GameState.newGame()
        val dice = arrayOf(Die(6), Die(1), Die(0, used = true), Die(0, used = true))
        state.dice.indices.forEach { state.dice[it] = dice[it] }
        state.currentPlayer = Player.WHITE

        val moves = BackgammonRules.calculateLegalMoves(state.board, Player.WHITE, state.dice)
        assertTrue("Should have legal moves from starting position", moves.isNotEmpty())

        // Verify that moves use die values 6 or 1
        assertTrue(moves.all { it.dieValue == 6 || it.dieValue == 1 })
    }

    @Test
    fun `doubles give 4 dice to use`() {
        val state = GameState.newGame()
        val dice = arrayOf(Die(3), Die(3), Die(3), Die(3))
        state.dice.indices.forEach { state.dice[it] = dice[it] }
        state.currentPlayer = Player.WHITE

        val moves = BackgammonRules.calculateLegalMoves(state.board, Player.WHITE, state.dice)
        assertTrue("Should have legal moves with doubles", moves.isNotEmpty())
        // All move die values should be 3
        assertTrue(moves.all { it.dieValue == 3 })
    }

    @Test
    fun `bar checkers must be played first`() {
        val board = Array(28) { BoardField() }
        // White has 1 checker on bar and 14 elsewhere
        board[GameState.WHITE_BAR].chipCount = 1; board[GameState.WHITE_BAR].owner = Player.WHITE
        board[18].chipCount = 14; board[18].owner = Player.WHITE
        // Red has some checkers
        board[0].chipCount = 15; board[0].owner = Player.RED

        val dice = arrayOf(Die(3), Die(5), Die(0, used = true), Die(0, used = true))

        val moves = BackgammonRules.calculateLegalMoves(board, Player.WHITE, dice)
        // All moves must originate from bar (index 24)
        assertTrue("All moves must be from bar", moves.all { it.from == GameState.WHITE_BAR })
    }

    @Test
    fun `cannot move to point with 2+ opponent checkers`() {
        val board = Array(28) { BoardField() }
        // White checker at matrix 11 (real 1)
        board[11].chipCount = 1; board[11].owner = Player.WHITE
        // Red has 2 checkers at matrix 8 (real 4) — blocks a move of 3
        board[8].chipCount = 2; board[8].owner = Player.RED
        // Also need White to have more checkers somewhere to avoid bearing-off phase
        board[0].chipCount = 14; board[0].owner = Player.WHITE

        val dice = arrayOf(Die(3), Die(0, used = true), Die(0, used = true), Die(0, used = true))
        val moves = BackgammonRules.calculateLegalMoves(board, Player.WHITE, dice)

        // White at real 1 + die 3 = real 4 = matrix 8 — blocked by 2 Red checkers
        val blockedMove = moves.find { it.from == 11 && it.to == 8 }
        assertNull("Should not be able to move to a blocked point", blockedMove)
    }

    @Test
    fun `can hit a single opponent checker (blot)`() {
        val board = Array(28) { BoardField() }
        // White checker at matrix 11 (real 1)
        board[11].chipCount = 1; board[11].owner = Player.WHITE
        // Red has 1 checker at matrix 8 (real 4) — hittable blot
        board[8].chipCount = 1; board[8].owner = Player.RED
        // More white checkers to avoid bearing-off
        board[0].chipCount = 14; board[0].owner = Player.WHITE

        val dice = arrayOf(Die(3), Die(0, used = true), Die(0, used = true), Die(0, used = true))
        val moves = BackgammonRules.calculateLegalMoves(board, Player.WHITE, dice)

        // White at real 1 + die 3 = real 4 = matrix 8 — should be allowed (hit)
        val hitMove = moves.find { it.from == 11 && it.to == 8 }
        assertNotNull("Should be able to hit a blot", hitMove)
    }

    @Test
    fun `bearing off with exact roll`() {
        val board = Array(28) { BoardField() }
        // All white checkers in home board
        board[23].chipCount = 5; board[23].owner = Player.WHITE  // real 24
        board[22].chipCount = 5; board[22].owner = Player.WHITE  // real 23
        board[21].chipCount = 5; board[21].owner = Player.WHITE  // real 22

        val dice = arrayOf(Die(1), Die(0, used = true), Die(0, used = true), Die(0, used = true))
        val moves = BackgammonRules.calculateLegalMoves(board, Player.WHITE, dice)

        // Checker at real 24 + die 1 = real 25 → bear off
        val bearOff = moves.find { it.from == 23 && it.to == GameState.WHITE_BEAR_OFF }
        assertNotNull("Should be able to bear off with exact roll", bearOff)
    }

    @Test
    fun `bearing off with overshoot allowed for farthest checker`() {
        val board = Array(28) { BoardField() }
        // White: 5 at real 24 (matrix 23), 5 at real 23 (matrix 22), 5 at real 20 (matrix 19)
        board[23].chipCount = 5; board[23].owner = Player.WHITE
        board[22].chipCount = 5; board[22].owner = Player.WHITE
        board[19].chipCount = 5; board[19].owner = Player.WHITE  // real 20 — farthest back

        val dice = arrayOf(Die(6), Die(0, used = true), Die(0, used = true), Die(0, used = true))
        val moves = BackgammonRules.calculateLegalMoves(board, Player.WHITE, dice)

        // real 20 + 6 = 26 > 25, but it's the farthest back → allowed
        val overshoot = moves.find { it.from == 19 && it.to == GameState.WHITE_BEAR_OFF }
        assertNotNull("Farthest-back checker can bear off with overshoot", overshoot)

        // real 23 + 6 = 29 > 25, and it's NOT farthest back → not allowed
        val blocked = moves.find { it.from == 22 && it.to == GameState.WHITE_BEAR_OFF }
        assertNull("Non-farthest checker cannot overshoot", blocked)
    }

    @Test
    fun `movesFromField returns destinations`() {
        val moves = listOf(
            Move(3, 11, 8),
            Move(5, 11, 6),
            Move(3, 0, 3)
        )
        val fromField11 = BackgammonRules.movesFromField(moves, 11)
        assertNotNull(fromField11)
        assertEquals(setOf(8, 6), fromField11)
    }

    @Test
    fun `movesFromField returns null when no moves from field`() {
        val moves = listOf(Move(3, 11, 8))
        assertNull(BackgammonRules.movesFromField(moves, 5))
    }
}
