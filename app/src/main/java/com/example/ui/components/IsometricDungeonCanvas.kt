package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.abs

/** Geometria do tabuleiro: células sempre QUADRADAS e centralizadas (sem distorção em telas longas). */
private data class BoardGeometry(val cell: Float, val offsetX: Float, val offsetY: Float) {
    companion object {
        fun of(width: Float, height: Float, cols: Int, rows: Int): BoardGeometry {
            val cell = minOf(width / cols, height / rows)
            return BoardGeometry(cell, (width - cell * cols) / 2f, (height - cell * rows) / 2f)
        }
    }
}

private fun tileColor(type: TileType): Color = when (type) {
    TileType.WALL -> Color(0xFF1E1E26)
    TileType.FLOOR -> Color(0xFF2B2B38)
    TileType.DOOR -> Color(0xFF854D0E)
    TileType.SARCOPHAGUS -> Color(0xFF57534E)
    TileType.CHEST -> GoldSecondary
    TileType.ALTAR -> ArcanePurple
    TileType.TRAP_SPIKE -> Color(0xFF7F1D1D)
    TileType.TRAP_POISON -> Color(0xCC22C55E)
    TileType.SECRET_DOOR -> Color(0xFF3B82F6)
    TileType.FOUNTAIN -> ArcaneCyan
    TileType.RUBBLE -> Color(0xFF44403C)
    TileType.TORCH_STAND -> TorchOrange
    TileType.EXIT_STAIRS -> GoldPrimary
    TileType.FISSURE -> Color(0xFF171717)
    TileType.TOXIC_GAS -> Color(0x8022C55E)
}

private val UnexploredColor = Color(0xFF08080A)
private val WallAccent = Color(0xFF121216)
private val UsedMarker = Color(0x99000000)

@Composable
fun IsometricDungeonCanvas(
    map: DungeonMap,
    playerX: Int,
    playerY: Int,
    enemies: List<Enemy> = emptyList(),
    torchRadius: Float = 3.5f,
    onTileClick: ((Int, Int) -> Unit)? = null,
    onSwipe: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Callbacks sempre atualizados sem reiniciar os detectores de gesto a cada recomposição.
    val currentTileClick by rememberUpdatedState(onTileClick)
    val currentSwipe by rememberUpdatedState(onSwipe)
    val cols = map.width
    val rows = map.height
    val hasTap = onTileClick != null
    val hasSwipe = onSwipe != null

    val tapModifier = if (hasTap) {
        Modifier.pointerInput(cols, rows) {
            detectTapGestures { offset ->
                val g = BoardGeometry.of(size.width.toFloat(), size.height.toFloat(), cols, rows)
                val lx = offset.x - g.offsetX
                val ly = offset.y - g.offsetY
                if (lx < 0 || ly < 0) return@detectTapGestures
                val tx = (lx / g.cell).toInt()
                val ty = (ly / g.cell).toInt()
                if (tx in 0 until cols && ty in 0 until rows) currentTileClick?.invoke(tx, ty)
            }
        }
    } else Modifier

    val swipeModifier = if (hasSwipe) {
        Modifier.pointerInput(Unit) {
            var totalX = 0f
            var totalY = 0f
            val threshold = 24.dp.toPx()
            detectDragGestures(
                onDragStart = {
                    totalX = 0f
                    totalY = 0f
                },
                onDragEnd = {
                    if (maxOf(abs(totalX), abs(totalY)) >= threshold) {
                        if (abs(totalX) > abs(totalY)) {
                            currentSwipe?.invoke(if (totalX > 0) 1 else -1, 0)
                        } else {
                            currentSwipe?.invoke(0, if (totalY > 0) 1 else -1)
                        }
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    totalX += dragAmount.x
                    totalY += dragAmount.y
                }
            )
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBg)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(tapModifier)
                .then(swipeModifier)
        ) {
            val g = BoardGeometry.of(size.width, size.height, cols, rows)
            val cell = g.cell
            val cellSize = Size(cell, cell)
            val innerSize = Size(cell - 2f, cell - 2f)

            // 1. Piso, paredes e objetos
            for (tile in map.tiles) {
                val left = g.offsetX + tile.x * cell
                val top = g.offsetY + tile.y * cell

                if (!tile.isExplored) {
                    drawRect(color = UnexploredColor, topLeft = Offset(left, top), size = cellSize)
                    continue
                }

                val base = tileColor(tile.type)
                drawRect(
                    color = if (tile.isVisible) base else base.copy(alpha = base.alpha * 0.35f),
                    topLeft = Offset(left + 1f, top + 1f),
                    size = innerSize
                )
                drawRect(
                    color = DarkBorder.copy(alpha = if (tile.isVisible) 0.4f else 0.15f),
                    topLeft = Offset(left, top),
                    size = cellSize,
                    style = Stroke(width = 1f)
                )

                if (!tile.isVisible) continue
                val cx = left + cell / 2
                val cy = top + cell / 2
                when (tile.type) {
                    TileType.WALL -> drawLine(
                        color = WallAccent,
                        start = Offset(left + 2f, cy),
                        end = Offset(left + cell - 2f, cy),
                        strokeWidth = 1.5f
                    )
                    TileType.SARCOPHAGUS -> drawCircle(color = GoldLight.copy(alpha = 0.8f), radius = cell * 0.25f, center = Offset(cx, cy))
                    TileType.CHEST -> drawRect(
                        color = GoldLight,
                        topLeft = Offset(left + cell * 0.25f, top + cell * 0.25f),
                        size = Size(cell * 0.5f, cell * 0.5f)
                    )
                    TileType.ALTAR -> {
                        val path = Path().apply {
                            moveTo(cx, top + cell * 0.2f)
                            lineTo(left + cell * 0.8f, top + cell * 0.8f)
                            lineTo(left + cell * 0.2f, top + cell * 0.8f)
                            close()
                        }
                        drawPath(path, color = GoldLight, style = Fill)
                    }
                    TileType.EXIT_STAIRS -> drawCircle(color = GoldPrimary, radius = cell * 0.35f, center = Offset(cx, cy), style = Stroke(width = 2f))
                    TileType.TORCH_STAND -> if (!tile.lootClaimed) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(TorchOrange, Color.Transparent),
                                center = Offset(cx, cy),
                                radius = cell * 0.6f
                            ),
                            center = Offset(cx, cy),
                            radius = cell * 0.6f
                        )
                    }
                    else -> {}
                }

                // Objetos já utilizados ficam escurecidos
                if (tile.lootClaimed && tile.type.isInteractive) {
                    drawRect(color = UsedMarker, topLeft = Offset(left + 1f, top + 1f), size = innerSize)
                }
            }

            // 2. Patrulhas inimigas visíveis
            for (enemy in enemies) {
                if (!enemy.isAlive) continue
                val enemyTile = map.tiles.getOrNull(enemy.posY * cols + enemy.posX)
                    ?.takeIf { it.x == enemy.posX && it.y == enemy.posY }
                    ?: map.tiles.find { it.x == enemy.posX && it.y == enemy.posY }
                if (enemyTile?.isVisible != true) continue
                val center = Offset(g.offsetX + enemy.posX * cell + cell / 2, g.offsetY + enemy.posY * cell + cell / 2)
                drawCircle(color = if (enemy.isBoss) BloodCrimson else ArcanePurple, radius = cell * 0.38f, center = center)
                drawCircle(color = Color.White, radius = cell * 0.15f, center = center)
            }

            // 3. Grupo do jogador com aura da tocha
            val playerCenter = Offset(g.offsetX + playerX * cell + cell / 2, g.offsetY + playerY * cell + cell / 2)
            val torchPixelRadius = (torchRadius * cell).coerceAtMost(size.maxDimension)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TorchOrange.copy(alpha = 0.3f), GoldLight.copy(alpha = 0.1f), Color.Transparent),
                    center = playerCenter,
                    radius = torchPixelRadius
                ),
                radius = torchPixelRadius,
                center = playerCenter
            )
            drawCircle(color = GoldPrimary, radius = cell * 0.42f, center = playerCenter)
            drawCircle(color = Color(0xFF1E1B18), radius = cell * 0.32f, center = playerCenter)
            drawCircle(color = ArcaneCyan, radius = cell * 0.18f, center = playerCenter)
        }
    }
}
