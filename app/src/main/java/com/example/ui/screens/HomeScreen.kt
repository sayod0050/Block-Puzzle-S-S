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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModeType
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

@Composable
fun HomeScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val dailyStreak = viewModel.prefs.dailyStreak
    val totalStars = viewModel.prefs.getTotalAdventureStars()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(theme.backgroundStart, theme.backgroundEnd)
                )
            )
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(44.dp))

        // Top Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(theme.accentColor, theme.blockColors.getOrElse(1) { Color(0xFFFF3366) })
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = playerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColorPrimary
                    )
                    Text(
                        text = "S&S Studio",
                        fontSize = 12.sp,
                        color = theme.textColorSecondary
                    )
                }
            }

            // Coins & Settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Coins pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x33FFB300))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.SETTINGS) },
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.settings,
                        tint = theme.textColorPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Play Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.appTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textColorPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = strings.creators,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.accentColor,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Big Play Button
                Button(
                    onClick = { viewModel.startMode(GameModeType.CLASSIC) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("play_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = strings.play,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.play.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Grid Menu Options
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                MenuGridCard(
                    title = strings.gameModes,
                    subtitle = "10 Modes",
                    icon = Icons.Default.GridView,
                    accentColor = theme.blockColors.getOrElse(0) { Color(0xFF00E5FF) },
                    theme = theme,
                    testTag = "modes_card"
                ) {
                    viewModel.navigateTo(GameScreen.GAME_MODES)
                }
            }

            item {
                MenuGridCard(
                    title = strings.dailyChallenge,
                    subtitle = if (dailyStreak > 0) "$dailyStreak ${strings.days}" else strings.todayChallenge,
                    icon = Icons.Default.Event,
                    accentColor = theme.blockColors.getOrElse(1) { Color(0xFFFF3366) },
                    theme = theme,
                    testTag = "daily_card"
                ) {
                    viewModel.navigateTo(GameScreen.DAILY_CHALLENGE)
                }
            }

            item {
                MenuGridCard(
                    title = strings.adventure,
                    subtitle = "$totalStars / 60 ${strings.stars}",
                    icon = Icons.Default.Map,
                    accentColor = theme.blockColors.getOrElse(2) { Color(0xFFFFB300) },
                    theme = theme,
                    testTag = "adventure_card"
                ) {
                    viewModel.navigateTo(GameScreen.ADVENTURE_MAP)
                }
            }

            item {
                MenuGridCard(
                    title = strings.themes,
                    subtitle = "8 Styles",
                    icon = Icons.Default.Palette,
                    accentColor = theme.blockColors.getOrElse(3) { Color(0xFF00E676) },
                    theme = theme,
                    testTag = "themes_card"
                ) {
                    viewModel.navigateTo(GameScreen.THEMES)
                }
            }

            item {
                MenuGridCard(
                    title = strings.achievements,
                    subtitle = "Trophies",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = theme.blockColors.getOrElse(4) { Color(0xFF7C4DFF) },
                    theme = theme,
                    testTag = "achievements_card"
                ) {
                    viewModel.navigateTo(GameScreen.ACHIEVEMENTS)
                }
            }

            item {
                MenuGridCard(
                    title = strings.statistics,
                    subtitle = strings.leaderboard,
                    icon = Icons.Default.Leaderboard,
                    accentColor = theme.blockColors.getOrElse(5) { Color(0xFFFF6D00) },
                    theme = theme,
                    testTag = "statistics_card"
                ) {
                    viewModel.navigateTo(GameScreen.STATISTICS)
                }
            }

            item {
                MenuGridCard(
                    title = strings.about,
                    subtitle = "S&S Studio",
                    icon = Icons.Default.Info,
                    accentColor = theme.accentColor,
                    theme = theme,
                    testTag = "about_card"
                ) {
                    viewModel.navigateTo(GameScreen.ABOUT)
                }
            }
        }
    }
}

@Composable
fun MenuGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    theme: com.example.model.VisualTheme,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColorPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = theme.textColorSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
