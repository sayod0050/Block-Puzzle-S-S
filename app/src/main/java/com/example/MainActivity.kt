package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AdventureMapScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameModesScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private var activeViewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val gameViewModel: GameViewModel = viewModel()
                    activeViewModel = gameViewModel
                    GameAppNavigation(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activeViewModel?.soundManager?.stopBackgroundMusic()
    }

    override fun onResume() {
        super.onResume()
        if (activeViewModel?.currentScreen?.value == GameScreen.GAME_PLAY) {
            val isZen = activeViewModel?.activeMode?.value == com.example.model.GameModeType.ZEN
            activeViewModel?.soundManager?.startBackgroundMusic(isZenMode = isZen)
        }
    }
}

@Composable
fun GameAppNavigation(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle system back navigation to return to Home screen
    if (currentScreen != GameScreen.HOME && currentScreen != GameScreen.SPLASH) {
        BackHandler {
            viewModel.navigateTo(GameScreen.HOME)
        }
    }

    when (currentScreen) {
        GameScreen.SPLASH -> SplashScreen(viewModel = viewModel)
        GameScreen.HOME -> HomeScreen(viewModel = viewModel)
        GameScreen.GAME_MODES -> GameModesScreen(viewModel = viewModel)
        GameScreen.GAME_PLAY -> GamePlayScreen(viewModel = viewModel)
        GameScreen.ADVENTURE_MAP -> AdventureMapScreen(viewModel = viewModel)
        GameScreen.DAILY_CHALLENGE -> DailyChallengeScreen(viewModel = viewModel)
        GameScreen.THEMES -> ThemesScreen(viewModel = viewModel)
        GameScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
        GameScreen.STATISTICS -> StatisticsScreen(viewModel = viewModel)
        GameScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        GameScreen.ABOUT -> AboutScreen(viewModel = viewModel)
    }
}
