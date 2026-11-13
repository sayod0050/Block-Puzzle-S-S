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
import com.example.model.GameModeType
import com.example.model.GameModes
import com.example.model.GameThemes
import com.example.model.GridCell
import com.example.model.HexCell
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

    private val _playerName = MutableStateFlow(prefs.playerName)
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    // Reactive settings StateFlows
    private val _masterVolume = MutableStateFlow(prefs.masterVolume)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _musicVolume = MutableStateFlow(prefs.musicVolume)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _sfxVolume = MutableStateFlow(prefs.sfxVolume)
    val sfxVolume: StateFlow<Float> = _sfxVolume.asStateFlow()

    private val _isMusicEnabled = MutableStateFlow(prefs.isMusicEnabled)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isSfxEnabled = MutableStateFlow(prefs.isSfxEnabled)
    val isSfxEnabled: StateFlow<Boolean> = _isSfxEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.isVibrationEnabled)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isPlacementPreviewEnabled = MutableStateFlow(prefs.isPlacementPreviewEnabled)
    val isPlacementPreviewEnabled: StateFlow<Boolean> = _isPlacementPreviewEnabled.asStateFlow()

    private val _isReducedMotionEnabled = MutableStateFlow(prefs.isReducedMotionEnabled)
    val isReducedMotionEnabled: StateFlow<Boolean> = _isReducedMotionEnabled.asStateFlow()

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

    // Boards & Reactive Snapshots
    val standardBoard = StandardGameBoard(10)
    val hexBoard = HexGameBoard(3)

    private val _boardGrid = MutableStateFlow(standardBoard.snapshotGrid())
    val boardGrid: StateFlow<List<List<GridCell>>> = _boardGrid.asStateFlow()

    private val _hexGrid = MutableStateFlow(hexBoard.snapshotCells())
    val hexGrid: StateFlow<Map<Pair<Int, Int>, HexCell>> = _hexGrid.asStateFlow()

    private val _lastPlacedCells = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    val lastPlacedCells: StateFlow<Set<Pair<Int, Int>>> = _lastPlacedCells.asStateFlow()

    private val _lastClearedCells = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val lastClearedCells: StateFlow<List<Pair<Int, Int>>> = _lastClearedCells.asStateFlow()

    private val _clearBurstId = MutableStateFlow(0L)
    val clearBurstId: StateFlow<Long> = _clearBurstId.asStateFlow()

    private val _placeAnimId = MutableStateFlow(0L)
    val placeAnimId: StateFlow<Long> = _placeAnimId.asStateFlow()

    // Tray Pieces (size 3)
    private val _trayPieces = MutableStateFlow<List<BlockShape?>>(listOf(null, null, null))
    val trayPieces: StateFlow<List<BlockShape?>> = _trayPieces.asStateFlow()

    // Selection state for tap-to-place
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
    private var zenSnapshot: Array<Array<GridCell>>? = null
    private var zenScoreSnapshot = 0

    // Coroutine Jobs
    private var timerJob: Job? = null
    private var playTimeTrackerJob: Job? = null

    companion object {
        const val PUZZLE_TARGET_SCORE = 800
        const val DAILY_TARGET_SCORE = 1200
    }

    init {
        soundManager.masterVolume = prefs.masterVolume
        soundManager.musicVolume = prefs.musicVolume
        soundManager.sfxVolume = prefs.sfxVolume
        soundManager.isMusicEnabled = prefs.isMusicEnabled
        soundManager.isSfxEnabled = prefs.isSfxEnabled
        hapticManager.isVibrationEnabled = prefs.isVibrationEnabled

        playTimeTrackerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_currentScreen.value == GameScreen.GAME_PLAY && !_isPaused.value && !_isGameOver.value && !_isVictory.value) {
                    prefs.totalPlayTimeSeconds += 1
                }
            }
        }
    }

    private fun syncBoardSnapshots() {
        _boardGrid.value = standardBoard.snapshotGrid()
        _hexGrid.value = hexBoard.snapshotCells()
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

    fun setPlayerName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            prefs.playerName = trimmed
            _playerName.value = trimmed
        }
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
        _lastPlacedCells.value = emptySet()
        _lastClearedCells.value = emptyList()
        zenSnapshot = null
        particleManager.clear()

        if (modeInfo.isHexa) {
            hexBoard.reset()
        } else {
            standardBoard.reset()
            if (mode == GameModeType.ADVENTURE) {
                val stage = AdventureStages.getStage(level)
                standardBoard.loadObstacles(stage.preFilledCells)
                _movesLeft.value = if (stage.maxMoves > 0) stage.maxMoves else 999
            } else if (mode == GameModeType.DAILY_CHALLENGE) {
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
                standardBoard.addBomb(count = 9)
            }
        }

        syncBoardSnapshots()
        refillTray(modeInfo.isHexa)

        timerJob?.cancel()
        if (mode == GameModeType.TIME_ATTACK) {
            _timeLeftSeconds.value = modeInfo.initialTimeSeconds
            timerJob = viewModelScope.launch {
                while (isActive && _timeLeftSeconds.value > 0) {
                    delay(1000)
                    if (!_isPaused.value && !_isGameOver.value && !_isVictory.value) {
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
        if (_isPaused.value || _isGameOver.value || _isVictory.value) return
        if (_trayPieces.value.getOrNull(index) != null) {
            soundManager.playBlockPickup()
            hapticManager.vibrateClick()
            _selectedPieceIndex.value = if (_selectedPieceIndex.value == index) null else index
        }
    }

    fun clearSelection() {
        _selectedPieceIndex.value = null
    }

    fun tryPlaceSelectedPiece(row: Int, col: Int): Boolean {
        if (_isPaused.value || _isGameOver.value || _isVictory.value) return false
        val idx = _selectedPieceIndex.value ?: return false
        val piece = _trayPieces.value.getOrNull(idx) ?: return false

        return if (_activeMode.value == GameModeType.HEXA) {
            tryPlaceHexPiece(idx, piece, row, col)
        } else {
            val snapped = standardBoard.findTapPlacement(piece, row, col) ?: return false
            tryPlaceStandardPiece(idx, piece, snapped.first, snapped.second)
        }
    }

    fun tryPlaceStandardPiece(pieceIndex: Int, piece: BlockShape, row: Int, col: Int): Boolean {
        if (_isPaused.value || _isGameOver.value || _isVictory.value) return false
        if (_trayPieces.value.getOrNull(pieceIndex)?.id != piece.id) return false
        if (!standardBoard.canPlacePiece(piece, row, col)) {
            return false
        }

        if (_activeMode.value == GameModeType.ZEN) {
            zenSnapshot = standardBoard.createSnapshot()
            zenScoreSnapshot = _score.value
        }

        val placedCoords = standardBoard.placePiece(piece, row, col)
        _lastPlacedCells.value = placedCoords.toSet()
        _placeAnimId.value = System.nanoTime()

        soundManager.playBlockPlace()
        hapticManager.vibratePlace()

        if (_activeMode.value == GameModeType.PUZZLE_CHALLENGE ||
            _activeMode.value == GameModeType.DAILY_CHALLENGE ||
            (_activeMode.value == GameModeType.ADVENTURE && AdventureStages.getStage(_adventureLevel.value).maxMoves > 0)
        ) {
            _movesLeft.value = (_movesLeft.value - 1).coerceAtLeast(0)
        }

        val basePoints = piece.coordinates.size * 10
        var addedScore = basePoints

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

            _lastClearedCells.value = clearResult.clearedCells
            _clearBurstId.value = System.nanoTime()

            soundManager.playLineClear(clearResult.clearedCount)
            hapticManager.vibrateClear(clearResult.clearedCount)

            _combo.value = (_combo.value + 1).coerceAtMost(8)
            if (_combo.value > prefs.highestCombo) {
                prefs.highestCombo = _combo.value
            }

            val comboMsg = if (clearResult.clearedCount >= 3) {
                "PERFECT! x${_combo.value}"
            } else if (_combo.value > 1) {
                "COMBO x${_combo.value}!"
            } else {
                "+$addedScore"
            }
            triggerFloatingCombo(comboMsg)

            if (_activeMode.value == GameModeType.TIME_ATTACK) {
                _timeLeftSeconds.value = (_timeLeftSeconds.value + (clearResult.clearedCount * 5)).coerceAtMost(180)
            }

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
            _lastClearedCells.value = emptyList()
            if (_activeMode.value == GameModeType.COMBO_RUSH || _activeMode.value == GameModeType.TIME_ATTACK) {
                _combo.value = 1
            }
        }

        if (_activeMode.value == GameModeType.BOMB_CHALLENGE) {
            if (clearResult.defusedBombs > 0) {
                prefs.totalBombsDefused += clearResult.defusedBombs
                soundManager.playBombDefused()
                checkAchievement(AchievementId.BOMB_SQUAD, prefs.totalBombsDefused)
            }
            val exploded = standardBoard.tickBombs()
            if (exploded) {
                _bombExploded.value = true
                soundManager.playBombTick()
                syncBoardSnapshots()
                handleGameOver()
                return true
            }
            if (kotlin.random.Random.nextInt(4) == 0) {
                standardBoard.addBomb(count = 9)
                soundManager.playBombTick()
                hapticManager.vibrateBombTick()
            }
        }

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

        if (newScore >= 1000) checkAchievement(AchievementId.GETTING_BETTER, newScore)
        if (newScore >= 5000) checkAchievement(AchievementId.SS_CHAMPION, newScore)
        if (_activeMode.value == GameModeType.TIME_ATTACK && newScore >= 1500) {
            checkAchievement(AchievementId.TIME_FIGHTER, newScore)
        }

        val currentTray = _trayPieces.value.toMutableList()
        currentTray[pieceIndex] = null
        _trayPieces.value = currentTray
        _selectedPieceIndex.value = null

        syncBoardSnapshots()

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
            if (newScore >= DAILY_TARGET_SCORE) {
                handleDailyVictory()
                return true
            }
            if (_movesLeft.value <= 0) {
                handleGameOver()
                return true
            }
        } else if (_activeMode.value == GameModeType.PUZZLE_CHALLENGE) {
            if (newScore >= PUZZLE_TARGET_SCORE) {
                _isVictory.value = true
                soundManager.playVictory()
                prefs.addCoins(120)
                _coins.value = prefs.coins
                return true
            }
            if (_movesLeft.value <= 0) {
                handleGameOver()
                return true
            }
        }

        if (currentTray.all { it == null }) {
            refillTray(false)
        }

        checkMovesRemaining(false)
        return true
    }

    fun tryPlaceHexPiece(pieceIndex: Int, piece: BlockShape, q: Int, r: Int): Boolean {
        if (_isPaused.value || _isGameOver.value || _isVictory.value) return false
        if (_trayPieces.value.getOrNull(pieceIndex)?.id != piece.id) return false
        if (!hexBoard.canPlacePiece(piece, q, r)) return false

        val placedCoords = hexBoard.placePiece(piece, q, r)
        _lastPlacedCells.value = placedCoords.toSet()
        _placeAnimId.value = System.nanoTime()

        soundManager.playBlockPlace()
        hapticManager.vibratePlace()

        var addedScore = piece.coordinates.size * 12
        val clearResult = hexBoard.checkAndClearLines()

        if (clearResult.clearedCount > 0) {
            val linePoints = clearResult.clearedCount * 250 * _combo.value
            addedScore += linePoints
            _linesClearedInGame.value += clearResult.clearedCount
            prefs.totalLinesCleared += clearResult.clearedCount
            _lastClearedCells.value = clearResult.clearedCells
            _clearBurstId.value = System.nanoTime()

            soundManager.playLineClear(clearResult.clearedCount)
            hapticManager.vibrateClear(clearResult.clearedCount)
            _combo.value = (_combo.value + 1).coerceAtMost(6)
            triggerFloatingCombo("HEXA CLEAR! x${_combo.value}")
        } else {
            _lastClearedCells.value = emptyList()
            _combo.value = 1
        }

        val newScore = _score.value + addedScore
        _score.value = newScore
        if (newScore > _bestScore.value) {
            if (!_newHighScoreAchieved.value && _bestScore.value > 0) {
                _newHighScoreAchieved.value = true
                soundManager.playVictory()
            }
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

        syncBoardSnapshots()

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
        syncBoardSnapshots()
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
        _masterVolume.value = master
        _musicVolume.value = music
        _sfxVolume.value = sfx
        soundManager.masterVolume = master
        soundManager.musicVolume = music
        soundManager.sfxVolume = sfx
    }

    fun toggleMusic(enabled: Boolean) {
        prefs.isMusicEnabled = enabled
        _isMusicEnabled.value = enabled
        soundManager.isMusicEnabled = enabled
        if (enabled && _currentScreen.value == GameScreen.GAME_PLAY) {
            soundManager.startBackgroundMusic(isZenMode = _activeMode.value == GameModeType.ZEN)
        } else if (!enabled) {
            soundManager.stopBackgroundMusic()
        }
    }

    fun toggleSfx(enabled: Boolean) {
        prefs.isSfxEnabled = enabled
        _isSfxEnabled.value = enabled
        soundManager.isSfxEnabled = enabled
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.isVibrationEnabled = enabled
        _isVibrationEnabled.value = enabled
        hapticManager.isVibrationEnabled = enabled
    }

    fun togglePreview(enabled: Boolean) {
        prefs.isPlacementPreviewEnabled = enabled
        _isPlacementPreviewEnabled.value = enabled
    }

    fun toggleReducedMotion(enabled: Boolean) {
        prefs.isReducedMotionEnabled = enabled
        _isReducedMotionEnabled.value = enabled
    }

    fun resetAllProgress() {
        prefs.resetAllProgress()
        _coins.value = prefs.coins
        _playerName.value = prefs.playerName
        _unlockedThemes.value = prefs.getUnlockedThemes()
        _theme.value = GameThemes.getTheme(prefs.selectedThemeId)
        _language.value = prefs.language
        _strings.value = LocalizationManager.get(prefs.language)
        _masterVolume.value = prefs.masterVolume
        _musicVolume.value = prefs.musicVolume
        _sfxVolume.value = prefs.sfxVolume
        _isMusicEnabled.value = prefs.isMusicEnabled
        _isSfxEnabled.value = prefs.isSfxEnabled
        _isVibrationEnabled.value = prefs.isVibrationEnabled
        _isPlacementPreviewEnabled.value = prefs.isPlacementPreviewEnabled
        _isReducedMotionEnabled.value = prefs.isReducedMotionEnabled
        soundManager.masterVolume = prefs.masterVolume
        soundManager.musicVolume = prefs.musicVolume
        soundManager.sfxVolume = prefs.sfxVolume
        soundManager.isMusicEnabled = prefs.isMusicEnabled
        soundManager.isSfxEnabled = prefs.isSfxEnabled
        hapticManager.isVibrationEnabled = prefs.isVibrationEnabled
        _score.value = 0
        _bestScore.value = 0
    }
}
