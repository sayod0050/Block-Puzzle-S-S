package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameThemes
import com.example.model.VisualTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

@Composable
fun ThemesScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    val unlockedThemes by viewModel.unlockedThemes.collectAsState()
    val coins by viewModel.coins.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(currentTheme.backgroundStart, currentTheme.backgroundEnd)
                )
            )
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(44.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.HOME) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(currentTheme.cardBackground)
                        .testTag("themes_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = currentTheme.textColorPrimary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = strings.themes,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = currentTheme.textColorPrimary
                    )
                    Text(
                        text = "8 Visual Styles",
                        fontSize = 13.sp,
                        color = currentTheme.textColorSecondary
                    )
                }
            }

            // Coins Balance Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33FFB300))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Themes List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(GameThemes.allThemes) { themeItem ->
                val isUnlocked = unlockedThemes.contains(themeItem.id) || themeItem.isDefaultUnlocked
                val isEquipped = currentTheme.id == themeItem.id

                ThemeCard(
                    themeItem = themeItem,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    canAfford = coins >= themeItem.unlockCost,
                    strings = strings,
                    currentTheme = currentTheme,
                    onEquip = { viewModel.selectTheme(themeItem.id) },
                    onUnlock = { viewModel.unlockTheme(themeItem.id) }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    themeItem: VisualTheme,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    strings: com.example.model.LocalizedStrings,
    currentTheme: VisualTheme,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = currentTheme.cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .border(
                width = if (isEquipped) 2.dp else 0.dp,
                color = if (isEquipped) currentTheme.accentColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("theme_card_${themeItem.id.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = themeItem.nameKey(strings),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.textColorPrimary
                    )
                    Text(
                        text = if (themeItem.isDark) "Dark Theme" else "Light Theme",
                        fontSize = 12.sp,
                        color = currentTheme.textColorSecondary
                    )
                }

                // Action Button: Equipped / Equip / Unlock
                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentTheme.accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = strings.equipped,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.accentColor
                        )
                    }
                } else if (isUnlocked) {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(text = strings.equip, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onUnlock,
                        enabled = canAfford,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Unlock",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${themeItem.unlockCost}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Palette Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeItem.boardBackground)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                themeItem.blockColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
