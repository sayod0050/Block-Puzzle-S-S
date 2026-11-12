package com.example.model

data class AdventureStage(
    val levelNumber: Int,
    val targetScore: Int,
    val targetLines: Int = 0,
    val maxMoves: Int = 0,
    val preFilledCells: List<Pair<Int, Int>> = emptyList(),
    val star1Score: Int,
    val star2Score: Int,
    val star3Score: Int,
    val rewardCoins: Int = 50
)

object AdventureStages {
    val stages = (1..20).map { lvl ->
        val baseScore = 400 + (lvl * 200)
        val obstacles = when (lvl) {
            4 -> listOf(Pair(0, 0), Pair(0, 9), Pair(9, 0), Pair(9, 9))
            8 -> listOf(Pair(4, 4), Pair(4, 5), Pair(5, 4), Pair(5, 5))
            12 -> listOf(Pair(2, 2), Pair(2, 7), Pair(7, 2), Pair(7, 7))
            16 -> listOf(Pair(3, 3), Pair(3, 6), Pair(6, 3), Pair(6, 6), Pair(4, 4), Pair(5, 5))
            20 -> listOf(Pair(1, 1), Pair(1, 8), Pair(8, 1), Pair(8, 8), Pair(4, 5), Pair(5, 4))
            else -> emptyList()
        }
        val targetLines = if (lvl % 2 == 0) (4 + lvl / 2) else 0

        AdventureStage(
            levelNumber = lvl,
            targetScore = baseScore,
            targetLines = targetLines,
            maxMoves = if (lvl % 3 == 0) (15 + lvl) else 0,
            preFilledCells = obstacles,
            star1Score = baseScore,
            star2Score = (baseScore * 1.35).toInt(),
            star3Score = (baseScore * 1.75).toInt(),
            rewardCoins = 50 + lvl * 15
        )
    }

    fun getStage(level: Int): AdventureStage =
        stages.find { it.levelNumber == level } ?: stages.first()
}
