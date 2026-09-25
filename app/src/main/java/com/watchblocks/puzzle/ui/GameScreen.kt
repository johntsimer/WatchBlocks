package com.watchblocks.puzzle.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.watchblocks.puzzle.game.ClearFlash
import com.watchblocks.puzzle.game.GRID_SIZE
import com.watchblocks.puzzle.game.GameState
import com.watchblocks.puzzle.game.Piece
import kotlin.math.roundToInt

val BlockColors = listOf(
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFFFB300),
    Color(0xFF8E24AA),
    Color(0xFF00ACC1)
)

@Composable
fun GameScreen(gameState: GameState = remember { GameState() }) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenMinDp = minOf(maxWidth, maxHeight)
        val safeSize = screenMinDp * 0.80f

        val boardSizeDp = safeSize * 0.64f
        val cellSizeDp = boardSizeDp / GRID_SIZE
        val traySlotDp = boardSizeDp / 3.0f
        val traySpacerDp = safeSize * 0.025f

        val density = LocalDensity.current
        val cellSizePx = with(density) { cellSizeDp.toPx() }
        val traySlotPx = with(density) { traySlotDp.toPx() }

        var gridBounds by remember { mutableStateOf(Rect.Zero) }
        var draggingIndex by remember { mutableStateOf(-1) }
        var dragPosRoot by remember { mutableStateOf(Offset.Zero) }
        val trayItemCenters = remember { mutableStateMapOf<Int, Offset>() }

        // Fade-out flash drawn over cells that just cleared.
        val flashAlpha = remember { Animatable(0f) }
        val flash: ClearFlash? = gameState.flash
        LaunchedEffect(flash?.id) {
            if (flash != null) {
                flashAlpha.snapTo(1f)
                flashAlpha.animateTo(0f, animationSpec = tween(260))
            }
        }

        fun targetCell(piece: Piece): Pair<Int, Int> {
            val relative = dragPosRoot - gridBounds.topLeft
            val liftPx = cellSizePx * 1.3f
            val colF = relative.x / cellSizePx
            val rowF = (relative.y - liftPx) / cellSizePx
            val targetCol = (colF - piece.width / 2f).roundToInt()
            val targetRow = (rowF - piece.height / 2f).roundToInt()
            return targetRow to targetCol
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(safeSize)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameState.score}",
                        color = Color.White,
                        fontSize = (safeSize.value * 0.15f).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BEST ${gameState.best}",
                        color = Color.Gray,
                        fontSize = (safeSize.value * 0.055f).sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Grid, left side
                    Box(
                        modifier = Modifier
                            .size(boardSizeDp)
                            .onGloballyPositioned { coords ->
                                gridBounds = coords.boundsInRoot()
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawGrid(gameState, cellSizePx)
                        }

                        if (flash != null && flashAlpha.value > 0f) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawFlash(flash, cellSizePx, flashAlpha.value)
                            }
                        }

                        if (draggingIndex >= 0) {
                            val piece = gameState.pieces.getOrNull(draggingIndex)
                            if (piece != null) {
                                val (targetRow, targetCol) = targetCell(piece)
                                val valid = gameState.canPlace(piece, targetRow, targetCol)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawGhostPiece(piece, targetRow, targetCol, cellSizePx, valid)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(traySpacerDp))

                    // Piece tray, right side, stacked vertically
                    Column(
                        modifier = Modifier.height(boardSizeDp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (index in gameState.pieces.indices) {
                            val piece = gameState.pieces[index]
                            val isSelected = draggingIndex == index
                            Box(
                                modifier = Modifier
                                    .size(traySlotDp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(0xFF3A3A3E) else Color(0xFF1C1C1E)
                                    )
                                    .onGloballyPositioned { coords ->
                                        trayItemCenters[index] = coords.boundsInRoot().center
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (piece != null) {
                                    Canvas(
                                        modifier = Modifier
                                            .size(traySlotDp)
                                            .pointerInput(piece) {
                                                detectDragGestures(
                                                    onDragStart = { _ ->
                                                        draggingIndex = index
                                                        dragPosRoot = trayItemCenters[index] ?: Offset.Zero
                                                    },
                                                    onDrag = { change, amount ->
                                                        change.consume()
                                                        dragPosRoot += amount
                                                    },
                                                    onDragEnd = {
                                                        val p = gameState.pieces.getOrNull(draggingIndex)
                                                        if (p != null) {
                                                            val (r, c) = targetCell(p)
                                                            gameState.placePiece(draggingIndex, r, c)
                                                        }
                                                        draggingIndex = -1
                                                    },
                                                    onDragCancel = {
                                                        draggingIndex = -1
                                                    }
                                                )
                                            }
                                    ) {
                                        // Scale each piece to fit its slot regardless
                                        // of how wide or tall it is -- this is what
                                        // was clipping the bigger pieces before.
                                        val maxDim = maxOf(piece.width, piece.height)
                                        val fittedCellPx = (traySlotPx * 0.8f) / maxDim
                                        val alpha = if (isSelected) 0.35f else 1f
                                        drawTrayPiece(piece, fittedCellPx, alpha)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gameState.gameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .pointerInput(Unit) {
                        detectTapGestures { gameState.reset() }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GAME OVER",
                        color = Color.White,
                        fontSize = (safeSize.value * 0.11f).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Score ${gameState.score}",
                        color = Color.White,
                        fontSize = (safeSize.value * 0.07f).sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "tap to restart",
                        color = Color.Gray,
                        fontSize = (safeSize.value * 0.06f).sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGrid(gameState: GameState, cellPx: Float) {
    for (r in 0 until GRID_SIZE) {
        for (c in 0 until GRID_SIZE) {
            val v = gameState.cellAt(r, c)
            val color = if (v >= 0) BlockColors[v % BlockColors.size] else Color(0xFF1C1C1E)
            drawRect(
                color = color,
                topLeft = Offset(c * cellPx, r * cellPx),
                size = Size(cellPx - 1.5f, cellPx - 1.5f)
            )
        }
    }
}

private fun DrawScope.drawFlash(flash: ClearFlash, cellPx: Float, alpha: Float) {
    for (idx in flash.cellsIndices) {
        val r = idx / GRID_SIZE
        val c = idx % GRID_SIZE
        drawRect(
            color = Color.White.copy(alpha = alpha),
            topLeft = Offset(c * cellPx, r * cellPx),
            size = Size(cellPx - 1.5f, cellPx - 1.5f)
        )
    }
}

private fun DrawScope.drawGhostPiece(piece: Piece, row: Int, col: Int, cellPx: Float, valid: Boolean) {
    val tint = if (valid) Color(0xFF43A047) else Color(0xFFE53935)
    for ((dr, dc) in piece.cells) {
        val r = row + dr
        val c = col + dc
        if (r in 0 until GRID_SIZE && c in 0 until GRID_SIZE) {
            drawRect(
                color = tint.copy(alpha = 0.55f),
                topLeft = Offset(c * cellPx, r * cellPx),
                size = Size(cellPx - 1.5f, cellPx - 1.5f)
            )
        }
    }
}

private fun DrawScope.drawTrayPiece(piece: Piece, cellPx: Float, alpha: Float) {
    val color = BlockColors[piece.colorIndex % BlockColors.size].copy(alpha = alpha)
    val offsetX = (size.width - piece.width * cellPx) / 2f
    val offsetY = (size.height - piece.height * cellPx) / 2f
    for ((dr, dc) in piece.cells) {
        drawRect(
            color = color,
            topLeft = Offset(offsetX + dc * cellPx, offsetY + dr * cellPx),
            size = Size(cellPx - 1.5f, cellPx - 1.5f)
        )
    }
}
