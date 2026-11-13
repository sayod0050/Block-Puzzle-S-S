package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AchievementId
import com.example.model.AppLanguage
import com.example.model.GameModeType
import com.example.model.ThemeId

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ss_block_puzzle_prefs", Context.MODE_PRIVATE)

    var language: AppLanguage
        get() {
            val code = prefs.getString("key_language", AppLanguage.ENGLISH.code) ?: "en"
            return AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
        }
        set(value) {
            prefs.edit().putString("key_language", value.code).apply()
        }

    var selectedThemeId: ThemeId
        get() {
            val name = prefs.getString("key_theme", ThemeId.MODERN_DARK.name) ?: ThemeId.MODERN_DARK.name
            return try {
                ThemeId.valueOf(name)
            } catch (_: Exception) {
                ThemeId.MODERN_DARK
            }
        }
        set(value) {
            prefs.edit().putString("key_theme", value.name).apply()
        }

    fun getUnlockedThemes(): Set<ThemeId> {
        val defaultSet = setOf(
            ThemeId.MODERN_DARK.name,
            ThemeId.PURE_LIGHT.name,
            ThemeId.OCEAN_BLUE.name
        )
        val saved = prefs.getStringSet("key_unlocked_themes", defaultSet) ?: defaultSet
        return saved.mapNotNull {
            try {
                ThemeId.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }.toSet()
    }

    fun unlockTheme(themeId: ThemeId) {
        val current = getUnlockedThemes().map { it.name }.toMutableSet()
        current.add(themeId.name)
        prefs.edit().putStringSet("key_unlocked_themes", current).apply()
    }

    var coins: Int
        get() = prefs.getInt("key_coins", 150)
        set(value) = prefs.edit().putInt("key_coins", value.coerceAtLeast(0)).apply()

    fun addCoins(amount: Int) {
        coins += amount
    }

    fun getHighScore(mode: GameModeType): Int {
        return prefs.getInt("key_highscore_${mode.name}", 0)
    }

    fun setHighScore(mode: GameModeType, score: Int) {
        val current = getHighScore(mode)
        if (score > current) {
            prefs.edit().putInt("key_highscore_${mode.name}", score).apply()
        }
    }

    // Audio & UX settings
    var masterVolume: Float
        get() = prefs.getFloat("key_vol_master", 0.85f)
        set(value) = prefs.edit().putFloat("key_vol_master", value).apply()

    var musicVolume: Float
        get() = prefs.getFloat("key_vol_music", 0.65f)
        set(value) = prefs.edit().putFloat("key_vol_music", value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat("key_vol_sfx", 0.85f)
        set(value) = prefs.edit().putFloat("key_vol_sfx", value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean("key_music_enabled", true)
        set(value) = prefs.edit().putBoolean("key_music_enabled", value).apply()

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean("key_sfx_enabled", true)
        set(value) = prefs.edit().putBoolean("key_sfx_enabled", value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("key_vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("key_vibration_enabled", value).apply()

    var isPlacementPreviewEnabled: Boolean
        get() = prefs.getBoolean("key_preview_enabled", true)
        set(value) = prefs.edit().putBoolean("key_preview_enabled", value).apply()

    var isReducedMotionEnabled: Boolean
        get() = prefs.getBoolean("key_reduced_motion", false)
        set(value) = prefs.edit().putBoolean("key_reduced_motion", value).apply()

    var isHintsEnabled: Boolean
        get() = prefs.getBoolean("key_hints_enabled", true)
        set(value) = prefs.edit().putBoolean("key_hints_enabled", value).apply()

    var playerName: String
        get() = prefs.getString("key_player_name", "Player") ?: "Player"
        set(value) = prefs.edit().putString("key_player_name", value).apply()

    // Statistics
    var totalGamesPlayed: Int
        get() = prefs.getInt("key_stat_games", 0)
        set(value) = prefs.edit().putInt("key_stat_games", value).apply()

    var totalLinesCleared: Int
        get() = prefs.getInt("key_stat_lines", 0)
        set(value) = prefs.edit().putInt("key_stat_lines", value).apply()

    var highestCombo: Int
        get() = prefs.getInt("key_stat_combo", 0)
        set(value) = prefs.edit().putInt("key_stat_combo", value).apply()

    var totalPlayTimeSeconds: Long
        get() = prefs.getLong("key_stat_playtime", 0L)
        set(value) = prefs.edit().putLong("key_stat_playtime", value).apply()

    var totalBombsDefused: Int
        get() = prefs.getInt("key_stat_bombs_defused", 0)
        set(value) = prefs.edit().putInt("key_stat_bombs_defused", value).apply()

    // Adventure stars: map level -> stars
    fun getAdventureStars(level: Int): Int {
        return prefs.getInt("key_adv_level_$level", 0)
    }

    fun setAdventureStars(level: Int, stars: Int) {
        val curr = getAdventureStars(level)
        if (stars > curr) {
            prefs.edit().putInt("key_adv_level_$level", stars).apply()
        }
    }

    fun getTotalAdventureStars(): Int {
        return (1..20).sumOf { getAdventureStars(it) }
    }

    // Daily Challenge
    var dailyStreak: Int
        get() = prefs.getInt("key_daily_streak", 0)
        set(value) = prefs.edit().putInt("key_daily_streak", value).apply()

    var lastDailyCompletedDate: String
        get() = prefs.getString("key_last_daily_date", "") ?: ""
        set(value) = prefs.edit().putString("key_last_daily_date", value).apply()

    var completedDailyCount: Int
        get() = prefs.getInt("key_daily_completed_count", 0)
        set(value) = prefs.edit().putInt("key_daily_completed_count", value).apply()

    // Achievements
    fun getUnlockedAchievements(): Set<AchievementId> {
        val saved = prefs.getStringSet("key_achievements", emptySet()) ?: emptySet()
        return saved.mapNotNull {
            try {
                AchievementId.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }.toSet()
    }

    fun unlockAchievement(id: AchievementId) {
        val current = getUnlockedAchievements().map { it.name }.toMutableSet()
        current.add(id.name)
        prefs.edit().putStringSet("key_achievements", current).apply()
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}
