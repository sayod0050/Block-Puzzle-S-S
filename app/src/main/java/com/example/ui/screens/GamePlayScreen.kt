package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AdventureStages
import com.example.model.BlockShape
import com.example.model.GameModeType
import com.example.model.GridCell
import com.example.model.HexCell
import com.example.model.VisualTheme
import com.example.ui.components.drawShinyBlock
import com.example.ui.components.drawShinyHexBlock
import com.example.ui.components.hexAxialToPixel
import com.example.ui.components.hexPiecePixelCentroid
import com.example.ui.components.pixelToHexAxial
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel
import kotlin.math.roundToInt

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
    val boardGrid by viewModel.boardGrid.collectAsState()
    val hexGrid by viewModel.hexGrid.collectAsState()
    val lastPlacedCells by viewModel.lastPlacedCells.collectAsState()
    val lastClearedCells by viewModel.lastClearedCells.collectAsState()
    val clearBurstId by viewModel.clearBurstId.collectAsState()
    val placeAnimId by viewModel.placeAnimId.collectAsState()
    val previewEnabled by viewModel.isPlacementPreviewEnabled.collectAsState()
    val reducedMotion by viewModel.isReducedMotionEnabled.collectAsState()

    // Coordinate tracking in root space
    var rootOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    var boardTopLeftInRoot by remember { mutableStateOf(Offset.Zero) }
    var boardWidthPx by remember { mutableFloatStateOf(0f) }
    val slotTopLeftMap = remember { mutableStateMapOf<Int, Offset>() }

    // Active drag state
    var draggingPieceIndex by remember { mutableStateOf<Int?>(null) }
    var dragCenterInRoot by remember { mutableStateOf(Offset.Zero) }
    var hoverGridCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var hoverIsValid by remember { mutableStateOf(false) }

    // Placement pop animation & screen shake
    val placeScale = remember { Animatable(1f) }
    val screenShakeX = remember { Animatable(0f) }
    val screenShakeY = remember { Animatable(0f) }

    LaunchedEffect(placeAnimId) {
        if (placeAnimId > 0L && !reducedMotion) {
            placeScale.snapTo(0.76f)
            placeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            screenShakeX.snapTo(kotlin.random.Random.nextFloat() * 6f - 3f)
            screenShakeY.snapTo(kotlin.random.Random.nextFloat() * 6f - 3f)
            screenShakeX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
            screenShakeY.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
        } else {
            placeScale.snapTo(1f)
        }
    }

    // Line-clear flash, boom particle bursts & heavy screen shake
    val clearFlashAlpha = remember { Animatable(0f) }
    var particleTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(clearBurstId) {
        if (clearBurstId > 0L && lastClearedCells.isNotEmpty() && boardWidthPx > 0f && !reducedMotion) {
            val isHex = mode == GameModeType.HEXA
            val centerOffset = Offset(boardWidthPx / 2f, boardWidthPx / 2f)
            viewModel.particleManager.spawnBoomBurst(centerOffset, theme.blockColors, count = if (combo >= 2) 75 else 45)

            screenShakeX.snapTo(kotlin.random.Random.nextFloat() * 16f - 8f)
            screenShakeY.snapTo(kotlin.random.Random.nextFloat() * 16f - 8f)
            screenShakeX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
            screenShakeY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))

            if (isHex) {
                val hexRadius = boardWidthPx / 13.5f
                val center = Offset(boardWidthPx / 2f, boardWidthPx / 2f)
                lastClearedCells.forEach { (q, r) ->
                    val px = center + hexAxialToPixel(q, r, hexRadius)
                    viewModel.particleManager.spawnLineClearBurst(
                        center = px,
                        colors = theme.blockColors,
                        count = 12
                    )
                }
            } else {
                val cellSize = boardWidthPx / 10f
                lastClearedCells.forEach { (r, c) ->
                    val px = Offset(c * cellSize + cellSize / 2f, r * cellSize + cellSize / 2f)
                    viewModel.particleManager.spawnLineClearBurst(
                        center = px,
                        colors = theme.blockColors,
                        count = 12
                    )
                }
            }

            clearFlashAlpha.snapTo(0.95f)
            var lastTime = 0L
            while (viewModel.particleManager.particles.isNotEmpty() || clearFlashAlpha.value > 0.01f) {
                withFrameNanos { frameTime ->
                    val dt = if (lastTime == 0L) 0.016f else ((frameTime - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastTime = frameTime
                    viewModel.particleManager.update(dt)
                    particleTick++
                }
                if (clearFlashAlpha.value > 0f) {
                    clearFlashAlpha.snapTo((clearFlashAlpha.value - 0.05f).coerceAtLeast(0f))
                }
            }
        }
    }

    // Score scale animation on score change
    val scoreScale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        if (score > 0 && !reducedMotion) {
            scoreScale.animateTo(1.22f, tween(100))
            scoreScale.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
        }
    }

    // Latest references for pointerInput closures
    val currentBoardTopLeft by rememberUpdatedState(boardTopLeftInRoot)
    val currentBoardWidthPx by rememberUpdatedState(boardWidthPx)
    val currentMode by rememberUpdatedState(mode)
    val currentIsBlocked by rememberUpdatedState(isPaused || isGameOver || isVictory)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootOriginInRoot = coords.positionInRoot()
            }
            .background(
                Brush.verticalGradient(
                    listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // TOP HUD: Pause, Score, Best, Mode Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .size(48.dp)
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
                        modifier = Modifier
                            .scale(scoreScale.value)
                            .testTag("score_value")
                    )
                }

                if (mode == GameModeType.ZEN) {
                    IconButton(
                        onClick = { viewModel.undoZenMove() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(theme.cardBackground)
                            .testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-HUD for Mode Specific Telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    GameModeType.PUZZLE_CHALLENGE -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x227C4DFF))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.movesLeft}: $movesLeft  •  ${strings.targetGoal}: ${GameViewModel.PUZZLE_TARGET_SCORE}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB388FF)
                            )
                        }
                    }
                    GameModeType.DAILY_CHALLENGE -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x227C4DFF))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.movesLeft}: $movesLeft  •  ${strings.targetGoal}: ${GameViewModel.DAILY_TARGET_SCORE}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB388FF)
                            )
                        }
                    }
                    GameModeType.ADVENTURE -> {
                        val advLevel by viewModel.adventureLevel.collectAsState()
                        val stage = AdventureStages.getStage(advLevel)
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
                            text = if (selectedPieceIndex != null) strings.tapToPlace else strings.dragToPlace,
                            fontSize = 12.sp,
                            color = theme.textColorSecondary.copy(alpha = 0.85f)
                        )
                    }
                }

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
                val boardDim = minOf(maxWidth, maxHeight)

                Box(
                    modifier = Modifier
                        .size(boardDim)
                        .offset(x = screenShakeX.value.dp, y = screenShakeY.value.dp)
                        .shadow(16.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(theme.boardBackground)
                        .border(3.dp, theme.boardBorder, RoundedCornerShape(22.dp))
                        .padding(4.dp)
                        .onGloballyPositioned { coords ->
                            boardTopLeftInRoot = coords.positionInRoot() - rootOriginInRoot
                            boardWidthPx = coords.size.width.toFloat()
                        }
                        .testTag("game_board")
                ) {
                    val activePreviewIdx = draggingPieceIndex ?: selectedPieceIndex
                    val activePreviewPiece = trayPieces.getOrNull(activePreviewIdx ?: -1)

                    if (mode == GameModeType.HEXA) {
                        HexBoardCanvas(
                            viewModel = viewModel,
                            hexGrid = hexGrid,
                            theme = theme,
                            hoverCell = hoverGridCell,
                            hoverIsValid = hoverIsValid,
                            previewPiece = activePreviewPiece,
                            previewEnabled = previewEnabled,
                            clearFlashAlpha = clearFlashAlpha.value,
                            lastClearedCells = lastClearedCells,
                            particleTick = particleTick,
                            onCellTapped = { q, r ->
                                viewModel.tryPlaceSelectedPiece(q, r)
                            }
                        )
                    } else {
                        StandardBoardCanvas(
                            viewModel = viewModel,
                            boardGrid = boardGrid,
                            theme = theme,
                            hoverCell = hoverGridCell,
                            hoverIsValid = hoverIsValid,
                            previewPiece = activePreviewPiece,
                            previewEnabled = previewEnabled,
                            lastPlacedCells = lastPlacedCells,
                            placeScale = placeScale.value,
                            clearFlashAlpha = clearFlashAlpha.value,
                            lastClearedCells = lastClearedCells,
                            particleTick = particleTick,
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
                                .scale(placeScale.value)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7C4DFF), Color(0xFFFF3366), Color(0xFFFFB300))
                                    )
                                )
                                .border(3.dp, Color.White, RoundedCornerShape(20.dp))
                                .shadow(20.dp, RoundedCornerShape(20.dp))
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = comboItem.text,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
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
                    .height(126.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.cardBackground.copy(alpha = 0.72f))
                    .border(1.dp, theme.boardBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                trayPieces.forEachIndexed { index, piece ->
                    val isDraggingThis = draggingPieceIndex == index
                    val isSelected = selectedPieceIndex == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .padding(4.dp)
                            .onGloballyPositioned { coords ->
                                slotTopLeftMap[index] = coords.positionInRoot() - rootOriginInRoot
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) theme.accentColor.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) theme.accentColor else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("tray_piece_$index")
                            .semantics {
                                onClick {
                                    if (piece != null) {
                                        viewModel.selectPieceForPlacement(index)
                                        true
                                    } else false
                                }
                            }
                            .pointerInput(index, piece?.id) {
                                if (piece == null) return@pointerInput

                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    if (currentIsBlocked) return@awaitEachGesture

                                    val startLocal = down.position
                                    val slotOrigin = slotTopLeftMap[index] ?: Offset.Zero
                                    val isMouse = down.type == PointerType.Mouse
                                    val touchSlop = viewConfiguration.touchSlop
                                    var isDragging = false

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: event.changes.firstOrNull()
                                            ?: break

                                        if (!change.pressed) {
                                            // Pointer released
                                            if (isDragging) {
                                                val hover = hoverGridCell
                                                if (hover != null && hoverIsValid) {
                                                    val placed = if (currentMode == GameModeType.HEXA) {
                                                        viewModel.tryPlaceHexPiece(index, piece, hover.first, hover.second)
                                                    } else {
                                                        viewModel.tryPlaceStandardPiece(index, piece, hover.first, hover.second)
                                                    }
                                                    if (!placed) {
                                                        viewModel.soundManager.playButtonClick()
                                                    }
                                                }
                                                draggingPieceIndex = null
                                                hoverGridCell = null
                                                hoverIsValid = false
                                            } else {
                                                // Quick tap -> select/deselect for Tap-to-Place
                                                viewModel.selectPieceForPlacement(index)
                                            }
                                            break
                                        }

                                        val distFromStart = (change.position - startLocal).getDistance()
                                        if (!isDragging && distFromStart > touchSlop) {
                                            isDragging = true
                                            draggingPieceIndex = index
                                            viewModel.clearSelection()
                                            viewModel.soundManager.playBlockPickup()
                                            viewModel.hapticManager.vibrateClick()
                                        }

                                        if (isDragging) {
                                            if (change.positionChanged()) {
                                                change.consume()
                                            }
                                            val bWidth = currentBoardWidthPx
                                            val bTopLeft = currentBoardTopLeft
                                            if (bWidth > 0f) {
                                                val pointerRoot = slotOrigin + change.position
                                                if (currentMode == GameModeType.HEXA) {
                                                    val hexRadius = bWidth / 13.5f
                                                    val liftY = if (isMouse) 0f else hexRadius * 2.2f
                                                    val liftedCenter = pointerRoot - Offset(0f, liftY)
                                                    dragCenterInRoot = liftedCenter

                                                    val boardCenter = bTopLeft + Offset(bWidth / 2f, bWidth / 2f)
                                                    val centroid = hexPiecePixelCentroid(piece, hexRadius)
                                                    val anchorPixel = liftedCenter - centroid
                                                    val (q, r) = pixelToHexAxial(
                                                        anchorPixel.x - boardCenter.x,
                                                        anchorPixel.y - boardCenter.y,
                                                        hexRadius
                                                    )
                                                    val insideHexArea = (liftedCenter - boardCenter).getDistance() <= bWidth * 0.58f
                                                    if (insideHexArea) {
                                                        hoverGridCell = Pair(q, r)
                                                        hoverIsValid = viewModel.hexBoard.canPlacePiece(piece, q, r)
                                                    } else {
                                                        hoverGridCell = null
                                                        hoverIsValid = false
                                                    }
                                                } else {
                                                    val cellSize = bWidth / 10f
                                                    val liftY = if (isMouse) 0f else cellSize * 1.45f
                                                    val liftedCenter = pointerRoot - Offset(0f, liftY)
                                                    dragCenterInRoot = liftedCenter

                                                    val pieceW = piece.colSpan * cellSize
                                                    val pieceH = piece.rowSpan * cellSize
                                                    val pieceTopLeft = liftedCenter - Offset(pieceW / 2f, pieceH / 2f)

                                                    val targetCol = ((pieceTopLeft.x - bTopLeft.x) / cellSize).roundToInt()
                                                    val targetRow = ((pieceTopLeft.y - bTopLeft.y) / cellSize).roundToInt()

                                                    val insideX = liftedCenter.x in (bTopLeft.x - cellSize)..(bTopLeft.x + bWidth + cellSize)
                                                    val insideY = liftedCenter.y in (bTopLeft.y - cellSize)..(bTopLeft.y + bWidth + cellSize)
                                                    if (insideX && insideY) {
                                                        hoverGridCell = Pair(targetRow, targetCol)
                                                        hoverIsValid = viewModel.standardBoard.canPlacePiece(piece, targetRow, targetCol)
                                                    } else {
                                                        hoverGridCell = null
                                                        hoverIsValid = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null && !isDraggingThis) {
                            TrayPiecePreview(
                                piece = piece,
                                theme = theme
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // FLOATING DRAGGED PIECE OVERLAY (at exact 1:1 board scale)
        val activeDraggedPiece = draggingPieceIndex?.let { trayPieces.getOrNull(it) }
        if (activeDraggedPiece != null && boardWidthPx > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val blockColor = theme.blockColors.getOrElse(activeDraggedPiece.colorIndex) { theme.accentColor }
                if (mode == GameModeType.HEXA) {
                    val hexRadius = boardWidthPx / 13.5f
                    val centroid = hexPiecePixelCentroid(activeDraggedPiece, hexRadius)
                    val origin = dragCenterInRoot - centroid
                    for ((dq, dr) in activeDraggedPiece.normalizedCoordinates) {
                        val center = origin + hexAxialToPixel(dq, dr, hexRadius)
                        drawShinyHexBlock(
                            center = center,
                            radius = hexRadius * 0.94f,
                            color = blockColor,
                            isFilled = true,
                            borderColor = Color.White,
                            alpha = 0.96f,
                            glowIntensity = 0.6f
                        )
                    }
                } else {
                    val cellSize = boardWidthPx / 10f
                    val pieceW = activeDraggedPiece.colSpan * cellSize
                    val pieceH = activeDraggedPiece.rowSpan * cellSize
                    val topLeft = dragCenterInRoot - Offset(pieceW / 2f, pieceH / 2f)
                    val padding = 2.5f

                    for ((pr, pc) in activeDraggedPiece.normalizedCoordinates) {
                        val bx = topLeft.x + pc * cellSize
                        val by = topLeft.y + pr * cellSize
                        drawShinyBlock(
                            topLeft = Offset(bx + padding, by + padding),
                            size = Size(cellSize - padding * 2f, cellSize - padding * 2f),
                            color = blockColor,
                            alpha = 0.96f,
                            scale = 1.04f,
                            glowIntensity = 0.55f
                        )
                    }
                }
            }
        }

        // Achievement Toast Popup
        AnimatedVisibility(
            visible = achievementPopup != null,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(350)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
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

        if (isPaused) {
            PauseDialog(
                theme = theme,
                strings = strings,
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.startMode(mode, viewModel.adventureLevel.value) },
                onQuit = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }

        if (isGameOver) {
            GameOverDialog(
                theme = theme,
                strings = strings,
                score = score,
                bestScore = bestScore,
                isNewRecord = newRecord,
                onRestart = { viewModel.startMode(mode, viewModel.adventureLevel.value) },
                onQuit = { viewModel.navigateTo(GameScreen.HOME) }
            )
        }

        if (isVictory) {
            VictoryDialog(
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

@Composable
fun StandardBoardCanvas(
    viewModel: GameViewModel,
    boardGrid: List<List<GridCell>>,
    theme: VisualTheme,
    hoverCell: Pair<Int, Int>?,
    hoverIsValid: Boolean,
    previewPiece: BlockShape?,
    previewEnabled: Boolean,
    lastPlacedCells: Set<Pair<Int, Int>>,
    placeScale: Float,
    clearFlashAlpha: Float,
    lastClearedCells: List<Pair<Int, Int>>,
    particleTick: Int,
    onCellTapped: (Int, Int) -> Unit
) {
    val currentOnCellTapped by rememberUpdatedState(onCellTapped)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 10f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 9)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 9)
                    currentOnCellTapped(row, col)
                }
            }
    ) {
        // Touch particleTick so Canvas redraws during particle animations
        @Suppress("UNUSED_VARIABLE")
        val frameTick = particleTick

        val cellSize = size.width / 10f
        val padding = 2.5f

        val willClearCells = if (previewEnabled && previewPiece != null && hoverCell != null && hoverIsValid) {
            viewModel.standardBoard.getWillClearCells(previewPiece, hoverCell.first, hoverCell.second)
        } else {
            emptySet()
        }

        // Draw empty cells & filled shiny blocks
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                val cell = boardGrid.getOrNull(r)?.getOrNull(c)
                val x = c * cellSize
                val y = r * cellSize
                val cellPos = Offset(x + padding, y + padding)
                val cellDim = Size(cellSize - padding * 2f, cellSize - padding * 2f)

                if (cell != null && cell.isFilled) {
                    val blockColor = theme.blockColors.getOrElse(cell.colorIndex) { Color(0xFF00E5FF) }
                    val isNewlyPlaced = lastPlacedCells.contains(Pair(r, c))
                    val isWillClear = willClearCells.contains(Pair(r, c))

                    drawShinyBlock(
                        topLeft = cellPos,
                        size = cellDim,
                        color = blockColor,
                        bombCountdown = cell.bombCountdown,
                        isObstacle = cell.isObstacle,
                        scale = if (isNewlyPlaced) placeScale else 1f,
                        glowIntensity = if (isWillClear) 0.9f else 0f
                    )
                } else {
                    val corner = CornerRadius(6f, 6f)
                    drawRoundRect(
                        color = theme.emptyCellColor,
                        topLeft = cellPos,
                        size = cellDim,
                        cornerRadius = corner
                    )
                    drawRoundRect(
                        color = theme.emptyCellBorder,
                        topLeft = cellPos,
                        size = cellDim,
                        cornerRadius = corner,
                        style = Stroke(width = 1.2f)
                    )
                }
            }
        }

        // Placement Preview Ghost Overlay & Predictive Line Clear Glow
        if (previewEnabled && previewPiece != null && hoverCell != null) {
            val (hr, hc) = hoverCell
            val previewColor = if (hoverIsValid) {
                theme.blockColors.getOrElse(previewPiece.colorIndex) { theme.accentColor }
            } else {
                Color(0xFFFF3366)
            }

            // Highlight rows/columns about to clear
            if (hoverIsValid && willClearCells.isNotEmpty()) {
                for ((wr, wc) in willClearCells) {
                    val wx = wc * cellSize + padding
                    val wy = wr * cellSize + padding
                    drawRoundRect(
                        color = previewColor.copy(alpha = 0.28f),
                        topLeft = Offset(wx, wy),
                        size = Size(cellSize - padding * 2f, cellSize - padding * 2f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        topLeft = Offset(wx, wy),
                        size = Size(cellSize - padding * 2f, cellSize - padding * 2f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1.8f)
                    )
                }
            }

            // Ghost piece blocks
            for ((pr, pc) in previewPiece.normalizedCoordinates) {
                val tr = hr + pr
                val tc = hc + pc
                if (tr in 0..9 && tc in 0..9) {
                    val px = tc * cellSize + padding
                    val py = tr * cellSize + padding
                    val cellDim = Size(cellSize - padding * 2f, cellSize - padding * 2f)

                    if (hoverIsValid) {
                        drawShinyBlock(
                            topLeft = Offset(px, py),
                            size = cellDim,
                            color = previewColor,
                            alpha = 0.48f,
                            glowIntensity = 0.5f
                        )
                    } else {
                        drawRoundRect(
                            color = Color(0x44FF3366),
                            topLeft = Offset(px, py),
                            size = cellDim,
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xCCFF3366),
                            topLeft = Offset(px, py),
                            size = cellDim,
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 2.2f)
                        )
                    }
                }
            }
        }

        // Line-clear flash sweep effect
        if (clearFlashAlpha > 0.01f && lastClearedCells.isNotEmpty()) {
            for ((cr, cc) in lastClearedCells) {
                val fx = cc * cellSize + padding
                val fy = cr * cellSize + padding
                drawRoundRect(
                    color = Color.White.copy(alpha = clearFlashAlpha * 0.85f),
                    topLeft = Offset(fx, fy),
                    size = Size(cellSize - padding * 2f, cellSize - padding * 2f),
                    cornerRadius = CornerRadius(7f, 7f)
                )
            }
        }

        // Active particles
        for (p in viewModel.particleManager.particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = p.position
            )
            drawCircle(
                color = Color.White.copy(alpha = p.alpha * 0.6f),
                radius = p.size * 0.4f,
                center = p.position
            )
        }
    }
}

@Composable
fun HexBoardCanvas(
    viewModel: GameViewModel,
    hexGrid: Map<Pair<Int, Int>, HexCell>,
    theme: VisualTheme,
    hoverCell: Pair<Int, Int>?,
    hoverIsValid: Boolean,
    previewPiece: BlockShape?,
    previewEnabled: Boolean,
    clearFlashAlpha: Float,
    lastClearedCells: List<Pair<Int, Int>>,
    particleTick: Int,
    onCellTapped: (Int, Int) -> Unit
) {
    val hexBoard = viewModel.hexBoard
    val currentOnCellTapped by rememberUpdatedState(onCellTapped)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val hexRadius = size.width / 13.5f
                    val (q, r) = pixelToHexAxial(offset.x - center.x, offset.y - center.y, hexRadius)
                    if (hexBoard.validCoords.contains(Pair(q, r))) {
                        currentOnCellTapped(q, r)
                    }
                }
            }
    ) {
        @Suppress("UNUSED_VARIABLE")
        val frameTick = particleTick

        val center = Offset(size.width / 2f, size.height / 2f)
        val hexRadius = size.width / 13.5f

        val willClearCells = if (previewEnabled && previewPiece != null && hoverCell != null && hoverIsValid) {
            hexBoard.getWillClearCells(previewPiece, hoverCell.first, hoverCell.second)
        } else {
            emptySet()
        }

        for (coord in hexBoard.validCoords) {
            val q = coord.first
            val r = coord.second
            val cellCenter = center + hexAxialToPixel(q, r, hexRadius)
            val cell = hexGrid[coord]

            val isFilled = cell?.isFilled == true
            val fillColor = if (isFilled) {
                theme.blockColors.getOrElse(cell?.colorIndex ?: 0) { Color(0xFF00E5FF) }
            } else {
                theme.emptyCellColor
            }

            drawShinyHexBlock(
                center = cellCenter,
                radius = hexRadius * 0.92f,
                color = fillColor,
                isFilled = isFilled,
                borderColor = if (isFilled) Color.White.copy(alpha = 0.5f) else theme.emptyCellBorder,
                glowIntensity = if (willClearCells.contains(coord)) 0.85f else 0f
            )
        }

        // Ghost preview for Hexa mode
        if (previewEnabled && previewPiece != null && hoverCell != null) {
            val (hq, hr) = hoverCell
            val previewColor = if (hoverIsValid) {
                theme.blockColors.getOrElse(previewPiece.colorIndex) { theme.accentColor }
            } else {
                Color(0xFFFF3366)
            }
            for ((dq, dr) in previewPiece.normalizedCoordinates) {
                val targetCoord = Pair(hq + dq, hr + dr)
                if (hexBoard.validCoords.contains(targetCoord)) {
                    val px = center + hexAxialToPixel(targetCoord.first, targetCoord.second, hexRadius)
                    drawShinyHexBlock(
                        center = px,
                        radius = hexRadius * 0.92f,
                        color = previewColor,
                        isFilled = true,
                        borderColor = Color.White,
                        alpha = if (hoverIsValid) 0.45f else 0.35f
                    )
                }
            }
        }

        // Flash & particles
        if (clearFlashAlpha > 0.01f && lastClearedCells.isNotEmpty()) {
            for ((q, r) in lastClearedCells) {
                val px = center + hexAxialToPixel(q, r, hexRadius)
                drawShinyHexBlock(
                    center = px,
                    radius = hexRadius * 0.92f,
                    color = Color.White,
                    isFilled = true,
                    borderColor = Color.White,
                    alpha = clearFlashAlpha * 0.8f
                )
            }
        }

        for (p in viewModel.particleManager.particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = p.position
            )
        }
    }
}

@Composable
fun TrayPiecePreview(
    piece: BlockShape,
    theme: VisualTheme
) {
    val blockColor = theme.blockColors.getOrElse(piece.colorIndex) { Color(0xFF00E5FF) }

    Canvas(modifier = Modifier.size(84.dp)) {
        if (piece.isHex) {
            val hexRadius = (size.width / 8.5f).coerceAtMost(13.dp.toPx())
            val centroid = hexPiecePixelCentroid(piece, hexRadius)
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)
            for ((dq, dr) in piece.normalizedCoordinates) {
                val pos = canvasCenter + hexAxialToPixel(dq, dr, hexRadius) - centroid
                drawShinyHexBlock(
                    center = pos,
                    radius = hexRadius * 0.90f,
                    color = blockColor,
                    isFilled = true,
                    borderColor = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            val rSpan = piece.rowSpan
            val cSpan = piece.colSpan
            val cellSize = (size.width / (maxOf(rSpan, cSpan, 3) + 0.6f)).coerceAtMost(22.dp.toPx())

            val totalW = cSpan * cellSize
            val totalH = rSpan * cellSize
            val startX = (size.width - totalW) / 2f
            val startY = (size.height - totalH) / 2f
            val pad = 1.8f

            for ((r, c) in piece.normalizedCoordinates) {
                val bx = startX + c * cellSize
                val by = startY + r * cellSize

                drawShinyBlock(
                    topLeft = Offset(bx + pad, by + pad),
                    size = Size(cellSize - pad * 2f, cellSize - pad * 2f),
                    color = blockColor
                )
            }
        }
    }
}

@Composable
fun PauseDialog(
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
