package com.royalbackgammon.core

import com.royalbackgammon.core.bot.BotDifficulty
import com.royalbackgammon.core.bot.BotStrategy
import com.royalbackgammon.core.dice.FixedDiceRoller
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.logic.GameEngine
import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.*
import com.royalbackgammon.core.scoring.MatchState
import com.royalbackgammon.core.variant.Variant
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class VariantRulesTest {

    private fun emptyBoard() = Array(28) { BoardField() }

    private fun put(board: Array<BoardField>, player: Int, real: Int, count: Int) {
        val idx = PositionMapper.toMatrix(real, player)
        board[idx].chipCount = count
        board[idx].owner = player
    }

    private fun dice(a: Int, b: Int): Array<Die> =
        if (a == b) arrayOf(Die(a), Die(a), Die(a), Die(a))
        else arrayOf(Die(a), Die(b), Die(0, used = true), Die(0, used = true))

    // ---- Maximum-dice rule ----

    @Test
    fun `first move that strands the second die is illegal`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 1, 1)    // A
        put(b, Player.WHITE, 18, 1)   // B
        put(b, Player.WHITE, 24, 13)  // immobile outside bear-off phase
        put(b, Player.RED, 19, 2)     // white real 6 blocked
        put(b, Player.RED, 12, 13)

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(6, 5))
        val bFrom = PositionMapper.toMatrix(18, Player.WHITE)

        assertNull("B+6 leaves the 5 unplayable",
            moves.find { it.from == bFrom && it.dieValue == 6 })
        assertNotNull("B+5 allows A+6 afterwards",
            moves.find { it.from == bFrom && it.dieValue == 5 })
        assertNotNull("A+6 allows B+5 afterwards",
            moves.find { it.from == PositionMapper.toMatrix(1, Player.WHITE) && it.dieValue == 6 })
    }

    @Test
    fun `when only one die can be played the higher die is forced`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 1, 1)
        put(b, Player.WHITE, 24, 14)
        put(b, Player.RED, 17, 2)     // white real 8 blocked
        put(b, Player.RED, 12, 13)

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(6, 1))

        assertEquals(1, moves.size)
        assertEquals(6, moves[0].dieValue)
    }

    @Test
    fun `lower die is allowed when the higher die cannot be played at all`() {
        val b = emptyBoard()
        put(b, Player.WHITE, 1, 1)
        put(b, Player.WHITE, 24, 14)
        put(b, Player.RED, 18, 2)     // white real 7 blocked
        put(b, Player.RED, 17, 2)     // white real 8 blocked
        put(b, Player.RED, 12, 11)

        val moves = BackgammonRules.calculateLegalMoves(b, Player.WHITE, dice(6, 1))

        assertEquals(1, moves.size)
        assertEquals(1, moves[0].dieValue)
    }

    @Test
    fun `doubles produce each move once`() {
        val state = GameState.newGame()
        val moves = BackgammonRules.calculateLegalMoves(state.board, Player.WHITE, dice(3, 3))
        assertEquals(moves.size, moves.distinct().size)
    }

    @Test
    fun `bear-off overshoot uses true farthest checker for red`() {
        val b = emptyBoard()
        put(b, Player.RED, 20, 5)
        put(b, Player.RED, 23, 5)
        put(b, Player.RED, 24, 5)
        put(b, Player.WHITE, 12, 15)

        val moves = BackgammonRules.calculateLegalMoves(
            b, Player.RED, arrayOf(Die(6), Die(0, used = true), Die(0, used = true), Die(0, used = true))
        )

        assertEquals(1, moves.size)
        assertEquals(PositionMapper.toMatrix(20, Player.RED), moves[0].from)
        assertEquals(GameState.RED_BEAR_OFF, moves[0].to)
    }

    // ---- Win types and scoring ----

    private fun whiteWonBoard(): Array<BoardField> {
        val b = emptyBoard()
        b[GameState.WHITE_BEAR_OFF].chipCount = 15; b[GameState.WHITE_BEAR_OFF].owner = Player.WHITE
        return b
    }

    @Test
    fun `single win when loser has borne off`() {
        val b = whiteWonBoard()
        b[GameState.RED_BEAR_OFF].chipCount = 1; b[GameState.RED_BEAR_OFF].owner = Player.RED
        put(b, Player.RED, 20, 14)
        assertEquals(WinType.SINGLE, BackgammonRules.winType(b, Player.WHITE))
    }

    @Test
    fun `gammon when loser has borne off nothing`() {
        val b = whiteWonBoard()
        put(b, Player.RED, 15, 15)
        assertEquals(WinType.GAMMON, BackgammonRules.winType(b, Player.WHITE))
    }

    @Test
    fun `backgammon when loser has a checker in winner home board`() {
        val b = whiteWonBoard()
        put(b, Player.RED, 15, 14)
        put(b, Player.RED, 3, 1)
        assertEquals(WinType.BACKGAMMON, BackgammonRules.winType(b, Player.WHITE))
    }

    @Test
    fun `backgammon when loser has a checker on the bar`() {
        val b = whiteWonBoard()
        put(b, Player.RED, 15, 14)
        b[GameState.RED_BAR].chipCount = 1; b[GameState.RED_BAR].owner = Player.RED
        assertEquals(WinType.BACKGAMMON, BackgammonRules.winType(b, Player.WHITE))
    }

    @Test
    fun `backgammon scores 3 in standard and 2 in tavla and portes`() {
        val b = whiteWonBoard()
        put(b, Player.RED, 2, 15)
        assertEquals(3, BackgammonRules.gameResult(b, Variant.STANDARD)!!.points)
        assertEquals(2, BackgammonRules.gameResult(b, Variant.TAVLA)!!.points)
        assertEquals(2, BackgammonRules.gameResult(b, Variant.PORTES)!!.points)
    }

    @Test
    fun `gameResult is null while game is ongoing`() {
        assertNull(BackgammonRules.gameResult(GameState.newGame().board, Variant.STANDARD))
    }

    @Test
    fun `engine reports result when last checker is borne off`() {
        val b = emptyBoard()
        b[GameState.WHITE_BEAR_OFF].chipCount = 14; b[GameState.WHITE_BEAR_OFF].owner = Player.WHITE
        put(b, Player.WHITE, 24, 1)
        put(b, Player.RED, 10, 15)
        val state = GameState(
            board = b, dice = dice(1, 2), currentPlayer = Player.WHITE,
            turnState = GameState.STATE_MOVE, variant = Variant.TAVLA
        )
        val engine = GameEngine(state, FixedDiceRoller(1))
        val move = engine.legalMoves.first { it.to == GameState.WHITE_BEAR_OFF }
        engine.makeMove(move.from, move.to)

        assertEquals(GameResult(Player.WHITE, WinType.GAMMON, 2), engine.result)
    }

    // ---- Opening roll ----

    @Test
    fun `tied opening roll restarts the opening`() {
        val engine = GameEngine(GameState.newGame(), FixedDiceRoller(4, 4, 5, 2))
        engine.rollDice()
        engine.rollDice()
        assertEquals(GameState.STATE_INITIAL_ROLL_P1, engine.state.turnState)
        assertEquals(Player.WHITE, engine.state.currentPlayer)

        engine.rollDice()
        engine.rollDice()
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertEquals(Player.WHITE, engine.state.currentPlayer)
    }

    @Test
    fun `standard starter plays the opening dice`() {
        val engine = GameEngine(GameState.newGame(Variant.STANDARD), FixedDiceRoller(2, 6))
        engine.rollDice(); engine.rollDice()
        assertEquals(Player.RED, engine.state.currentPlayer)
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertTrue(engine.legalMoves.all { it.dieValue == 2 || it.dieValue == 6 })
    }

    @Test
    fun `tavla starter rolls again and may open with doubles`() {
        val engine = GameEngine(GameState.newGame(Variant.TAVLA), FixedDiceRoller(2, 6, 5, 5))
        engine.rollDice(); engine.rollDice()
        assertEquals(Player.RED, engine.state.currentPlayer)
        assertEquals(GameState.STATE_ROLL, engine.state.turnState)
        assertTrue(engine.legalMoves.isEmpty())

        engine.rollDice()
        assertEquals(GameState.STATE_MOVE, engine.state.turnState)
        assertEquals(Player.RED, engine.state.currentPlayer)
        assertEquals(4, engine.state.dice.count { !it.used && it.value == 5 })
    }

    // ---- Match scoring ----

    @Test
    fun `match ends when target is reached`() {
        val match = MatchState(Variant.PORTES, targetPoints = 5)
        match.record(GameResult(Player.WHITE, WinType.GAMMON, 2))
        match.record(GameResult(Player.RED, WinType.SINGLE, 1))
        assertFalse(match.isOver())
        match.record(GameResult(Player.WHITE, WinType.BACKGAMMON, 2))
        assertFalse(match.isOver())
        match.record(GameResult(Player.WHITE, WinType.SINGLE, 1))
        assertTrue(match.isOver())
        assertEquals(Player.WHITE, match.winner())
        assertEquals(5, match.scoreOf(Player.WHITE))
        assertEquals(4, match.gamesPlayed)
    }

    @Test
    fun `draw counts as a game without points`() {
        val match = MatchState(Variant.PLAKOTO, targetPoints = 3)
        match.record(GameResult(Player.NONE, WinType.DRAW, 0))
        assertEquals(1, match.gamesPlayed)
        assertEquals(0, match.whiteScore + match.redScore)
        assertFalse(match.isOver())
    }

    @Test(expected = IllegalStateException::class)
    fun `recording after match end fails`() {
        val match = MatchState(targetPoints = 1)
        match.record(GameResult(Player.RED, WinType.SINGLE, 1))
        match.record(GameResult(Player.RED, WinType.SINGLE, 1))
    }

    // ---- Persistence ----

    @Test
    fun `variant survives json round trip`() {
        val state = GameState.newGame(Variant.PORTES)
        assertEquals(Variant.PORTES, GameState.fromJson(state.toJson()).variant)
    }

    @Test
    fun `json without variant field loads as standard`() {
        val legacy = GameState.newGame().toJson().replace(",\"variant\":\"STANDARD\"", "")
        assertFalse(legacy.contains("variant"))
        assertEquals(Variant.STANDARD, GameState.fromJson(legacy).variant)
    }

    // ---- Bot still performs under the stricter move generator ----

    @Test
    fun `royal bot picks a legal move quickly`() {
        val state = GameState.newGame()
        for (i in 0 until 4) state.dice[i] = dice(6, 6)[i]
        val legal = BackgammonRules.calculateLegalMoves(state.board, Player.WHITE, state.dice)
        val bot = BotStrategy(BotDifficulty.ROYAL, Random(7))

        val start = System.nanoTime()
        val move = bot.chooseMove(state, legal)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        assertTrue(move in legal)
        assertTrue("Bot took ${elapsedMs}ms", elapsedMs < 1500)
    }
}
