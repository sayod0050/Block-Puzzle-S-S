package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import com.example.ui.components.drawShinyBlock

@Composable
fun SplashScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val theme by viewModel.theme.collectAsState()

    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val pulse = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        pulse.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(1800)
        viewModel.navigateTo(GameScreen.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.navigateTo(GameScreen.HOME)
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // S&S Emblem Icon with glossy shiny 3D blocks
            Box(
                modifier = Modifier
                    .scale(scale.value * pulse.value)
                    .size(134.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2A2D4A), Color(0xFF141624))
                        )
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gap = 6.dp.toPx()
                    val tileSize = (size.width - gap) / 2f
                    val colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFFFF3366),
                        Color(0xFFFFB300),
                        Color(0xFF00E676)
                    )
                    drawShinyBlock(Offset(0f, 0f), Size(tileSize, tileSize), colors[0], glowIntensity = 0.4f)
                    drawShinyBlock(Offset(tileSize + gap, 0f), Size(tileSize, tileSize), colors[1], glowIntensity = 0.4f)
                    drawShinyBlock(Offset(0f, tileSize + gap), Size(tileSize, tileSize), colors[2], glowIntensity = 0.4f)
                    drawShinyBlock(Offset(tileSize + gap, tileSize + gap), Size(tileSize, tileSize), colors[3], glowIntensity = 0.4f)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Studio Abbreviation Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x3300E5FF))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "S & S",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Title
            Text(
                text = strings.appTitle,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = theme.textColorPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Creators Credit
            Text(
                text = strings.creators,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textColorSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Slogan
            Text(
                text = strings.slogan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = theme.textColorSecondary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Tap to continue footer
        Text(
            text = strings.play,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.accentColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(pulse.value - 0.2f)
        )
    }
}
