package com.example.model

enum class GameModeType {
    CLASSIC,
    TIME_ATTACK,
    ZEN,
    BOMB_CHALLENGE,
    ADVENTURE,
    DAILY_CHALLENGE,
    HEXA,
    PUZZLE_CHALLENGE,
    COMBO_RUSH,
    ENDLESS
}

data class GameModeInfo(
    val type: GameModeType,
    val titleKey: (LocalizedStrings) -> String,
    val descKey: (LocalizedStrings) -> String,
    val iconName: String,
    val initialTimeSeconds: Int = 0,
    val maxMoves: Int = 0,
    val hasBombs: Boolean = false,
    val isHexa: Boolean = false,
    val allowUndo: Boolean = false,
    val hasDecayingCombo: Boolean = false
)

object GameModes {
    val allModes = listOf(
        GameModeInfo(
            type = GameModeType.CLASSIC,
            titleKey = { it.modeClassic },
            descKey = { it.modeClassicDesc },
            iconName = "grid_view"
        ),
        GameModeInfo(
            type = GameModeType.TIME_ATTACK,
            titleKey = { it.modeTimeAttack },
            descKey = { it.modeTimeAttackDesc },
            iconName = "timer",
            initialTimeSeconds = 90
        ),
        GameModeInfo(
            type = GameModeType.ZEN,
            titleKey = { it.modeZen },
            descKey = { it.modeZenDesc },
            iconName = "spa",
            allowUndo = true
        ),
        GameModeInfo(
            type = GameModeType.BOMB_CHALLENGE,
            titleKey = { it.modeBomb },
            descKey = { it.modeBombDesc },
            iconName = "emergency",
            hasBombs = true
        ),
        GameModeInfo(
            type = GameModeType.ADVENTURE,
            titleKey = { it.modeAdventure },
            descKey = { it.modeAdventureDesc },
            iconName = "map"
        ),
        GameModeInfo(
            type = GameModeType.DAILY_CHALLENGE,
            titleKey = { it.modeDaily },
            descKey = { it.modeDailyDesc },
            iconName = "event"
        ),
        GameModeInfo(
            type = GameModeType.HEXA,
            titleKey = { it.modeHexa },
            descKey = { it.modeHexaDesc },
            iconName = "hexagon",
            isHexa = true
        ),
        GameModeInfo(
            type = GameModeType.PUZZLE_CHALLENGE,
            titleKey = { it.modePuzzle },
            descKey = { it.modePuzzleDesc },
            iconName = "psychology",
            maxMoves = 18
        ),
        GameModeInfo(
            type = GameModeType.COMBO_RUSH,
            titleKey = { it.modeComboRush },
            descKey = { it.modeComboRushDesc },
            iconName = "bolt",
            hasDecayingCombo = true
        ),
        GameModeInfo(
            type = GameModeType.ENDLESS,
            titleKey = { it.modeEndless },
            descKey = { it.modeEndlessDesc },
            iconName = "all_inclusive"
        )
    )

    fun get(type: GameModeType): GameModeInfo =
        allModes.find { it.type == type } ?: allModes.first()
}
