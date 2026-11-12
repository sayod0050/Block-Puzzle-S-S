package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AdventureStages
import com.example.model.BlockShape
import com.example.model.GameModeType
import com.example.model.VisualTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val mode by viewModel.activeMode.collectAsState()
    val score by viewModel.score.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val combo by viewModel.combo.collectAsState()
    val timeLeft by viewModel.timeLeftSeconds.collectAsState()
    val movesLeft by viewModel.movesLeft.collectAsState()
    val trayPieces by viewModel.trayPieces.collectAsState()
    val selectedPieceIndex by viewModel.selectedPieceIndex.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val isVictory by viewModel.isVictory.collectAsState()
    val newRecord by viewModel.newHighScoreAchieved.collectAsState()
    val floatingCombos by viewModel.floatingCombos.collectAsState()
    val achievementPopup by viewModel.achievementPopup.collectAsState()

    // Dragging state across the screen
    var draggingPieceIndex by remember { mutableStateOf<Int?>(null) }
    var dragTouchOffset by remember { mutableStateOf(Offset.Zero) }
    var hoverGridCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Board layout metrics
    var boardTopLeft by remember { mutableStateOf(Offset.Zero) }
    var boardCellSize by remember { mutableStateOf(0f) }

    // Score scale animation on score change
    val scoreScale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        if (score > 0) {
            scoreScale.animateTo(1.22f, tween(100))
            scoreScale.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // TOP HUD: Pause, Score, Best, Mode Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button
                IconButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(theme.cardBackground)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = strings.pause,
                        tint = theme.textColorPrimary
                    )
                }

                // Current Score with bouncing animation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.score.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColorSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$score",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.textColorPrimary,
                        modifier = Modifier.scale(scoreScale.value)
                    )
                }

                // Best Score or Mode Specific Action (e.g. Undo in Zen)
                if (mode == GameModeType.ZEN) {
                    IconButton(
                        onClick = { viewModel.undoZenMove() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(theme.cardBackground)
                            .testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = theme.accentColor
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.bestScore.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColorSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$bestScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-HUD for Mode Specific Telemetry (Timer, Moves, Target, Combo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left badge: Timer / Moves / Level
                when (mode) {
                    GameModeType.TIME_ATTACK -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (timeLeft <= 15) Color(0x33FF3366) else Color(0x2200E5FF)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.timeLeft}: ",
                                fontSize = 13.sp,
                                color = theme.textColorSecondary
                            )
                            Text(
                                text = "${timeLeft}s",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timeLeft <= 15) Color(0xFFFF3366) else theme.accentColor
                            )
                        }
                    }
                    GameModeType.PUZZLE_CHALLENGE, GameModeType.DAILY_CHALLENGE -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x227C4DFF))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.movesLeft}: ",
                                fontSize = 13.sp,
                                color = theme.textColorSecondary
                            )
                            Text(
                                text = "$movesLeft",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB388FF)
                            )
                        }
                    }
                    GameModeType.ADVENTURE -> {
                        val stage = AdventureStages.getStage(viewModel.adventureLevel.collectAsState().value)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x2200E676))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.level} ${stage.levelNumber} - ${strings.targetGoal}: ${stage.targetScore}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = strings.dragToPlace,
                            fontSize = 12.sp,
                            color = theme.textColorSecondary.copy(alpha = 0.8f)
                        )
                    }
                }

                // Right badge: Active Combo Multiplier
                if (combo > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF3366), Color(0xFFFFB300))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${strings.combo} x$combo!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // THE BOARD CONTAINER
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth.value
                val availableHeight = maxHeight.value
                val boardDim = minOf(availableWidth, availableHeight).dp

                Box(
                    modifier = Modifier
                        .size(boardDim)
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(theme.boardBackground)
                        .border(3.dp, theme.boardBorder, RoundedCornerShape(22.dp))
                        .testTag("game_board")
                ) {
                    if (mode == GameModeType.HEXA) {
                        HexBoardCanvas(
                            viewModel = viewModel,
                            theme = theme,
                            hoverCell = hoverGridCell,
                            selectedPieceIndex = selectedPieceIndex ?: draggingPieceIndex,
                            onBoardLayout = { tl, cs ->
                                boardTopLeft = tl
                                boardCellSize = cs
                            },
                            onCellTapped = { q, r ->
                                viewModel.tryPlaceSelectedPiece(q, r)
                            }
                        )
                    } else {
                        StandardBoardCanvas(
                            viewModel = viewModel,
                            theme = theme,
                            hoverCell = hoverGridCell,
                            selectedPieceIndex = selectedPieceIndex ?: draggingPieceIndex,
                            onBoardLayout = { tl, cs ->
                                boardTopLeft = tl
                                boardCellSize = cs
                            },
                            onCellTapped = { r, c ->
                                viewModel.tryPlaceSelectedPiece(r, c)
                            }
                        )
                    }

                    // Floating combos overlay
                    for (comboItem in floatingCombos) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xEE1E2138))
                                .border(2.dp, theme.accentColor, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = comboItem.text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TRAY OF 3 AVAILABLE PIECES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.cardBackground.copy(alpha = 0.6f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                trayPieces.forEachIndexed { index, piece ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedPieceIndex == index) theme.accentColor.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (selectedPieceIndex == index) 2.dp else 0.dp,
                                color = if (selectedPieceIndex == index) theme.accentColor else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                if (piece != null) {
                                    viewModel.selectPieceForPlacement(index)
                                }
                            }
                            .pointerInput(piece) {
                                if (piece != null) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            draggingPieceIndex = index
                                            dragTouchOffset = offset
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragTouchOffset += dragAmount

                                            // Calculate grid cell under finger (with lift offset)
                                            if (boardCellSize > 0f) {
                                                val liftedY = dragTouchOffset.y - 120f // Lift above finger
                                                val liftedX = dragTouchOffset.x
                                                val relX = liftedX - boardTopLeft.x
                                                val relY = liftedY - boardTopLeft.y
                                                val targetCol = (relX / boardCellSize).toInt()
                                                val targetRow = (relY / boardCellSize).toInt()
                                                if (targetRow in 0..9 && targetCol in 0..9) {
                                                    hoverGridCell = Pair(targetRow, targetCol)
                                                } else {
                                                    hoverGridCell = null
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            val hover = hoverGridCell
                                            if (hover != null && draggingPieceIndex != null) {
                                                val placed = if (mode == GameModeType.HEXA) {
                                                    viewModel.tryPlaceHexPiece(draggingPieceIndex!!, piece, hover.first, hover.second)
                                                } else {
                                                    viewModel.tryPlaceStandardPiece(draggingPieceIndex!!, piece, hover.first, hover.second)
                                                }
                                                if (!placed) {
                                                    viewModel.soundManager.playButtonClick()
                                                }
                                            }
                                            draggingPieceIndex = null
                                            hoverGridCell = null
                                        },
                                        onDragCancel = {
                                            draggingPieceIndex = null
                                            hoverGridCell = null
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            TrayPiecePreview(
                                piece = piece,
                                theme = theme,
                                isSelected = selectedPieceIndex == index
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Achievement Toast Popup
        AnimatedVisibility(
            visible = achievementPopup != null,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(350)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        ) {
            achievementPopup?.let { ach ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2138)),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .border(2.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp))
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Achievement",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.achievements.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                            Text(
                                text = ach.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "+${ach.reward} ${strings.coins}",
                                fontSize = 12.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }
        }

        // Pause Modal Dialog
        if (isPaused) {
            PauseDialog(
                viewModel = viewModel,
                theme = theme,
                strings = strings,
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.startMode(mode, viewModel.adventureLevel.value) },
                onQuit = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }

        // Game Over Modal Dialog
        if (isGameOver) {
            GameOverDialog(
                viewModel = viewModel,
                theme = theme,
                strings = strings,
                score = score,
                bestScore = bestScore,
                isNewRecord = newRecord,
                onRestart = { viewModel.startMode(mode, viewModel.adventureLevel.value) },
                onQuit = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }

        // Victory Modal Dialog (Adventure / Daily)
        if (isVictory) {
            VictoryDialog(
                viewModel = viewModel,
                theme = theme,
                strings = strings,
                score = score,
                onNext = {
                    val nextLvl = (viewModel.adventureLevel.value + 1).coerceAtMost(20)
                    viewModel.startMode(mode, nextLvl)
                },
                onQuit = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }
    }
}

// 10x10 Standard Board Canvas
@Composable
fun StandardBoardCanvas(
    viewModel: GameViewModel,
    theme: VisualTheme,
    hoverCell: Pair<Int, Int>?,
    selectedPieceIndex: Int?,
    onBoardLayout: (Offset, Float) -> Unit,
    onCellTapped: (Int, Int) -> Unit
) {
    val board = viewModel.standardBoard
    val piece = viewModel.trayPieces.collectAsState().value.getOrNull(selectedPieceIndex ?: -1)
    val previewEnabled = viewModel.prefs.isPlacementPreviewEnabled

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(piece) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 10f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 9)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 9)
                    onCellTapped(row, col)
                }
            }
    ) {
        val cellSize = size.width / 10f
        onBoardLayout(Offset.Zero, cellSize)

        // Draw cells
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                val cell = board.getCell(r, c)
                val x = c * cellSize
                val y = r * cellSize
                val padding = 2.5f

                if (cell != null && cell.isFilled) {
                    val blockColor = if (cell.isObstacle) Color(0xFF78909C)
                    else theme.blockColors.getOrElse(cell.colorIndex) { Color(0xFF00E5FF) }

                    // Glossy 3D Jewel Block
                    drawJewelBlock(
                        topLeft = Offset(x + padding, y + padding),
                        size = Size(cellSize - padding * 2, cellSize - padding * 2),
                        color = blockColor,
                        bombCountdown = cell.bombCountdown
                    )
                } else {
                    // Empty Cell
                    drawRoundRect(
                        color = theme.emptyCellColor,
                        topLeft = Offset(x + padding, y + padding),
                        size = Size(cellSize - padding * 2, cellSize - padding * 2),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = theme.emptyCellBorder,
                        topLeft = Offset(x + padding, y + padding),
                        size = Size(cellSize - padding * 2, cellSize - padding * 2),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1.2f)
                    )
                }
            }
        }

        // Placement Preview Overlay (Ghost Block)
        if (previewEnabled && piece != null && hoverCell != null) {
            val (hr, hc) = hoverCell
            if (board.canPlacePiece(piece, hr, hc)) {
                val previewColor = theme.blockColors.getOrElse(piece.colorIndex) { theme.accentColor }
                for ((pr, pc) in piece.normalizedCoordinates) {
                    val tr = hr + pr
                    val tc = hc + pc
                    if (tr in 0..9 && tc in 0..9) {
                        val px = tc * cellSize
                        val py = tr * cellSize
                        val padding = 2.5f

                        drawRoundRect(
                            color = previewColor.copy(alpha = 0.45f),
                            topLeft = Offset(px + padding, py + padding),
                            size = Size(cellSize - padding * 2, cellSize - padding * 2),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = previewColor,
                            topLeft = Offset(px + padding, py + padding),
                            size = Size(cellSize - padding * 2, cellSize - padding * 2),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }
    }
}

// Hexagonal Board Canvas for Mode 7
@Composable
fun HexBoardCanvas(
    viewModel: GameViewModel,
    theme: VisualTheme,
    hoverCell: Pair<Int, Int>?,
    selectedPieceIndex: Int?,
    onBoardLayout: (Offset, Float) -> Unit,
    onCellTapped: (Int, Int) -> Unit
) {
    val hexBoard = viewModel.hexBoard
    val piece = viewModel.trayPieces.collectAsState().value.getOrNull(selectedPieceIndex ?: -1)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(piece) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val hexRadius = size.width / 14f

                    // Find nearest hex cell to tap
                    var closestCoord: Pair<Int, Int>? = null
                    var minDist = Float.MAX_VALUE
                    for (coord in hexBoard.validCoords) {
                        val q = coord.first
                        val r = coord.second
                        val hx = center.x + hexRadius * (kotlin.math.sqrt(3f) * q + kotlin.math.sqrt(3f) / 2f * r)
                        val hy = center.y + hexRadius * (3f / 2f * r)
                        val dist = (Offset(hx, hy) - offset).getDistance()
                        if (dist < hexRadius && dist < minDist) {
                            minDist = dist
                            closestCoord = coord
                        }
                    }
                    if (closestCoord != null) {
                        onCellTapped(closestCoord.first, closestCoord.second)
                    }
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val hexRadius = size.width / 14f
        onBoardLayout(Offset.Zero, hexRadius * 2f)

        for (coord in hexBoard.validCoords) {
            val q = coord.first
            val r = coord.second
            val hx = center.x + hexRadius * (kotlin.math.sqrt(3f) * q + kotlin.math.sqrt(3f) / 2f * r)
            val hy = center.y + hexRadius * (3f / 2f * r)
            val cell = hexBoard.getCell(q, r)

            val isFilled = cell?.isFilled == true
            val fillColor = if (isFilled) {
                theme.blockColors.getOrElse(cell?.colorIndex ?: 0) { Color(0xFF00E5FF) }
            } else {
                theme.emptyCellColor
            }

            drawHexagon(
                center = Offset(hx, hy),
                radius = hexRadius * 0.92f,
                fillColor = fillColor,
                borderColor = if (isFilled) Color.White.copy(alpha = 0.5f) else theme.emptyCellBorder
            )
        }
    }
}

fun DrawScope.drawHexagon(
    center: Offset,
    radius: Float,
    fillColor: Color,
    borderColor: Color
) {
    val path = Path()
    for (i in 0 until 6) {
        val angleRad = (60f * i - 30f) * PI.toFloat() / 180f
        val x = center.x + radius * cos(angleRad)
        val y = center.y + radius * sin(angleRad)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = borderColor, style = Stroke(width = 1.5f))
}

// Draw Glossy 3D Jewel Block
fun DrawScope.drawJewelBlock(
    topLeft: Offset,
    size: Size,
    color: Color,
    bombCountdown: Int? = null
) {
    val cornerRadius = CornerRadius(7f, 7f)

    // Base Fill with subtle gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(color, color.copy(alpha = 0.85f)),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius
    )

    // Top Gloss Highlight
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
        size = Size(size.width - 4f, (size.height / 2f) - 2f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Outer Crisp Border
    drawRoundRect(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 1.5f)
    )

    // Bomb countdown circle indicator
    if (bombCountdown != null) {
        val center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)
        drawCircle(
            color = Color(0xFFD50000),
            radius = size.width * 0.35f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = size.width * 0.35f,
            center = center,
            style = Stroke(width = 1.5f)
        )
    }
}

// Tray Piece Preview Composable
@Composable
fun TrayPiecePreview(
    piece: BlockShape,
    theme: VisualTheme,
    isSelected: Boolean
) {
    val blockColor = theme.blockColors.getOrElse(piece.colorIndex) { Color(0xFF00E5FF) }

    Canvas(modifier = Modifier.size(75.dp)) {
        val rSpan = piece.rowSpan
        val cSpan = piece.colSpan
        val cellSize = (size.width / (maxOf(rSpan, cSpan, 3) + 0.5f)).coerceAtMost(22.dp.toPx())

        val totalW = cSpan * cellSize
        val totalH = rSpan * cellSize
        val startX = (size.width - totalW) / 2f
        val startY = (size.height - totalH) / 2f

        for ((r, c) in piece.normalizedCoordinates) {
            val bx = startX + c * cellSize
            val by = startY + r * cellSize
            val pad = 2f

            drawJewelBlock(
                topLeft = Offset(bx + pad, by + pad),
                size = Size(cellSize - pad * 2, cellSize - pad * 2),
                color = blockColor
            )
        }
    }
}

// In-game Modals
@Composable
fun PauseDialog(
    viewModel: GameViewModel,
    theme: VisualTheme,
    strings: com.example.model.LocalizedStrings,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.pause.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textColorPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = strings.resume)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.resume, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("restart_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = strings.restart)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.restart, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quit_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = strings.quit, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    viewModel: GameViewModel,
    theme: VisualTheme,
    strings: com.example.model.LocalizedStrings,
    score: Int,
    bestScore: Int,
    isNewRecord: Boolean,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isNewRecord) {
                    Text(
                        text = strings.newRecord,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFB300),
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = strings.gameOver.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textColorPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score Display
                Text(
                    text = "$score",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accentColor
                )
                Text(
                    text = "${strings.bestScore}: $bestScore",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textColorSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("game_over_replay_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = strings.tryAgain)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.tryAgain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("game_over_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = strings.quit, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun VictoryDialog(
    viewModel: GameViewModel,
    theme: VisualTheme,
    strings: com.example.model.LocalizedStrings,
    score: Int,
    onNext: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Stars Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.victory.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFB300),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "${strings.score}: $score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColorPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("victory_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                ) {
                    Text(text = strings.nextLevel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("victory_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = strings.quit, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
