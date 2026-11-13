package com.example.model

data class HexCell(
    val q: Int,
    val r: Int,
    val isFilled: Boolean = false,
    val colorIndex: Int = 0
) {
    val s: Int get() = -q - r
}

class HexGameBoard(val radius: Int = 3) {
    // Valid coordinates inside regular hexagon of given radius
    val validCoords: Set<Pair<Int, Int>> by lazy {
        val set = mutableSetOf<Pair<Int, Int>>()
        for (q in -radius..radius) {
            val r1 = maxOf(-radius, -q - radius)
            val r2 = minOf(radius, -q + radius)
            for (r in r1..r2) {
                set.add(Pair(q, r))
            }
        }
        set
    }

    private val grid = mutableMapOf<Pair<Int, Int>, HexCell>()

    init {
        reset()
    }

    fun reset() {
        grid.clear()
        for (coord in validCoords) {
            grid[coord] = HexCell(coord.first, coord.second, isFilled = false)
        }
    }

    fun getCell(q: Int, r: Int): HexCell? = grid[Pair(q, r)]

    fun canPlacePiece(piece: BlockShape, startQ: Int, startR: Int): Boolean {
        for ((dq, dr) in piece.normalizedCoordinates) {
            val targetCoord = Pair(startQ + dq, startR + dr)
            if (!validCoords.contains(targetCoord)) return false
            if (grid[targetCoord]?.isFilled == true) return false
        }
        return true
    }

    fun canPlacePieceAnywhere(piece: BlockShape): Boolean {
        for (coord in validCoords) {
            if (canPlacePiece(piece, coord.first, coord.second)) return true
        }
        return false
    }

    fun placePiece(piece: BlockShape, startQ: Int, startR: Int): List<Pair<Int, Int>> {
        val placed = mutableListOf<Pair<Int, Int>>()
        for ((dq, dr) in piece.normalizedCoordinates) {
            val targetCoord = Pair(startQ + dq, startR + dr)
            grid[targetCoord] = HexCell(
                q = targetCoord.first,
                r = targetCoord.second,
                isFilled = true,
                colorIndex = piece.colorIndex
            )
            placed.add(targetCoord)
        }
        return placed
    }

    fun checkAndClearLines(): ClearResult {
        val linesToClear = mutableListOf<List<Pair<Int, Int>>>()

        // 1. Constant q lines
        for (q in -radius..radius) {
            val lineCells = validCoords.filter { it.first == q }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true }) {
                linesToClear.add(lineCells)
            }
        }

        // 2. Constant r lines
        for (r in -radius..radius) {
            val lineCells = validCoords.filter { it.second == r }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true }) {
                linesToClear.add(lineCells)
            }
        }

        // 3. Constant s = -q - r lines
        for (s in -radius..radius) {
            val lineCells = validCoords.filter { -it.first - it.second == s }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true }) {
                linesToClear.add(lineCells)
            }
        }

        val clearedCoords = mutableSetOf<Pair<Int, Int>>()
        for (line in linesToClear) {
            for (coord in line) {
                clearedCoords.add(coord)
                grid[coord] = HexCell(coord.first, coord.second, isFilled = false)
            }
        }

        return ClearResult(
            clearedCount = linesToClear.size,
            clearedCells = clearedCoords.toList(),
            defusedBombs = 0
        )
    }

    fun snapshotCells(): Map<Pair<Int, Int>, HexCell> {
        return grid.toMap()
    }

    fun getWillClearCells(piece: BlockShape, startQ: Int, startR: Int): Set<Pair<Int, Int>> {
        if (!canPlacePiece(piece, startQ, startR)) return emptySet()
        val projected = piece.normalizedCoordinates.map { Pair(startQ + it.first, startR + it.second) }.toSet()
        val willClear = mutableSetOf<Pair<Int, Int>>()

        for (q in -radius..radius) {
            val lineCells = validCoords.filter { it.first == q }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true || projected.contains(it) }) {
                willClear.addAll(lineCells)
            }
        }
        for (r in -radius..radius) {
            val lineCells = validCoords.filter { it.second == r }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true || projected.contains(it) }) {
                willClear.addAll(lineCells)
            }
        }
        for (s in -radius..radius) {
            val lineCells = validCoords.filter { -it.first - it.second == s }
            if (lineCells.isNotEmpty() && lineCells.all { grid[it]?.isFilled == true || projected.contains(it) }) {
                willClear.addAll(lineCells)
            }
        }
        return willClear
    }
}
