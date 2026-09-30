package com.royalbackgammon.core

import com.royalbackgammon.core.logic.PositionMapper
import com.royalbackgammon.core.model.GameState
import com.royalbackgammon.core.model.Player
import org.junit.Assert.*
import org.junit.Test

class PositionMapperTest {

    @Test
    fun `white bar maps to real 0`() {
        assertEquals(0, PositionMapper.toReal(GameState.WHITE_BAR, Player.WHITE))
    }

    @Test
    fun `white bear-off maps to real 100`() {
        assertEquals(100, PositionMapper.toReal(GameState.WHITE_BEAR_OFF, Player.WHITE))
    }

    @Test
    fun `red bar maps to real 0`() {
        assertEquals(0, PositionMapper.toReal(GameState.RED_BAR, Player.RED))
    }

    @Test
    fun `red bear-off maps to real 100`() {
        assertEquals(100, PositionMapper.toReal(GameState.RED_BEAR_OFF, Player.RED))
    }

    @Test
    fun `white matrix 0 is real 12`() {
        assertEquals(12, PositionMapper.toReal(0, Player.WHITE))
    }

    @Test
    fun `white matrix 11 is real 1`() {
        assertEquals(1, PositionMapper.toReal(11, Player.WHITE))
    }

    @Test
    fun `white matrix 12 is real 13`() {
        assertEquals(13, PositionMapper.toReal(12, Player.WHITE))
    }

    @Test
    fun `white matrix 23 is real 24`() {
        assertEquals(24, PositionMapper.toReal(23, Player.WHITE))
    }

    @Test
    fun `red matrix position is inverted from white`() {
        // Red's real = 25 - White's real
        // Matrix 0 → White real 12 → Red real 13
        assertEquals(13, PositionMapper.toReal(0, Player.RED))
        // Matrix 23 → White real 24 → Red real 1
        assertEquals(1, PositionMapper.toReal(23, Player.RED))
    }

    @Test
    fun `toMatrix is inverse of toReal for white`() {
        for (matrix in 0..23) {
            val real = PositionMapper.toReal(matrix, Player.WHITE)
            assertEquals(matrix, PositionMapper.toMatrix(real, Player.WHITE))
        }
    }

    @Test
    fun `toMatrix is inverse of toReal for red`() {
        for (matrix in 0..23) {
            val real = PositionMapper.toReal(matrix, Player.RED)
            assertEquals(matrix, PositionMapper.toMatrix(real, Player.RED))
        }
    }
}
