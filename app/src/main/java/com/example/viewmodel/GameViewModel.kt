package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.model.AchievementId
import com.example.model.AchievementsList
import com.example.model.AdventureStages
import com.example.model.AppLanguage
import com.example.model.BlockShape
import com.example.model.BlockShapeFactory
import com.example.model.GameModeInfo
import com.example.model.GameModeType
import com.example.model.GameModes
import com.example.model.GameThemes
import com.example.model.HexGameBoard
import com.example.model.LocalizationManager
import com.example.model.LocalizedStrings
import com.example.model.ParticleManager
import com.example.model.StandardGameBoard
import com.example.model.ThemeId
import com.example.model.VisualTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GameScreen {
    SPLASH,
    HOME,
    GAME_MODES,
    GAME_PLAY,
    ADVENTURE_MAP,
    DAILY_CHALLENGE,
    THEMES,
    ACHIEVEMENTS,
    STATISTICS,
    SETTINGS,
    ABOUT
}

data class FloatingCombo(
    val id: Long = System.nanoTime(),
    val text: String,
    val position: Offset = Offset(0f, 0f)
)

data class AchievementPopup(
    val title: String,
    val reward: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = GamePreferences(application)
    val soundManager = SoundManager()
    val hapticManager = HapticManager(application)
    val particleManager = ParticleManager()

    // Navigation
    private val _currentScreen = MutableStateFlow(GameScreen.SPLASH)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Selected Language & Strings
    private val _language = MutableStateFlow(prefs.language)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _strings = MutableStateFlow(LocalizationManager.get(prefs.language))
    val strings: StateFlow<LocalizedStrings> = _strings.asStateFlow()

    // Selected Visual Theme
    private val _theme = MutableStateFlow(GameThemes.getTheme(prefs.selectedThemeId))
    val theme: StateFlow<VisualTheme> = _theme.asStateFlow()

    private val _unlockedThemes = MutableStateFlow(prefs.getUnlockedThemes())
    val unlockedThemes: StateFlow<Set<ThemeId>> = _unlockedThemes.asStateFlow()

    private val _coins = MutableStateFlow(prefs.coins)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    // Active Game State
    private val _activeMode = MutableStateFlow(GameModeType.CLASSIC)
    val activeMode: StateFlow<GameModeType> = _activeMode.asStateFlow()

    private val _adventureLevel = MutableStateFlow(1)
    val adventureLevel: StateFlow<Int> = _adventureLevel.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _combo = MutableStateFlow(1)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(90)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _movesLeft = MutableStateFlow(18)
    val movesLeft: StateFlow<Int> = _movesLeft.asStateFlow()

    private val _linesClearedInGame = MutableStateFlow(0)
    val linesClearedInGame: StateFlow<Int> = _linesClearedInGame.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _isVictory = MutableStateFlow(false)
    val isVictory: StateFlow<Boolean> = _isVictory.asStateFlow()

    private val _newHighScoreAchieved = MutableStateFlow(false)
    val newHighScoreAchieved: StateFlow<Boolean> = _newHighScoreAchieved.asStateFlow()

    // Boards
    val standardBoard = StandardGameBoard(10)
    val hexBoard = HexGameBoard(3)

    // Tray Pieces (size 3)
    private val _trayPieces = MutableStateFlow<List<BlockShape?>>(listOf(null, null, null))
    val trayPieces: StateFlow<List<BlockShape?>> = _trayPieces.asStateFlow()

    // Selection / Dragging state
    private val _selectedPieceIndex = MutableStateFlow<Int?>(null)
    val selectedPieceIndex: StateFlow<Int?> = _selectedPieceIndex.asStateFlow()

    // Floating notifications
    private val _floatingCombos = MutableStateFlow<List<FloatingCombo>>(emptyList())
    val floatingCombos: StateFlow<List<FloatingCombo>> = _floatingCombos.asStateFlow()

    private val _achievementPopup = MutableStateFlow<AchievementPopup?>(null)
    val achievementPopup: StateFlow<AchievementPopup?> = _achievementPopup.asStateFlow()

    // Active bomb warning
    private val _bombExploded = MutableStateFlow(false)
    val bombExploded: StateFlow<Boolean> = _bombExploded.asStateFlow()

    // Zen Mode Undo snapshot
    private var zenSnapshot: Array<Array<com.example.model.GridCell>>? = null
    private var zenScoreSnapshot = 0

    // Coroutine Jobs
    private var timerJob: Job? = null
    private var playTimeTrackerJob: Job? = null

    init {
        // Sync Audio Settings
        soundManager.masterVolume = prefs.masterVolume
        soundManager.musicVolume = prefs.musicVolume
        soundManager.sfxVolume = prefs.sfxVolume
        soundManager.isMusicEnabled = prefs.isMusicEnabled
        soundManager.isSfxEnabled = prefs.isSfxEnabled
        hapticManager.isVibrationEnabled = prefs.isVibrationEnabled

        // Start play time tracking
        playTimeTrackerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_currentScreen.value == GameScreen.GAME_PLAY && !_isPaused.value && !_isGameOver.value) {
                    prefs.totalPlayTimeSeconds += 1
                }
            }
        }
    }

    fun navigateTo(screen: GameScreen) {
        soundManager.playButtonClick()
        _currentScreen.value = screen
        if (screen == GameScreen.GAME_PLAY) {
            soundManager.startBackgroundMusic(isZenMode = _activeMode.value == GameModeType.ZEN)
        } else {
            soundManager.stopBackgroundMusic()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.language = lang
        _language.value = lang
        _strings.value = LocalizationManager.get(lang)
    }

    fun selectTheme(themeId: ThemeId) {
        val t = GameThemes.getTheme(themeId)
        prefs.selectedThemeId = themeId
        _theme.value = t
    }

    fun unlockTheme(themeId: ThemeId) {
        val t = GameThemes.getTheme(themeId)
        if (_coins.value >= t.unlockCost) {
            prefs.coins -= t.unlockCost
            _coins.value = prefs.coins
            prefs.unlockTheme(themeId)
            _unlockedThemes.value = prefs.getUnlockedThemes()
            soundManager.playRewardUnlocked()
            selectTheme(themeId)
        }
    }

    fun startMode(mode: GameModeType, level: Int = 1) {
        _activeMode.value = mode
        _adventureLevel.value = level
        val modeInfo = GameModes.get(mode)

        _score.value = 0
        _bestScore.value = prefs.getHighScore(mode)
        _combo.value = 1
        _linesClearedInGame.value = 0
        _isPaused.value = false
        _isGameOver.value = false
        _isVictory.value = false
        _newHighScoreAchieved.value = false
        _bombExploded.value = false
        _selectedPieceIndex.value = null
        _floatingCombos.value = emptyList()

        // Init board
        if (modeInfo.isHexa) {
            hexBoard.reset()
        } else {
            standardBoard.reset()
            if (mode == GameModeType.ADVENTURE) {
                val stage = AdventureStages.getStage(level)
                standardBoard.loadObstacles(stage.preFilledCells)
                _movesLeft.value = if (stage.maxMoves > 0) stage.maxMoves else 999
            } else if (mode == GameModeType.DAILY_CHALLENGE) {
                // Seed daily challenge
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
                val seed = sdf.format(Date()).toLongOrNull() ?: 20261112L
                val random = kotlin.random.Random(seed)
                val dailyObstacles = mutableListOf<Pair<Int, Int>>()
                for (i in 0 until 5) {
                    dailyObstacles.add(Pair(random.nextInt(10), random.nextInt(10)))
                }
                standardBoard.loadObstacles(dailyObstacles)
                _movesLeft.value = 25
            } else if (mode == GameModeType.PUZZLE_CHALLENGE) {
                _movesLeft.value = modeInfo.maxMoves
            } else if (mode == GameModeType.BOMB_CHALLENGE) {
                // Pre-spawn an initial bomb
                standardBoard.addBomb(count = 9)
            }
        }

        // Replenish pieces
        refillTray(modeInfo.isHexa)

        // Init Timer if Time Attack
        timerJob?.cancel()
        if (mode == GameModeType.TIME_ATTACK) {
            _timeLeftSeconds.value = modeInfo.initialTimeSeconds
            timerJob = viewModelScope.launch {
                while (isActive && _timeLeftSeconds.value > 0) {
                    delay(1000)
                    if (!_isPaused.value && !_isGameOver.value) {
                        _timeLeftSeconds.value -= 1
                        if (_timeLeftSeconds.value <= 0) {
                            handleGameOver()
                        }
                    }
                }
            }
        }

        prefs.totalGamesPlayed += 1
        checkAchievement(AchievementId.FIRST_STEPS, 1)

        navigateTo(GameScreen.GAME_PLAY)
    }

    private fun refillTray(isHex: Boolean) {
        val newPieces = BlockShapeFactory.generatePieces(isHex, 3, _theme.value.blockColors.size)
        _trayPieces.value = newPieces
    }

    fun selectPieceForPlacement(index: Int) {
        if (_trayPieces.value.getOrNull(index) != null) {
            soundManager.playBlockPickup()
            hapticManager.vibrateClick()
            _selectedPieceIndex.value = if (_selectedPieceIndex.value == index) null else index
        }
    }

    fun tryPlaceSelectedPiece(row: Int, col: Int) {
        val idx = _selectedPieceIndex.value ?: return
        val piece = _trayPieces.value.getOrNull(idx) ?: return

        if (_activeMode.value == GameModeType.HEXA) {
            tryPlaceHexPiece(idx, piece, row, col)
        } else {
            tryPlaceStandardPiece(idx, piece, row, col)
        }
    }

    fun tryPlaceStandardPiece(pieceIndex: Int, piece: BlockShape, row: Int, col: Int): Boolean {
        if (!standardBoard.canPlacePiece(piece, row, col)) {
            return false
        }

        // Zen Mode snapshot before move
        if (_activeMode.value == GameModeType.ZEN) {
            zenSnapshot = standardBoard.createSnapshot()
            zenScoreSnapshot = _score.value
        }

        // Place on board
        standardBoard.placePiece(piece, row, col)
        soundManager.playBlockPlace()
        hapticManager.vibratePlace()

        // Deduct move if puzzle or daily
        if (_activeMode.value == GameModeType.PUZZLE_CHALLENGE || _activeMode.value == GameModeType.DAILY_CHALLENGE) {
            _movesLeft.value = (_movesLeft.value - 1).coerceAtLeast(0)
        }

        // Base points
        val basePoints = piece.coordinates.size * 10
        var addedScore = basePoints

        // Check line clears
        val clearResult = standardBoard.checkAndClearLines()
        if (clearResult.clearedCount > 0) {
            val comboBonus = _combo.value
            val linePoints = when (clearResult.clearedCount) {
                1 -> 100
                2 -> 300
                3 -> 650
                4 -> 1100
                else -> 1600 + (clearResult.clearedCount - 5) * 500
            } * comboBonus

            addedScore += linePoints
            _linesClearedInGame.value += clearResult.clearedCount
            prefs.totalLinesCleared += clearResult.clearedCount

            // Particle burst at center of cleared cells
            clearResult.clearedCells.forEach { (cr, cc) ->
                // Approximate coordinate offset for particle spawn
                particleManager.spawnLineClearBurst(
                    center = Offset(cc * 70f + 100f, cr * 70f + 250f),
                    colors = _theme.value.blockColors,
                    count = 8
                )
            }

            // Sound and haptics
            soundManager.playLineClear(clearResult.clearedCount)
            hapticManager.vibrateClear(clearResult.clearedCount)

            // Multiplier surge
            _combo.value = (_combo.value + 1).coerceAtMost(8)
            if (_combo.value > prefs.highestCombo) {
                prefs.highestCombo = _combo.value
            }

            // Show floating combo text
            val comboMsg = if (clearResult.clearedCount >= 3) {
                "PERFECT! x${_combo.value}"
            } else if (_combo.value > 1) {
                "COMBO x${_combo.value}!"
            } else {
                "+$addedScore"
            }
            triggerFloatingCombo(comboMsg)

            // Bonus time for Time Attack
            if (_activeMode.value == GameModeType.TIME_ATTACK) {
                _timeLeftSeconds.value = (_timeLeftSeconds.value + (clearResult.clearedCount * 5)).coerceAtMost(180)
            }

            // Check achievements
            if (clearResult.clearedCount >= 3) {
                checkAchievement(AchievementId.PERFECT_PLANNING, 3)
            }
            if (_combo.value >= 3) {
                checkAchievement(AchievementId.COMBO_MASTER, _combo.value)
            }
            checkAchievement(AchievementId.DEDICATED_PLAYER, prefs.totalLinesCleared)
            if (_activeMode.value == GameModeType.PUZZLE_CHALLENGE) {
                checkAchievement(AchievementId.PUZZLE_EXPERT, prefs.totalLinesCleared)
            }
        } else {
            // No lines cleared this turn
            if (_activeMode.value == GameModeType.COMBO_RUSH || _activeMode.value == GameModeType.TIME_ATTACK) {
                _combo.value = 1
            }
        }

        // Bomb Challenge mechanics
        if (_activeMode.value == GameModeType.BOMB_CHALLENGE) {
            val exploded = standardBoard.tickBombs()
            if (exploded) {
                _bombExploded.value = true
                soundManager.playBombTick()
                handleGameOver()
                return true
            }
            // Spawn a new bomb every 4 placements if none exist
            if (kotlin.random.Random.nextInt(4) == 0) {
                standardBoard.addBomb(count = 9)
                soundManager.playBombTick()
                hapticManager.vibrateBombTick()
            }
            if (clearResult.defusedBombs > 0) {
                soundManager.playBombDefused()
                checkAchievement(AchievementId.BOMB_SQUAD, clearResult.defusedBombs)
            }
        }

        // Update score
        val newScore = _score.value + addedScore
        _score.value = newScore

        if (newScore > _bestScore.value) {
            if (!_newHighScoreAchieved.value && _bestScore.value > 0) {
                _newHighScoreAchieved.value = true
                soundManager.playVictory()
            }
            _bestScore.value = newScore
            prefs.setHighScore(_activeMode.value, newScore)
        }

        // Check score milestones for achievements
        if (newScore >= 1000) checkAchievement(AchievementId.GETTING_BETTER, newScore)
        if (newScore >= 5000) checkAchievement(AchievementId.SS_CHAMPION, newScore)
        if (_activeMode.value == GameModeType.TIME_ATTACK && newScore >= 1500) {
            checkAchievement(AchievementId.TIME_FIGHTER, newScore)
        }

        // Consume placed piece
        val currentTray = _trayPieces.value.toMutableList()
        currentTray[pieceIndex] = null
        _trayPieces.value = currentTray
        _selectedPieceIndex.value = null

        // Check victory conditions (Adventure / Daily)
        if (_activeMode.value == GameModeType.ADVENTURE) {
            val stage = AdventureStages.getStage(_adventureLevel.value)
            if (newScore >= stage.targetScore && _linesClearedInGame.value >= stage.targetLines) {
                handleAdventureVictory(stage, newScore)
                return true
            }
            if (stage.maxMoves > 0 && _movesLeft.value <= 0) {
                handleGameOver()
                return true
            }
        } else if (_activeMode.value == GameModeType.DAILY_CHALLENGE) {
            if (newScore >= 1200) {
                handleDailyVictory()
                return true
            }
            if (_movesLeft.value <= 0) {
                handleGameOver()
                return true
            }
        } else if (_activeMode.value == GameModeType.PUZZLE_CHALLENGE) {
            if (_movesLeft.value <= 0) {
                handleGameOver()
                return true
            }
        }

        // Refill tray if all 3 used
        if (currentTray.all { it == null }) {
            refillTray(false)
        }

        // Check if game over (no valid moves remain)
        checkMovesRemaining(false)

        return true
    }

    fun tryPlaceHexPiece(pieceIndex: Int, piece: BlockShape, q: Int, r: Int): Boolean {
        if (!hexBoard.canPlacePiece(piece, q, r)) return false

        hexBoard.placePiece(piece, q, r)
        soundManager.playBlockPlace()
        hapticManager.vibratePlace()

        var addedScore = piece.coordinates.size * 12
        val clearResult = hexBoard.checkAndClearLines()

        if (clearResult.clearedCount > 0) {
            val linePoints = clearResult.clearedCount * 250 * _combo.value
            addedScore += linePoints
            soundManager.playLineClear(clearResult.clearedCount)
            hapticManager.vibrateClear(clearResult.clearedCount)
            _combo.value = (_combo.value + 1).coerceAtMost(6)
            triggerFloatingCombo("HEXA CLEAR! x${_combo.value}")
        } else {
            _combo.value = 1
        }

        val newScore = _score.value + addedScore
        _score.value = newScore
        if (newScore > _bestScore.value) {
            _bestScore.value = newScore
            prefs.setHighScore(GameModeType.HEXA, newScore)
        }
        if (newScore >= 800) {
            checkAchievement(AchievementId.HEXA_NOVICE, newScore)
        }

        val currentTray = _trayPieces.value.toMutableList()
        currentTray[pieceIndex] = null
        _trayPieces.value = currentTray
        _selectedPieceIndex.value = null

        if (currentTray.all { it == null }) {
            refillTray(true)
        }

        checkMovesRemaining(true)
        return true
    }

    private fun checkMovesRemaining(isHex: Boolean) {
        val remainingPieces = _trayPieces.value.filterNotNull()
        if (remainingPieces.isEmpty()) return

        val hasAnyValidMove = remainingPieces.any { piece ->
            if (isHex) hexBoard.canPlacePieceAnywhere(piece)
            else standardBoard.canPlacePieceAnywhere(piece)
        }

        if (!hasAnyValidMove) {
            handleGameOver()
        }
    }

    private fun handleGameOver() {
        if (_isGameOver.value) return
        _isGameOver.value = true
        soundManager.playGameOver()
        // Award participation coins
        val earnedCoins = (_score.value / 60).coerceAtLeast(5)
        prefs.addCoins(earnedCoins)
        _coins.value = prefs.coins
    }

    private fun handleAdventureVictory(stage: com.example.model.AdventureStage, finalScore: Int) {
        _isVictory.value = true
        soundManager.playVictory()
        val stars = when {
            finalScore >= stage.star3Score -> 3
            finalScore >= stage.star2Score -> 2
            else -> 1
        }
        prefs.setAdventureStars(stage.levelNumber, stars)
        prefs.addCoins(stage.rewardCoins)
        _coins.value = prefs.coins

        checkAchievement(AchievementId.STAR_COLLECTOR, prefs.getTotalAdventureStars())
    }

    private fun handleDailyVictory() {
        _isVictory.value = true
        soundManager.playVictory()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        if (prefs.lastDailyCompletedDate != todayStr) {
            prefs.lastDailyCompletedDate = todayStr
            prefs.dailyStreak += 1
            prefs.completedDailyCount += 1
            prefs.addCoins(200)
            _coins.value = prefs.coins
            checkAchievement(AchievementId.DAILY_CHAMPION, prefs.completedDailyCount)
        }
    }

    fun undoZenMove() {
        val snap = zenSnapshot ?: return
        standardBoard.restoreSnapshot(snap)
        _score.value = zenScoreSnapshot
        zenSnapshot = null
        soundManager.playButtonClick()
    }

    fun togglePause() {
        soundManager.playButtonClick()
        _isPaused.value = !_isPaused.value
    }

    private fun triggerFloatingCombo(text: String) {
        val item = FloatingCombo(text = text)
        _floatingCombos.value = _floatingCombos.value + item
        viewModelScope.launch {
            delay(1200)
            _floatingCombos.value = _floatingCombos.value.filterNot { it.id == item.id }
        }
    }

    fun checkAchievement(id: AchievementId, currentProgress: Int) {
        val ach = AchievementsList.all.find { it.id == id } ?: return
        val unlocked = prefs.getUnlockedAchievements()
        if (!unlocked.contains(id) && currentProgress >= ach.target) {
            prefs.unlockAchievement(id)
            prefs.addCoins(ach.rewardCoins)
            _coins.value = prefs.coins
            soundManager.playRewardUnlocked()

            _achievementPopup.value = AchievementPopup(
                title = ach.title(_strings.value),
                reward = ach.rewardCoins
            )
            viewModelScope.launch {
                delay(3000)
                _achievementPopup.value = null
            }
        }
    }

    fun updateVolumes(master: Float, music: Float, sfx: Float) {
        prefs.masterVolume = master
        prefs.musicVolume = music
        prefs.sfxVolume = sfx
        soundManager.masterVolume = master
        soundManager.musicVolume = music
        soundManager.sfxVolume = sfx
    }

    fun toggleMusic(enabled: Boolean) {
        prefs.isMusicEnabled = enabled
        soundManager.isMusicEnabled = enabled
        if (enabled && _currentScreen.value == GameScreen.GAME_PLAY) {
            soundManager.startBackgroundMusic(isZenMode = _activeMode.value == GameModeType.ZEN)
        } else if (!enabled) {
            soundManager.stopBackgroundMusic()
        }
    }

    fun toggleSfx(enabled: Boolean) {
        prefs.isSfxEnabled = enabled
        soundManager.isSfxEnabled = enabled
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.isVibrationEnabled = enabled
        hapticManager.isVibrationEnabled = enabled
    }

    fun togglePreview(enabled: Boolean) {
        prefs.isPlacementPreviewEnabled = enabled
    }

    fun toggleReducedMotion(enabled: Boolean) {
        prefs.isReducedMotionEnabled = enabled
    }

    fun resetAllProgress() {
        prefs.resetAllProgress()
        _coins.value = prefs.coins
        _unlockedThemes.value = prefs.getUnlockedThemes()
        _theme.value = GameThemes.getTheme(prefs.selectedThemeId)
        _language.value = prefs.language
        _strings.value = LocalizationManager.get(prefs.language)
        _score.value = 0
        _bestScore.value = 0
    }
}
