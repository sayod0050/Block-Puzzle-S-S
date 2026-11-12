package com.example.model

enum class AchievementId {
    FIRST_STEPS,
    GETTING_BETTER,
    COMBO_MASTER,
    DAILY_CHAMPION,
    PUZZLE_EXPERT,
    PERFECT_PLANNING,
    DEDICATED_PLAYER,
    TIME_FIGHTER,
    BOMB_SQUAD,
    HEXA_NOVICE,
    STAR_COLLECTOR,
    SS_CHAMPION
}

data class Achievement(
    val id: AchievementId,
    val title: (LocalizedStrings) -> String,
    val description: (LocalizedStrings) -> String,
    val iconName: String,
    val target: Int,
    val rewardCoins: Int
)

object AchievementsList {
    val all = listOf(
        Achievement(
            id = AchievementId.FIRST_STEPS,
            title = { "First Steps" },
            description = { "Complete your first game in any mode" },
            iconName = "flag",
            target = 1,
            rewardCoins = 100
        ),
        Achievement(
            id = AchievementId.GETTING_BETTER,
            title = { "Getting Better" },
            description = { "Reach a single-game score of 1,000 points" },
            iconName = "trending_up",
            target = 1000,
            rewardCoins = 150
        ),
        Achievement(
            id = AchievementId.COMBO_MASTER,
            title = { "Combo Master" },
            description = { "Achieve a combo multiplier of x3 or higher" },
            iconName = "bolt",
            target = 3,
            rewardCoins = 200
        ),
        Achievement(
            id = AchievementId.DAILY_CHAMPION,
            title = { "Daily Champion" },
            description = { "Complete 3 Daily Challenges" },
            iconName = "event_available",
            target = 3,
            rewardCoins = 250
        ),
        Achievement(
            id = AchievementId.PUZZLE_EXPERT,
            title = { "Puzzle Expert" },
            description = { "Clear 50 total lines in Puzzle Challenge mode" },
            iconName = "psychology",
            target = 50,
            rewardCoins = 200
        ),
        Achievement(
            id = AchievementId.PERFECT_PLANNING,
            title = { "Perfect Planning" },
            description = { "Clear 3 or more lines in a single block drop" },
            iconName = "stars",
            target = 3,
            rewardCoins = 300
        ),
        Achievement(
            id = AchievementId.DEDICATED_PLAYER,
            title = { "Dedicated Player" },
            description = { "Clear a total of 250 lines across all games" },
            iconName = "military_tech",
            target = 250,
            rewardCoins = 350
        ),
        Achievement(
            id = AchievementId.TIME_FIGHTER,
            title = { "Time Fighter" },
            description = { "Score over 1,500 points in Time Attack mode" },
            iconName = "timer",
            target = 1500,
            rewardCoins = 250
        ),
        Achievement(
            id = AchievementId.BOMB_SQUAD,
            title = { "Bomb Squad" },
            description = { "Defuse 10 bombs in Bomb Challenge mode" },
            iconName = "shield",
            target = 10,
            rewardCoins = 300
        ),
        Achievement(
            id = AchievementId.HEXA_NOVICE,
            title = { "Hexa Strategist" },
            description = { "Score 800 points in Hexa Mode" },
            iconName = "hexagon",
            target = 800,
            rewardCoins = 250
        ),
        Achievement(
            id = AchievementId.STAR_COLLECTOR,
            title = { "Star Collector" },
            description = { "Earn 15 stars in Adventure Mode" },
            iconName = "grade",
            target = 15,
            rewardCoins = 400
        ),
        Achievement(
            id = AchievementId.SS_CHAMPION,
            title = { "S&S Champion" },
            description = { "Reach a master score of 5,000 points in any mode" },
            iconName = "emoji_events",
            target = 5000,
            rewardCoins = 500
        )
    )
}
