package com.example.model

import androidx.compose.ui.graphics.Color

enum class ThemeId {
    MODERN_DARK,
    PURE_LIGHT,
    OCEAN_BLUE,
    NEON_GLOW,
    NATURE,
    MIDNIGHT,
    PASTEL,
    PREMIUM_GOLD
}

data class VisualTheme(
    val id: ThemeId,
    val nameKey: (LocalizedStrings) -> String,
    val isDark: Boolean,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val boardBackground: Color,
    val boardBorder: Color,
    val emptyCellColor: Color,
    val emptyCellBorder: Color,
    val previewHighlightColor: Color,
    val cardBackground: Color,
    val textColorPrimary: Color,
    val textColorSecondary: Color,
    val accentColor: Color,
    val blockColors: List<Color>,
    val unlockCost: Int, // 0 for default unlocked
    val isDefaultUnlocked: Boolean = false
)

object GameThemes {
    val ModernDark = VisualTheme(
        id = ThemeId.MODERN_DARK,
        nameKey = { it.themeModernDark },
        isDark = true,
        backgroundStart = Color(0xFF0F101A),
        backgroundEnd = Color(0xFF181A2A),
        boardBackground = Color(0xFF1E2138),
        boardBorder = Color(0xFF33385C),
        emptyCellColor = Color(0xFF272B47),
        emptyCellBorder = Color(0xFF363B60),
        previewHighlightColor = Color(0x6600E5FF),
        cardBackground = Color(0xFF1E2138),
        textColorPrimary = Color(0xFFFFFFFF),
        textColorSecondary = Color(0xFF9EA3C7),
        accentColor = Color(0xFF00E5FF),
        blockColors = listOf(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF3366), // Pink
            Color(0xFFFFB300), // Amber
            Color(0xFF00E676), // Lime Green
            Color(0xFF7C4DFF), // Purple
            Color(0xFFFF6D00), // Orange
            Color(0xFF2979FF)  // Blue
        ),
        unlockCost = 0,
        isDefaultUnlocked = true
    )

    val PureLight = VisualTheme(
        id = ThemeId.PURE_LIGHT,
        nameKey = { it.themePureLight },
        isDark = false,
        backgroundStart = Color(0xFFF0F4F8),
        backgroundEnd = Color(0xFFE2E8F0),
        boardBackground = Color(0xFFCBD5E1),
        boardBorder = Color(0xFF94A3B8),
        emptyCellColor = Color(0xFFE2E8F0),
        emptyCellBorder = Color(0xFFCBD5E1),
        previewHighlightColor = Color(0x660284C7),
        cardBackground = Color(0xFFFFFFFF),
        textColorPrimary = Color(0xFF0F172A),
        textColorSecondary = Color(0xFF64748B),
        accentColor = Color(0xFF0284C7),
        blockColors = listOf(
            Color(0xFF0284C7), // Blue
            Color(0xFFE11D48), // Rose
            Color(0xFFD97706), // Amber
            Color(0xFF059669), // Emerald
            Color(0xFF7C3AED), // Violet
            Color(0xFFEA580C), // Orange
            Color(0xFF0891B2)  // Cyan
        ),
        unlockCost = 0,
        isDefaultUnlocked = true
    )

    val OceanBlue = VisualTheme(
        id = ThemeId.OCEAN_BLUE,
        nameKey = { it.themeOceanBlue },
        isDark = true,
        backgroundStart = Color(0xFF071426),
        backgroundEnd = Color(0xFF0A2540),
        boardBackground = Color(0xFF0E3356),
        boardBorder = Color(0xFF1A5285),
        emptyCellColor = Color(0xFF123E66),
        emptyCellBorder = Color(0xFF1F5F99),
        previewHighlightColor = Color(0x6638BDF8),
        cardBackground = Color(0xFF0F365A),
        textColorPrimary = Color(0xFFF0F9FF),
        textColorSecondary = Color(0xFF7DD3FC),
        accentColor = Color(0xFF38BDF8),
        blockColors = listOf(
            Color(0xFF38BDF8), // Sky Blue
            Color(0xFF2DD4BF), // Teal
            Color(0xFF818CF8), // Indigo
            Color(0xFFF472B6), // Coral Pink
            Color(0xFFFBBF24), // Sand
            Color(0xFF34D399), // Seafoam
            Color(0xFF60A5FA)  // Azure
        ),
        unlockCost = 0,
        isDefaultUnlocked = true
    )

    val NeonGlow = VisualTheme(
        id = ThemeId.NEON_GLOW,
        nameKey = { it.themeNeonGlow },
        isDark = true,
        backgroundStart = Color(0xFF0B021A),
        backgroundEnd = Color(0xFF180533),
        boardBackground = Color(0xFF23084C),
        boardBorder = Color(0xFF5616B2),
        emptyCellColor = Color(0xFF2D0E60),
        emptyCellBorder = Color(0xFF4C1D95),
        previewHighlightColor = Color(0x66FF007F),
        cardBackground = Color(0xFF240A4E),
        textColorPrimary = Color(0xFFFFFFFF),
        textColorSecondary = Color(0xFFE879F9),
        accentColor = Color(0xFFFF007F),
        blockColors = listOf(
            Color(0xFFFF007F), // Neon Pink
            Color(0xFF00F0FF), // Cyber Cyan
            Color(0xFF39FF14), // Neon Green
            Color(0xFFFFE600), // Electric Yellow
            Color(0xFFBD00FF), // Electric Violet
            Color(0xFFFF5400), // Neon Orange
            Color(0xFF00FFC4)  // Neon Mint
        ),
        unlockCost = 300
    )

    val Nature = VisualTheme(
        id = ThemeId.NATURE,
        nameKey = { it.themeNature },
        isDark = true,
        backgroundStart = Color(0xFF0F1E13),
        backgroundEnd = Color(0xFF1A3321),
        boardBackground = Color(0xFF24442D),
        boardBorder = Color(0xFF3B6B48),
        emptyCellColor = Color(0xFF2D5337),
        emptyCellBorder = Color(0xFF3F704D),
        previewHighlightColor = Color(0x664ADE80),
        cardBackground = Color(0xFF23442C),
        textColorPrimary = Color(0xFFF0FDF4),
        textColorSecondary = Color(0xFF86EFAC),
        accentColor = Color(0xFF4ADE80),
        blockColors = listOf(
            Color(0xFF4ADE80), // Forest Green
            Color(0xFFFBBF24), // Sunflower
            Color(0xFFFB7185), // Blossom Rose
            Color(0xFF38BDF8), // Clear Stream
            Color(0xFFA78BFA), // Lavender Wildflower
            Color(0xFFFB923C), // Sunset Amber
            Color(0xFF2DD4BF)  // Pine Teal
        ),
        unlockCost = 450
    )

    val Midnight = VisualTheme(
        id = ThemeId.MIDNIGHT,
        nameKey = { it.themeMidnight },
        isDark = true,
        backgroundStart = Color(0xFF080612),
        backgroundEnd = Color(0xFF130E26),
        boardBackground = Color(0xFF1D163A),
        boardBorder = Color(0xFF392B6E),
        emptyCellColor = Color(0xFF261D4C),
        emptyCellBorder = Color(0xFF3B2E74),
        previewHighlightColor = Color(0x66A855F7),
        cardBackground = Color(0xFF1C1538),
        textColorPrimary = Color(0xFFFAF5FF),
        textColorSecondary = Color(0xFFC084FC),
        accentColor = Color(0xFFA855F7),
        blockColors = listOf(
            Color(0xFFA855F7), // Nebula Violet
            Color(0xFF6366F1), // Deep Star Blue
            Color(0xFFEC4899), // Supernova
            Color(0xFF14B8A6), // Comet Jade
            Color(0xFFF59E0B), // Star Gold
            Color(0xFF06B6D4), // Starlight Teal
            Color(0xFF8B5CF6)  // Cosmic Purple
        ),
        unlockCost = 600
    )

    val Pastel = VisualTheme(
        id = ThemeId.PASTEL,
        nameKey = { it.themePastel },
        isDark = false,
        backgroundStart = Color(0xFFFAF5FF),
        backgroundEnd = Color(0xFFF3E8FF),
        boardBackground = Color(0xFFE9D5FF),
        boardBorder = Color(0xFFC084FC),
        emptyCellColor = Color(0xFFF3E8FF),
        emptyCellBorder = Color(0xFFE9D5FF),
        previewHighlightColor = Color(0x66A855F7),
        cardBackground = Color(0xFFFFFFFF),
        textColorPrimary = Color(0xFF581C87),
        textColorSecondary = Color(0xFF7E22CE),
        accentColor = Color(0xFFA855F7),
        blockColors = listOf(
            Color(0xFFF472B6), // Pastel Pink
            Color(0xFF38BDF8), // Pastel Sky
            Color(0xFF4ADE80), // Pastel Mint
            Color(0xFFFBBF24), // Pastel Peach
            Color(0xFFA78BFA), // Pastel Violet
            Color(0xFFFB923C), // Pastel Apricot
            Color(0xFF2DD4BF)  // Pastel Aqua
        ),
        unlockCost = 750
    )

    val PremiumGold = VisualTheme(
        id = ThemeId.PREMIUM_GOLD,
        nameKey = { it.themePremiumGold },
        isDark = true,
        backgroundStart = Color(0xFF12100E),
        backgroundEnd = Color(0xFF1F1A14),
        boardBackground = Color(0xFF2B231A),
        boardBorder = Color(0xFF5E4D34),
        emptyCellColor = Color(0xFF382E22),
        emptyCellBorder = Color(0xFF4F402D),
        previewHighlightColor = Color(0x66FFD700),
        cardBackground = Color(0xFF261F16),
        textColorPrimary = Color(0xFFFFFBEB),
        textColorSecondary = Color(0xFFFDE68A),
        accentColor = Color(0xFFFFD700),
        blockColors = listOf(
            Color(0xFFFFD700), // Imperial Gold
            Color(0xFFF59E0B), // Warm Amber
            Color(0xFFD97706), // Burnished Bronze
            Color(0xFFE5E7EB), // Polished Platinum
            Color(0xFFFBBF24), // Champagne
            Color(0xFFFB923C), // Rose Gold
            Color(0xFFEAB308)  // Radiant Gold
        ),
        unlockCost = 1000
    )

    val allThemes = listOf(
        ModernDark,
        PureLight,
        OceanBlue,
        NeonGlow,
        Nature,
        Midnight,
        Pastel,
        PremiumGold
    )

    fun getTheme(id: ThemeId): VisualTheme =
        allThemes.find { it.id == id } ?: ModernDark
}
