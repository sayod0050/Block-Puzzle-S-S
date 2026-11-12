package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModeInfo
import com.example.model.GameModeType
import com.example.model.GameModes
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

@Composable
fun GameModesScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val theme by viewModel.theme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(44.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.HOME) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(theme.cardBackground)
                    .testTag("modes_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.back,
                    tint = theme.textColorPrimary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = strings.gameModes,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textColorPrimary
                )
                Text(
                    text = "10 Unique Challenges",
                    fontSize = 13.sp,
                    color = theme.textColorSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Modes List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(GameModes.allModes) { modeInfo ->
                ModeListItem(
                    modeInfo = modeInfo,
                    highScore = viewModel.prefs.getHighScore(modeInfo.type),
                    theme = theme,
                    strings = strings,
                    onSelect = {
                        if (modeInfo.type == GameModeType.ADVENTURE) {
                            viewModel.navigateTo(GameScreen.ADVENTURE_MAP)
                        } else if (modeInfo.type == GameModeType.DAILY_CHALLENGE) {
                            viewModel.navigateTo(GameScreen.DAILY_CHALLENGE)
                        } else {
                            viewModel.startMode(modeInfo.type)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ModeListItem(
    modeInfo: GameModeInfo,
    highScore: Int,
    theme: com.example.model.VisualTheme,
    strings: com.example.model.LocalizedStrings,
    onSelect: () -> Unit
) {
    val modeIcon: ImageVector = when (modeInfo.type) {
        GameModeType.CLASSIC -> Icons.Default.GridView
        GameModeType.TIME_ATTACK -> Icons.Default.Timer
        GameModeType.ZEN -> Icons.Default.Spa
        GameModeType.BOMB_CHALLENGE -> Icons.Default.Emergency
        GameModeType.ADVENTURE -> Icons.Default.Map
        GameModeType.DAILY_CHALLENGE -> Icons.Default.Event
        GameModeType.HEXA -> Icons.Default.Hexagon
        GameModeType.PUZZLE_CHALLENGE -> Icons.Default.Psychology
        GameModeType.COMBO_RUSH -> Icons.Default.Bolt
        GameModeType.ENDLESS -> Icons.Default.AllInclusive
    }

    val accentColor = theme.blockColors.getOrElse(modeInfo.type.ordinal % theme.blockColors.size) {
        theme.accentColor
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .testTag("mode_item_${modeInfo.type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = modeIcon,
                    contentDescription = modeInfo.titleKey(strings),
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modeInfo.titleKey(strings),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColorPrimary
                )
                Text(
                    text = modeInfo.descKey(strings),
                    fontSize = 12.sp,
                    color = theme.textColorSecondary,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                if (highScore > 0) {
                    Text(
                        text = "${strings.bestScore}: $highScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = strings.play,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
