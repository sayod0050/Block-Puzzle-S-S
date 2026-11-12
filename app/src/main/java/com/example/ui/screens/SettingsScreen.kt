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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.AppLanguage
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val strings by viewModel.strings.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    var masterVol by remember { mutableFloatStateOf(viewModel.prefs.masterVolume) }
    var musicVol by remember { mutableFloatStateOf(viewModel.prefs.musicVolume) }
    var sfxVol by remember { mutableFloatStateOf(viewModel.prefs.sfxVolume) }

    var isMusicOn by remember { mutableStateOf(viewModel.prefs.isMusicEnabled) }
    var isSfxOn by remember { mutableStateOf(viewModel.prefs.isSfxEnabled) }
    var isVibrationOn by remember { mutableStateOf(viewModel.prefs.isVibrationEnabled) }
    var isPreviewOn by remember { mutableStateOf(viewModel.prefs.isPlacementPreviewEnabled) }
    var isReducedMotionOn by remember { mutableStateOf(viewModel.prefs.isReducedMotionEnabled) }

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
                    .testTag("settings_back_button")
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
                    text = strings.settings,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textColorPrimary
                )
                Text(
                    text = "Preferences & Controls",
                    fontSize = 13.sp,
                    color = theme.textColorSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Language Selection Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = strings.language,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColorPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = language == lang
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) theme.accentColor
                                            else theme.emptyCellColor
                                        )
                                        .clickable { viewModel.setLanguage(lang) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else theme.textColorPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Audio Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Audio & Sound",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColorPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Music Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.music, color = theme.textColorPrimary, fontSize = 15.sp)
                            Switch(
                                checked = isMusicOn,
                                onCheckedChange = {
                                    isMusicOn = it
                                    viewModel.toggleMusic(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        // SFX Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.soundEffects, color = theme.textColorPrimary, fontSize = 15.sp)
                            Switch(
                                checked = isSfxOn,
                                onCheckedChange = {
                                    isSfxOn = it
                                    viewModel.toggleSfx(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Master Volume Slider
                        Text(
                            text = "${strings.masterVolume}: ${(masterVol * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = theme.textColorSecondary
                        )
                        Slider(
                            value = masterVol,
                            onValueChange = {
                                masterVol = it
                                viewModel.updateVolumes(masterVol, musicVol, sfxVol)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.accentColor
                            )
                        )
                    }
                }
            }

            // Gameplay & Visual Toggles
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Gameplay & Feel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textColorPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vibration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.vibration, color = theme.textColorPrimary, fontSize = 15.sp)
                            Switch(
                                checked = isVibrationOn,
                                onCheckedChange = {
                                    isVibrationOn = it
                                    viewModel.toggleVibration(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        // Placement Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.placementPreview, color = theme.textColorPrimary, fontSize = 15.sp)
                            Switch(
                                checked = isPreviewOn,
                                onCheckedChange = {
                                    isPreviewOn = it
                                    viewModel.togglePreview(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        // Reduced Motion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strings.reducedMotion, color = theme.textColorPrimary, fontSize = 15.sp)
                            Switch(
                                checked = isReducedMotionOn,
                                onCheckedChange = {
                                    isReducedMotionOn = it
                                    viewModel.toggleReducedMotion(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }
                    }
                }
            }

            // Reset Progress Action
            item {
                Button(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF3366)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("reset_progress_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF3366)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.resetProgress,
                        color = Color(0xFFFF3366),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Reset Confirmation Alert Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(strings.resetConfirmTitle) },
            text = { Text(strings.resetConfirmMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
