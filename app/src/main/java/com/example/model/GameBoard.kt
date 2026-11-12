package com.example.model

data class GridCell(
    val isFilled: Boolean = false,
    val colorIndex: Int = 0,
    val bombCountdown: Int? = null,
    val isObstacle: Boolean = false
)

data class ClearResult(
    val clearedCount: Int,
    val clearedCells: List<Pair<Int, Int>>,
    val defusedBombs: Int
)

class StandardGameBoard(val size: Int = 10) {
    private val grid: Array<Array<GridCell>> = Array(size) { Array(size) { GridCell() } }

    fun getCell(row: Int, col: Int): GridCell? {
        if (row !in 0 until size || col !in 0 until size) return null
        return grid[row][col]
    }

    fun setCell(row: Int, col: Int, cell: GridCell) {
        if (row in 0 until size && col in 0 until size) {
            grid[row][col] = cell
        }
    }

    fun canPlacePiece(piece: BlockShape, startRow: Int, startCol: Int): Boolean {
        for ((r, c) in piece.normalizedCoordinates) {
            val targetR = startRow + r
            val targetC = startCol + c
            if (targetR !in 0 until size || targetC !in 0 until size) return false
            if (grid[targetR][targetC].isFilled) return false
        }
        return true
    }

    fun canPlacePieceAnywhere(piece: BlockShape): Boolean {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (canPlacePiece(piece, r, c)) return true
            }
        }
        return false
    }

    fun placePiece(piece: BlockShape, startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val placed = mutableListOf<Pair<Int, Int>>()
        for ((r, c) in piece.normalizedCoordinates) {
            val targetR = startRow + r
            val targetC = startCol + c
            grid[targetR][targetC] = GridCell(
                isFilled = true,
                colorIndex = piece.colorIndex,
                bombCountdown = null,
                isObstacle = false
            )
            placed.add(Pair(targetR, targetC))
        }
        return placed
    }

    fun tickBombs(): Boolean {
        var exploded = false
        for (r in 0 until size) {
            for (c in 0 until size) {
                val cell = grid[r][c]
                if (cell.isFilled && cell.bombCountdown != null) {
                    val nextCount = cell.bombCountdown - 1
                    if (nextCount <= 0) {
                        exploded = true
                    }
                    grid[r][c] = cell.copy(bombCountdown = nextCount)
                }
            }
        }
        return exploded
    }

    fun addBomb(count: Int = 9): Boolean {
        // Find empty cells to place a bomb
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!grid[r][c].isFilled) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }
        if (emptyCells.isEmpty()) return false
        val (r, c) = emptyCells.random()
        grid[r][c] = GridCell(isFilled = true, colorIndex = 1, bombCountdown = count)
        return true
    }

    fun checkAndClearLines(): ClearResult {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        // Check rows
        for (r in 0 until size) {
            var full = true
            for (c in 0 until size) {
                if (!grid[r][c].isFilled) {
                    full = false
                    break
                }
            }
            if (full) rowsToClear.add(r)
        }

        // Check columns
        for (c in 0 until size) {
            var full = true
            for (r in 0 until size) {
                if (!grid[r][c].isFilled) {
                    full = false
                    break
                }
            }
            if (full) colsToClear.add(c)
        }

        val clearedCoords = mutableSetOf<Pair<Int, Int>>()
        var defusedBombs = 0

        for (r in rowsToClear) {
            for (c in 0 until size) {
                clearedCoords.add(Pair(r, c))
            }
        }
        for (c in colsToClear) {
            for (r in 0 until size) {
                clearedCoords.add(Pair(r, c))
            }
        }

        for ((r, c) in clearedCoords) {
            if (grid[r][c].bombCountdown != null) {
                defusedBombs++
            }
            grid[r][c] = GridCell()
        }

        val totalLines = rowsToClear.size + colsToClear.size
        return ClearResult(totalLines, clearedCoords.toList(), defusedBombs)
    }

    fun reset() {
        for (r in 0 until size) {
            for (c in 0 until size) {
                grid[r][c] = GridCell()
            }
        }
    }

    fun loadObstacles(obstacles: List<Pair<Int, Int>>) {
        reset()
        for ((r, c) in obstacles) {
            if (r in 0 until size && c in 0 until size) {
                grid[r][c] = GridCell(isFilled = true, colorIndex = 6, isObstacle = true)
            }
        }
    }

    fun createSnapshot(): Array<Array<GridCell>> {
        return Array(size) { r -> Array(size) { c -> grid[r][c] } }
    }

    fun restoreSnapshot(snapshot: Array<Array<GridCell>>) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                grid[r][c] = snapshot[r][c]
            }
        }
    }
}
