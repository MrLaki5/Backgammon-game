package com.royalbackgammon.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.royalbackgammon.core.logic.BackgammonRules
import com.royalbackgammon.core.variant.RuleFamily
import com.royalbackgammon.core.variant.Variant

/**
 * Complete, serializable state of a backgammon game.
 *
 * Board layout (28 fields):
 *  - Indices 0–23: the 24 points (triangles)
 *  - Index 24: White's bar
 *  - Index 25: Red's bar
 *  - Index 26: Red's bear-off
 *  - Index 27: White's bear-off
 *
 * @property board Array of 28 board fields.
 * @property dice Array of 4 dice (supports doubles).
 * @property currentPlayer Currently active player (1=White, 2=Red).
 * @property turnState FSM state: 0=P1 initial roll, 1=P2 initial roll, 2=move, 3=roll.
 * @property winner 0 if game is ongoing, otherwise the winning player number.
 * @property variant Rule variant this game is played under.
 * @property turnsPlayed Completed turns in this game (both players).
 * @property headMovesThisTurn Checkers moved off the head in the current turn (running family).
 */
@Serializable
data class GameState(
    val board: Array<BoardField> = Array(28) { BoardField() },
    val dice: Array<Die> = Array(4) { Die(value = 0, used = true) },
    var currentPlayer: Int = Player.WHITE,
    var turnState: Int = STATE_INITIAL_ROLL_P1,
    var winner: Int = Player.NONE,
    val variant: Variant = Variant.STANDARD,
    var turnsPlayed: Int = 0,
    var headMovesThisTurn: Int = 0,
    /** Acey-deucey: a 1-2 was rolled and the named double has not been played yet. */
    /** Acey-deucey: checkers of each player sent back by a hit (index 0 = White). */
    var barHits: IntArray = IntArray(2),
    var bonusDoublePending: Boolean = false,
    /** Acey-deucey: this player rolls again after finishing the turn. */
    var extraTurnPending: Boolean = false
) {
    companion object {
        const val STATE_INITIAL_ROLL_P1 = 0
        const val STATE_INITIAL_ROLL_P2 = 1
        const val STATE_MOVE = 2
        const val STATE_ROLL = 3

        // Board indices
        const val WHITE_BAR = 24
        const val RED_BAR = 25
        const val RED_BEAR_OFF = 26
        const val WHITE_BEAR_OFF = 27

        @JvmStatic
        fun barIndex(player: Int): Int = if (player == Player.WHITE) WHITE_BAR else RED_BAR
        @JvmStatic
        fun bearOffIndex(player: Int): Int = if (player == Player.WHITE) WHITE_BEAR_OFF else RED_BEAR_OFF

        /**
         * Creates a new game with [variant]'s starting position: the standard layout for the
         * hitting family, all 15 checkers on each starting point otherwise.
         */
        @JvmStatic
        @JvmOverloads
        fun newGame(variant: Variant = Variant.STANDARD): GameState {
            val state = GameState(variant = variant)
            val b = state.board
            if (variant.startsOnBar) {
                b[barIndex(Player.WHITE)].let { it.chipCount = 15; it.owner = Player.WHITE }
                b[barIndex(Player.RED)].let { it.chipCount = 15; it.owner = Player.RED }
                return state
            }
            if (variant.family == RuleFamily.RUNNING || variant.family == RuleFamily.PINNING) {
                b[BackgammonRules.headIndex(Player.WHITE, variant)].let { it.chipCount = 15; it.owner = Player.WHITE }
                b[BackgammonRules.headIndex(Player.RED, variant)].let { it.chipCount = 15; it.owner = Player.RED }
                return state
            }
            // White pieces
            b[0].chipCount = 5; b[0].owner = Player.WHITE
            b[11].chipCount = 2; b[11].owner = Player.WHITE
            b[16].chipCount = 3; b[16].owner = Player.WHITE
            b[18].chipCount = 5; b[18].owner = Player.WHITE
            // Red pieces
            b[4].chipCount = 3; b[4].owner = Player.RED
            b[6].chipCount = 5; b[6].owner = Player.RED
            b[12].chipCount = 5; b[12].owner = Player.RED
            b[23].chipCount = 2; b[23].owner = Player.RED
            return state
        }

        private val json = Json { prettyPrint = false; encodeDefaults = true; ignoreUnknownKeys = true }

        /**
         * Deserializes a GameState from JSON.
         */
        fun fromJson(jsonString: String): GameState = json.decodeFromString(jsonString)
    }

    /**
     * Serializes this game state to JSON.
     */
    fun toJson(): String = json.encodeToString(this)

    /**
     * Creates a deep copy of this game state.
     */
    fun deepCopy(): GameState = GameState(
        board = Array(board.size) { board[it].copy() },
        dice = Array(dice.size) { dice[it].copy() },
        currentPlayer = currentPlayer,
        turnState = turnState,
        winner = winner,
        variant = variant,
        turnsPlayed = turnsPlayed,
        headMovesThisTurn = headMovesThisTurn,
        barHits = barHits.copyOf(),
        bonusDoublePending = bonusDoublePending,
        extraTurnPending = extraTurnPending
    )

    /** Head-rule context for the current player's turn. */
    fun turnContext(): TurnContext = TurnContext(headMovesThisTurn,
        firstTurn = turnsPlayed < 2, hitCheckersOnBar = hitsOnBar(currentPlayer))

    /** Checkers of [player] that are on the bar because they were hit. */
    fun hitsOnBar(player: Int): Int = barHits[if (player == Player.WHITE) 0 else 1]

    fun setHitsOnBar(player: Int, value: Int) {
        barHits[if (player == Player.WHITE) 0 else 1] = maxOf(0, value)
    }

    /** Ends the current turn: counts it, clears the head counter and switches player. */
    fun passTurn() {
        turnsPlayed++
        headMovesThisTurn = 0
        bonusDoublePending = false
        extraTurnPending = false
        switchPlayer()
    }

    /**
     * Switches the current player to the opponent.
     */
    fun switchPlayer() {
        currentPlayer = Player.opponent(currentPlayer)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        return board.contentEquals(other.board) &&
                dice.contentEquals(other.dice) &&
                currentPlayer == other.currentPlayer &&
                turnState == other.turnState &&
                winner == other.winner &&
                variant == other.variant &&
                turnsPlayed == other.turnsPlayed &&
                headMovesThisTurn == other.headMovesThisTurn &&
                barHits.contentEquals(other.barHits) &&
                bonusDoublePending == other.bonusDoublePending &&
                extraTurnPending == other.extraTurnPending
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + dice.contentHashCode()
        result = 31 * result + currentPlayer
        result = 31 * result + turnState
        result = 31 * result + winner
        result = 31 * result + variant.hashCode()
        result = 31 * result + turnsPlayed
        result = 31 * result + headMovesThisTurn
        result = 31 * result + barHits.contentHashCode()
        result = 31 * result + bonusDoublePending.hashCode()
        result = 31 * result + extraTurnPending.hashCode()
        return result
    }
}
