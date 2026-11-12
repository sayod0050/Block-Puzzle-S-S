package com.example.model

import kotlin.random.Random

data class BlockShape(
    val id: String,
    val coordinates: List<Pair<Int, Int>>, // relative (row, col) or (q, r)
    val colorIndex: Int,
    val isHex: Boolean = false
) {
    val rowSpan: Int
        get() = (coordinates.maxOfOrNull { it.first } ?: 0) - (coordinates.minOfOrNull { it.first } ?: 0) + 1

    val colSpan: Int
        get() = (coordinates.maxOfOrNull { it.second } ?: 0) - (coordinates.minOfOrNull { it.second } ?: 0) + 1

    val minRow: Int
        get() = coordinates.minOfOrNull { it.first } ?: 0

    val minCol: Int
        get() = coordinates.minOfOrNull { it.second } ?: 0

    val normalizedCoordinates: List<Pair<Int, Int>> by lazy {
        val mr = minRow
        val mc = minCol
        coordinates.map { Pair(it.first - mr, it.second - mc) }
    }
}

object BlockShapeFactory {

    private val squareShapes = listOf(
        // 1x1 Dot
        listOf(Pair(0, 0)),

        // 2x1 & 1x2 Lines
        listOf(Pair(0, 0), Pair(0, 1)),
        listOf(Pair(0, 0), Pair(1, 0)),

        // 3x1 & 1x3 Lines
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),

        // 4x1 & 1x4 Lines
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0)),

        // 5x1 & 1x5 Lines
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(4, 0)),

        // 2x2 Square
        listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1)),

        // 3x3 Square
        listOf(
            Pair(0, 0), Pair(0, 1), Pair(0, 2),
            Pair(1, 0), Pair(1, 1), Pair(1, 2),
            Pair(2, 0), Pair(2, 1), Pair(2, 2)
        ),

        // 2x2 Mini L corners (3 blocks)
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1)),
        listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1)),

        // 3x3 Full L shapes (5 blocks)
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1), Pair(2, 2)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(2, 0)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2), Pair(2, 2)),
        listOf(Pair(0, 2), Pair(1, 2), Pair(2, 0), Pair(2, 1), Pair(2, 2)),

        // T-Shapes (4 blocks)
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 1)),
        listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(0, 1)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(1, 1)),
        listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1), Pair(1, 0)),

        // Z & S shapes (4 blocks)
        listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2)),
        listOf(Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(1, 1)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1)),
        listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(2, 0)),

        // Plus Cross (5 blocks)
        listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(2, 1))
    )

    private val hexShapes = listOf(
        // Single Hex
        listOf(Pair(0, 0)),

        // 2-Hex lines
        listOf(Pair(0, 0), Pair(1, 0)),
        listOf(Pair(0, 0), Pair(0, 1)),
        listOf(Pair(0, 0), Pair(1, -1)),

        // 3-Hex lines
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
        listOf(Pair(0, 0), Pair(1, -1), Pair(2, -2)),

        // 4-Hex lines
        listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0)),

        // Triangle (3 hexes)
        listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)),
        listOf(Pair(0, 0), Pair(0, 1), Pair(-1, 1)),

        // Rhombus (4 hexes)
        listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1)),

        // Chevron / Angle (3 hexes)
        listOf(Pair(1, 0), Pair(0, 0), Pair(0, 1)),
        listOf(Pair(0, 0), Pair(1, 0), Pair(1, -1)),

        // Flower 4-hex cluster
        listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, -1))
    )

    fun createRandomStandardPiece(colorPaletteSize: Int = 7): BlockShape {
        val coords = squareShapes.random()
        val colorIdx = Random.nextInt(colorPaletteSize)
        val uid = "sq_${System.nanoTime()}_${Random.nextInt(1000)}"
        return BlockShape(id = uid, coordinates = coords, colorIndex = colorIdx, isHex = false)
    }

    fun createRandomHexPiece(colorPaletteSize: Int = 7): BlockShape {
        val coords = hexShapes.random()
        val colorIdx = Random.nextInt(colorPaletteSize)
        val uid = "hex_${System.nanoTime()}_${Random.nextInt(1000)}"
        return BlockShape(id = uid, coordinates = coords, colorIndex = colorIdx, isHex = true)
    }

    fun generatePieces(isHex: Boolean, count: Int = 3, colorPaletteSize: Int = 7): List<BlockShape> {
        return List(count) {
            if (isHex) createRandomHexPiece(colorPaletteSize)
            else createRandomStandardPiece(colorPaletteSize)
        }
    }
}
