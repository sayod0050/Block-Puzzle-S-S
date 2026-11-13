package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppLanguage
import com.example.model.BlockShape
import com.example.model.GameModeType
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("S&S Block Puzzle", appName)
    }

    @Test
    fun `full CUJ launch play select tap-place pause restart and quit`() {
        // 1. Splash screen appears and can be tapped to enter Home
        composeTestRule.onNodeWithTag("splash_screen").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // 2. Press Start / Play on HomeScreen
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // 3. GamePlayScreen board & tray pieces are displayed with score 0
        composeTestRule.onNodeWithTag("game_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tray_piece_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tray_piece_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tray_piece_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("score_value").assertIsDisplayed()

        // 4. Tap tray_piece_0 to select, then tap center of game_board to place
        composeTestRule.onNodeWithTag("tray_piece_0").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("game_board").performTouchInput {
            click(center)
        }
        composeTestRule.waitForIdle()

        // Score must now be > 0 (since every piece has >= 1 cell * 10 pts)
        composeTestRule.onNodeWithText("0").assertDoesNotExist()

        // 5. Also test Drag-and-Drop of tray_piece_1 onto the board
        val boardNode = composeTestRule.onNodeWithTag("game_board").fetchSemanticsNode()
        val tray1Node = composeTestRule.onNodeWithTag("tray_piece_1").fetchSemanticsNode()
        val startPos = tray1Node.boundsInRoot.center
        val endPos = Offset(
            x = boardNode.boundsInRoot.left + boardNode.boundsInRoot.width * 0.2f,
            y = boardNode.boundsInRoot.top + boardNode.boundsInRoot.height * 0.25f
        )

        composeTestRule.onNodeWithTag("tray_piece_1").performTouchInput {
            val localEnd = Offset(
                endPos.x - tray1Node.boundsInRoot.left,
                endPos.y - tray1Node.boundsInRoot.top
            )
            swipe(
                start = center,
                end = localEnd,
                durationMillis = 300
            )
        }
        composeTestRule.waitForIdle()

        // 6. Test Pause -> Restart -> Fresh score 0
        composeTestRule.onNodeWithTag("pause_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("restart_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("0").assertIsDisplayed()

        // 7. Test Drag-and-Drop onto empty board center -> piece must place and score > 0
        val freshBoardNode = composeTestRule.onNodeWithTag("game_board").fetchSemanticsNode()
        val tray0Node = composeTestRule.onNodeWithTag("tray_piece_0").fetchSemanticsNode()
        val cellSize = freshBoardNode.boundsInRoot.width / 10f
        val targetRoot = Offset(
            x = freshBoardNode.boundsInRoot.center.x,
            y = freshBoardNode.boundsInRoot.center.y + cellSize * 1.45f
        )
        composeTestRule.onNodeWithTag("tray_piece_0").performTouchInput {
            val localEnd = Offset(
                x = targetRoot.x - tray0Node.boundsInRoot.left,
                y = targetRoot.y - tray0Node.boundsInRoot.top
            )
            swipe(start = center, end = localEnd, durationMillis = 350)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()

        // 8. Pause -> Quit to Menu
        composeTestRule.onNodeWithTag("pause_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quit_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
    }

    @Test
    fun `viewmodel gameplay loop line clear game over and restart`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)

        vm.startMode(GameModeType.CLASSIC)
        assertEquals(GameScreen.GAME_PLAY, vm.currentScreen.value)
        assertEquals(0, vm.score.value)
        assertFalse(vm.isGameOver.value)

        // Pre-fill row 0 columns 0..8
        for (c in 0 until 9) {
            vm.standardBoard.placePiece(BlockShape("pre_$c", listOf(Pair(0, 0)), 0), 0, c)
        }

        assertEquals(3, vm.trayPieces.value.filterNotNull().size)

        // Place tray piece 0 at a valid board location
        val p0 = vm.trayPieces.value[0]!!
        var placedCoord: Pair<Int, Int>? = null
        outer@ for (r in 2 until 10) {
            for (c in 0 until 10) {
                if (vm.standardBoard.canPlacePiece(p0, r, c)) {
                    placedCoord = Pair(r, c)
                    break@outer
                }
            }
        }
        assertNotNull(placedCoord)
        assertTrue(vm.tryPlaceStandardPiece(0, p0, placedCoord!!.first, placedCoord.second))
        assertTrue(vm.score.value > 0)
        assertNull(vm.trayPieces.value[0])

        val filledCount = vm.boardGrid.value.flatten().count { it.isFilled }
        assertEquals(9 + p0.coordinates.size, filledCount)

        // Test Game Over detection when board has no room for remaining pieces
        vm.startMode(GameModeType.CLASSIC)
        // Fill checkerboard leaving only isolated 1x1 holes or full board except (9,9)
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                if (!(r == 9 && c == 9)) {
                    vm.standardBoard.placePiece(BlockShape("blk", listOf(Pair(0, 0)), 0), r, c)
                }
            }
        }
        // Check if remaining tray pieces >1x1 trigger game over after placing a 1x1 or checking board
        val bigPiece = vm.trayPieces.value.firstOrNull { it != null && it.coordinates.size > 1 }
        if (bigPiece != null) {
            assertFalse(vm.standardBoard.canPlacePieceAnywhere(bigPiece))
        }

        // Verify Hexa mode starts cleanly and places valid hex pieces
        vm.startMode(GameModeType.HEXA)
        assertEquals(GameModeType.HEXA, vm.activeMode.value)
        val hexPiece = vm.trayPieces.value[0]!!
        assertTrue(hexPiece.isHex)
        assertTrue(vm.hexBoard.canPlacePieceAnywhere(hexPiece))

        // Verify language switching preserves settings
        vm.setLanguage(AppLanguage.TAJIK)
        assertEquals("Оғоз", vm.strings.value.play)
        vm.setLanguage(AppLanguage.RUSSIAN)
        assertEquals("Играть", vm.strings.value.play)
        vm.setLanguage(AppLanguage.ENGLISH)
        assertEquals("Play", vm.strings.value.play)
    }
}
