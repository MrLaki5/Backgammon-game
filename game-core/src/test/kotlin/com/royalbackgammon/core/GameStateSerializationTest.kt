package com.royalbackgammon.core

import com.royalbackgammon.core.model.*
import org.junit.Assert.*
import org.junit.Test

class GameStateSerializationTest {

    @Test
    fun `new game serializes and deserializes correctly`() {
        val original = GameState.newGame()
        val json = original.toJson()
        val restored = GameState.fromJson(json)

        assertEquals(original, restored)
    }

    @Test
    fun `mid-game state round trips through JSON`() {
        val state = GameState.newGame()
        state.currentPlayer = Player.RED
        state.turnState = GameState.STATE_MOVE
        state.dice[0] = Die(4, used = false)
        state.dice[1] = Die(2, used = true)
        // Simulate a move happened
        state.board[0].chipCount = 4
        state.board[14].chipCount = 1
        state.board[14].owner = Player.WHITE

        val json = state.toJson()
        val restored = GameState.fromJson(json)

        assertEquals(state, restored)
    }

    @Test
    fun `JSON contains expected fields`() {
        val state = GameState.newGame()
        val json = state.toJson()

        assertTrue(json.contains("\"board\""))
        assertTrue(json.contains("\"dice\""))
        assertTrue(json.contains("\"currentPlayer\""))
        assertTrue(json.contains("\"turnState\""))
        assertTrue(json.contains("\"winner\""))
    }

    @Test
    fun `deepCopy creates independent copy`() {
        val original = GameState.newGame()
        val copy = original.deepCopy()

        // Modify copy
        copy.board[0].chipCount = 99
        copy.currentPlayer = Player.RED

        // Original unchanged
        assertEquals(5, original.board[0].chipCount)
        assertEquals(Player.WHITE, original.currentPlayer)
    }
}
