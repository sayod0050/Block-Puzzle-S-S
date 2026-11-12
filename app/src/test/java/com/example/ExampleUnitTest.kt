package com.example

import com.example.model.AppLanguage
import com.example.model.BlockShape
import com.example.model.GameThemes
import com.example.model.HexGameBoard
import com.example.model.LocalizationManager
import com.example.model.StandardGameBoard
import com.example.model.ThemeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStandardGameBoardRowClear() {
        val board = StandardGameBoard(10)

        // Fill row 0 completely except for 1 cell
        for (c in 0 until 9) {
            val single = BlockShape("test_$c", listOf(Pair(0, 0)), colorIndex = 0)
            assertTrue(board.canPlacePiece(single, 0, c))
            board.placePiece(single, 0, c)
        }

        // Row should not be full yet
        val resultBefore = board.checkAndClearLines()
        assertEquals(0, resultBefore.clearedCount)

        // Place last block at (0, 9)
        val lastPiece = BlockShape("last", listOf(Pair(0, 0)), colorIndex = 1)
        board.placePiece(lastPiece, 0, 9)

        // Now row 0 must clear
        val resultAfter = board.checkAndClearLines()
        assertEquals(1, resultAfter.clearedCount)
        assertEquals(10, resultAfter.clearedCells.size)
        // Verify row 0 cells are now empty
        for (c in 0 until 10) {
            assertFalse(board.getCell(0, c)?.isFilled ?: true)
        }
    }

    @Test
    fun testHexGameBoardAxialLineClear() {
        val hexBoard = HexGameBoard(radius = 3)

        // In radius 3, q = 0 has r from -3 to 3 (7 cells)
        val q0Cells = hexBoard.validCoords.filter { it.first == 0 }
        assertEquals(7, q0Cells.size)

        for (coord in q0Cells) {
            val hexPiece = BlockShape("hex", listOf(Pair(0, 0)), colorIndex = 2, isHex = true)
            hexBoard.placePiece(hexPiece, coord.first, coord.second)
        }

        val clearResult = hexBoard.checkAndClearLines()
        assertTrue(clearResult.clearedCount >= 1)
        assertTrue(clearResult.clearedCells.containsAll(q0Cells))
    }

    @Test
    fun testBombCountdownTick() {
        val board = StandardGameBoard(10)
        board.addBomb(count = 2)

        // Find bomb
        var bombFound = false
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                if (board.getCell(r, c)?.bombCountdown != null) {
                    assertEquals(2, board.getCell(r, c)?.bombCountdown)
                    bombFound = true
                }
            }
        }
        assertTrue(bombFound)

        // Tick 1
        var exploded = board.tickBombs()
        assertFalse(exploded)

        // Tick 2: reaches 0 -> explodes
        exploded = board.tickBombs()
        assertTrue(exploded)
    }

    @Test
    fun testLocalizationAllLanguagesValid() {
        for (lang in AppLanguage.entries) {
            val strings = LocalizationManager.get(lang)
            assertNotNull(strings.appTitle)
            assertNotNull(strings.creators)
            assertNotNull(strings.play)
            assertNotNull(strings.modeClassic)
            assertNotNull(strings.modeHexa)
            assertTrue(strings.appTitle.isNotBlank())
        }
    }

    @Test
    fun testThemesCoverage() {
        assertEquals(8, GameThemes.allThemes.size)
        for (id in ThemeId.entries) {
            val theme = GameThemes.getTheme(id)
            assertNotNull(theme)
            assertEquals(7, theme.blockColors.size)
        }
    }
}
