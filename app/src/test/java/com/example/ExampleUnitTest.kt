package com.example

import com.example.model.AppLanguage
import com.example.model.BlockShape
import com.example.model.GameThemes
import com.example.model.HexGameBoard
import com.example.model.LocalizationManager
import com.example.model.StandardGameBoard
import com.example.model.ThemeId
import com.example.ui.components.hexAxialToPixel
import com.example.ui.components.pixelToHexAxial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStandardGameBoardRowClear() {
        val board = StandardGameBoard(10)

        for (c in 0 until 9) {
            val single = BlockShape("test_$c", listOf(Pair(0, 0)), colorIndex = 0)
            assertTrue(board.canPlacePiece(single, 0, c))
            board.placePiece(single, 0, c)
        }

        val resultBefore = board.checkAndClearLines()
        assertEquals(0, resultBefore.clearedCount)

        val lastPiece = BlockShape("last", listOf(Pair(0, 0)), colorIndex = 1)
        board.placePiece(lastPiece, 0, 9)

        val resultAfter = board.checkAndClearLines()
        assertEquals(1, resultAfter.clearedCount)
        assertEquals(10, resultAfter.clearedCells.size)
        for (c in 0 until 10) {
            assertFalse(board.getCell(0, c)?.isFilled ?: true)
        }
    }

    @Test
    fun testPredictiveWillClearCells() {
        val board = StandardGameBoard(10)
        for (c in 0 until 8) {
            board.placePiece(BlockShape("p_$c", listOf(Pair(0, 0)), 0), 2, c)
        }
        val bar2 = BlockShape("bar2", listOf(Pair(0, 0), Pair(0, 1)), 1)
        val willClear = board.getWillClearCells(bar2, 2, 8)
        assertEquals(10, willClear.size)
        for (c in 0 until 10) {
            assertTrue(willClear.contains(Pair(2, c)))
        }
    }

    @Test
    fun testSmartTapPlacementSnapsInsideBounds() {
        val board = StandardGameBoard(10)
        val bar3 = BlockShape("bar3", listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)), 0)
        val pos = board.findTapPlacement(bar3, 0, 9)
        assertNotNull(pos)
        assertTrue(board.canPlacePiece(bar3, pos!!.first, pos.second))

        // Reject when completely blocked
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                board.placePiece(BlockShape("fill", listOf(Pair(0, 0)), 0), r, c)
            }
        }
        assertNull(board.findTapPlacement(bar3, 5, 5))
    }

    @Test
    fun testHexGameBoardAxialLineClearAndPixelRoundtrip() {
        val hexBoard = HexGameBoard(radius = 3)
        val hexRadius = 40f

        for (coord in hexBoard.validCoords) {
            val px = hexAxialToPixel(coord.first, coord.second, hexRadius)
            val roundtrip = pixelToHexAxial(px.x, px.y, hexRadius)
            assertEquals(coord, roundtrip)
        }

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

        assertFalse(board.tickBombs())
        assertTrue(board.tickBombs())
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
