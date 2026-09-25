package com.watchblocks.puzzle.game

/**
 * A piece is a set of (row, col) offsets, normalized so the smallest
 * row and column are both 0. [colorIndex] picks a color from the
 * shared palette used by the grid and tray renderers.
 */
data class Piece(
    val cells: List<Pair<Int, Int>>,
    val colorIndex: Int
) {
    val width: Int get() = cells.maxOf { it.second } + 1
    val height: Int get() = cells.maxOf { it.first } + 1
}

object PieceShapes {

    // Relative (row, col) offsets. No rotations, matching the source
    // game's behavior.
    private val shapes: List<List<Pair<Int, Int>>> = listOf(
        // Single
        listOf(0 to 0),

        // Dominoes
        listOf(0 to 0, 0 to 1),
        listOf(0 to 0, 1 to 0),

        // Trominoes
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(0 to 0, 1 to 0, 2 to 0),
        listOf(0 to 0, 1 to 0, 1 to 1),
        listOf(0 to 0, 0 to 1, 1 to 0),
        listOf(0 to 0, 0 to 1, 1 to 1),
        listOf(0 to 1, 1 to 0, 1 to 1),

        // Square
        listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),

        // Tetromino I
        listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3),
        listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0),

        // Tetromino L / J
        listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1),
        listOf(0 to 1, 1 to 1, 2 to 0, 2 to 1),
        listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0),
        listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2),

        // Tetromino S / Z
        listOf(0 to 1, 0 to 2, 1 to 0, 1 to 1),
        listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2),

        // Short T (3-wide, matches the request to keep these instead
        // of the plus/cross pentomino)
        listOf(0 to 0, 0 to 1, 0 to 2, 1 to 1),
        listOf(0 to 1, 1 to 0, 1 to 1, 1 to 2),

        // Pentomino long L
        listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 3 to 1),

        // Pentomino I
        listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4),

        // 3x3 corner
        listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0)
    )

    fun random(): Piece {
        val shape = shapes.random()
        val colorIndex = (0 until 6).random()
        return Piece(shape, colorIndex)
    }

    fun randomSet(count: Int = 3): List<Piece> = List(count) { random() }
}
