package com.watchblocks.puzzle.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

const val GRID_SIZE = 8

/**
 * Flat cell indices that were just cleared, tagged with an id so the
 * UI can tell one clear event from the next even if the same indices
 * happen to repeat. Game logic has already cleared these cells by the
 * time this is set -- it exists purely so the UI can flash them.
 */
data class ClearFlash(val cellsIndices: List<Int>, val id: Long)

/**
 * Holds all mutable game state as Compose state, so any composable
 * reading it recomposes automatically after a move. Grid cells hold
 * -1 for empty or a color index (0..5) for filled.
 */
class GameState {

    val cells: SnapshotStateList<Int> = mutableStateListOf<Int>().apply {
        repeat(GRID_SIZE * GRID_SIZE) { add(-1) }
    }

    var score by mutableIntStateOf(0)
        private set

    var best by mutableIntStateOf(0)

    var pieces by mutableStateOf<List<Piece?>>(PieceShapes.randomSet(3))
        private set

    var gameOver by mutableStateOf(false)
        private set

    var flash by mutableStateOf<ClearFlash?>(null)
        private set

    private var flashIdCounter = 0L

    init {
        seedInitialBoard()
    }

    fun cellAt(r: Int, c: Int): Int = cells[r * GRID_SIZE + c]

    private fun setCellAt(r: Int, c: Int, v: Int) {
        cells[r * GRID_SIZE + c] = v
    }

    fun canPlace(piece: Piece, atRow: Int, atCol: Int): Boolean {
        for ((dr, dc) in piece.cells) {
            val r = atRow + dr
            val c = atCol + dc
            if (r !in 0 until GRID_SIZE || c !in 0 until GRID_SIZE) return false
            if (cellAt(r, c) != -1) return false
        }
        return true
    }

    private fun findAnyValidPlacement(piece: Piece): Pair<Int, Int>? {
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (canPlace(piece, r, c)) return r to c
            }
        }
        return null
    }

    fun placePiece(pieceIndex: Int, atRow: Int, atCol: Int): Boolean {
        val piece = pieces.getOrNull(pieceIndex) ?: return false
        if (!canPlace(piece, atRow, atCol)) return false

        for ((dr, dc) in piece.cells) {
            setCellAt(atRow + dr, atCol + dc, piece.colorIndex)
        }
        score += piece.cells.size

        val newPieces = pieces.toMutableList()
        newPieces[pieceIndex] = null

        val cleared = clearLines()
        if (cleared > 0) {
            score += cleared * 10 + (cleared - 1).coerceAtLeast(0) * 5
        }

        pieces = if (newPieces.all { it == null }) {
            PieceShapes.randomSet(3)
        } else {
            newPieces
        }

        if (score > best) best = score

        checkGameOver()
        return true
    }

    private fun clearLines(): Int {
        val fullRows = (0 until GRID_SIZE).filter { r ->
            (0 until GRID_SIZE).all { c -> cellAt(r, c) != -1 }
        }
        val fullCols = (0 until GRID_SIZE).filter { c ->
            (0 until GRID_SIZE).all { r -> cellAt(r, c) != -1 }
        }

        if (fullRows.isEmpty() && fullCols.isEmpty()) return 0

        val flatIndices = LinkedHashSet<Int>()
        for (r in fullRows) for (c in 0 until GRID_SIZE) flatIndices.add(r * GRID_SIZE + c)
        for (c in fullCols) for (r in 0 until GRID_SIZE) flatIndices.add(r * GRID_SIZE + c)

        flashIdCounter++
        flash = ClearFlash(flatIndices.toList(), flashIdCounter)

        for (r in fullRows) for (c in 0 until GRID_SIZE) setCellAt(r, c, -1)
        for (c in fullCols) for (r in 0 until GRID_SIZE) setCellAt(r, c, -1)

        return fullRows.size + fullCols.size
    }

    private fun checkGameOver() {
        val remaining = pieces.filterNotNull()
        if (remaining.isEmpty()) {
            gameOver = false
            return
        }
        gameOver = remaining.none { findAnyValidPlacement(it) != null }
    }

    /**
     * Scatters a handful of pre-filled cells at the start of a game,
     * like the source game does, capping how many land in any one
     * row/column so nothing clears before the player has even moved.
     */
    private fun seedInitialBoard() {
        val perRow = IntArray(GRID_SIZE)
        val perCol = IntArray(GRID_SIZE)
        var placed = 0
        var attempts = 0
        val target = 10

        while (placed < target && attempts < 300) {
            attempts++
            val r = (0 until GRID_SIZE).random()
            val c = (0 until GRID_SIZE).random()
            if (cellAt(r, c) != -1) continue
            if (perRow[r] >= 4 || perCol[c] >= 4) continue
            setCellAt(r, c, (0 until 6).random())
            perRow[r]++
            perCol[c]++
            placed++
        }
    }

    fun reset() {
        for (i in cells.indices) cells[i] = -1
        score = 0
        pieces = PieceShapes.randomSet(3)
        gameOver = false
        flash = null
        seedInitialBoard()
    }
}
