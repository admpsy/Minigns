package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun IsometricDungeonCanvas(
    map: DungeonMap,
    playerX: Int,
    playerY: Int,
    enemies: List<Enemy> = emptyList(),
    torchRadius: Float = 3.5f,
    onTileClick: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBg)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(map.id) {
                    if (onTileClick != null) {
                        detectTapGestures { offset ->
                            val cellW = size.width / map.width
                            val cellH = size.height / map.height
                            val tx = (offset.x / cellW).toInt().coerceIn(0, map.width - 1)
                            val ty = (offset.y / cellH).toInt().coerceIn(0, map.height - 1)
                            onTileClick(tx, ty)
                        }
                    }
                }
        ) {
            val cellW = size.width / map.width
            val cellH = size.height / map.height

            // 1. Draw Tiles
            for (tile in map.tiles) {
                val left = tile.x * cellW
                val top = tile.y * cellH
                val rect = Size(cellW, cellH)

                if (!tile.isExplored) {
                    // Unexplored: Pitch Black Void
                    drawRect(
                        color = Color(0xFF08080A),
                        topLeft = Offset(left, top),
                        size = rect
                    )
                    continue
                }

                // Base floor/wall colors based on theme
                val themeColor = Color(map.theme.primaryColorHex)
                val baseTileColor = when (tile.type) {
                    TileType.WALL -> Color(0xFF1E1E26)
                    TileType.FLOOR -> Color(0xFF2B2B38)
                    TileType.DOOR -> Color(0xFF854D0E)
                    TileType.SARCOPHAGUS -> Color(0xFF57534E)
                    TileType.CHEST -> GoldSecondary
                    TileType.ALTAR -> ArcanePurple
                    TileType.TRAP_SPIKE -> Color(0xFF7F1D1D)
                    TileType.TRAP_POISON -> PoisonGreen.copy(alpha = 0.8f)
                    TileType.SECRET_DOOR -> Color(0xFF3B82F6)
                    TileType.FOUNTAIN -> ArcaneCyan
                    TileType.RUBBLE -> Color(0xFF44403C)
                    TileType.TORCH_STAND -> TorchOrange
                    TileType.EXIT_STAIRS -> GoldPrimary
                    TileType.FISSURE -> Color(0xFF171717)
                    TileType.TOXIC_GAS -> PoisonGreen.copy(alpha = 0.5f)
                }

                // If not in direct torch sight, render in shadow
                val finalColor = if (tile.isVisible) baseTileColor else baseTileColor.copy(alpha = 0.35f)

                // Draw Tile background
                drawRect(
                    color = finalColor,
                    topLeft = Offset(left + 1f, top + 1f),
                    size = Size(cellW - 2f, cellH - 2f)
                )

                // Grid lines (Tabletop battle-map grid feeling)
                drawRect(
                    color = DarkBorder.copy(alpha = if (tile.isVisible) 0.4f else 0.15f),
                    topLeft = Offset(left, top),
                    size = rect,
                    style = Stroke(width = 1f)
                )

                // Draw distinctive props when visible or explored
                if (tile.isVisible) {
                    when (tile.type) {
                        TileType.WALL -> {
                            // Wall brick pattern accent
                            drawLine(
                                color = Color(0xFF121216),
                                start = Offset(left + 2f, top + cellH / 2),
                                end = Offset(left + cellW - 2f, top + cellH / 2),
                                strokeWidth = 1.5f
                            )
                        }
                        TileType.SARCOPHAGUS -> {
                            // Sarcophagus emblem
                            drawCircle(
                                color = GoldLight.copy(alpha = 0.8f),
                                radius = minOf(cellW, cellH) * 0.25f,
                                center = Offset(left + cellW / 2, top + cellH / 2)
                            )
                        }
                        TileType.CHEST -> {
                            // Chest icon
                            drawRect(
                                color = GoldLight,
                                topLeft = Offset(left + cellW * 0.25f, top + cellH * 0.25f),
                                size = Size(cellW * 0.5f, cellH * 0.5f)
                            )
                        }
                        TileType.ALTAR -> {
                            // Mystic rune triangle
                            val path = Path().apply {
                                moveTo(left + cellW / 2, top + cellH * 0.2f)
                                lineTo(left + cellW * 0.8f, top + cellH * 0.8f)
                                lineTo(left + cellW * 0.2f, top + cellH * 0.8f)
                                close()
                            }
                            drawPath(path, color = GoldLight, style = Fill)
                        }
                        TileType.EXIT_STAIRS -> {
                            // Stairs descend
                            drawCircle(
                                color = GoldPrimary,
                                radius = minOf(cellW, cellH) * 0.35f,
                                center = Offset(left + cellW / 2, top + cellH / 2),
                                style = Stroke(width = 2f)
                            )
                        }
                        TileType.TORCH_STAND -> {
                            // Torch fire glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(TorchOrange, Color.Transparent),
                                    center = Offset(left + cellW / 2, top + cellH / 2),
                                    radius = minOf(cellW, cellH) * 0.6f
                                ),
                                center = Offset(left + cellW / 2, top + cellH / 2),
                                radius = minOf(cellW, cellH) * 0.6f
                            )
                        }
                        else -> {}
                    }
                }
            }

            // 2. Draw Enemies
            for (enemy in enemies) {
                if (enemy.isAlive) {
                    val ex = enemy.posX * cellW + cellW / 2
                    val ey = enemy.posY * cellH + cellH / 2
                    val enemyTile = map.tiles.find { it.x == enemy.posX && it.y == enemy.posY }

                    if (enemyTile?.isVisible == true) {
                        // Monster token
                        val monsterColor = if (enemy.isBoss) BloodCrimson else ArcanePurple
                        drawCircle(
                            color = monsterColor,
                            radius = minOf(cellW, cellH) * 0.38f,
                            center = Offset(ex, ey)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = minOf(cellW, cellH) * 0.15f,
                            center = Offset(ex, ey)
                        )
                    }
                }
            }

            // 3. Draw Player Party Token with Torchlight Glow
            val px = playerX * cellW + cellW / 2
            val py = playerY * cellH + cellH / 2

            // Dynamic torchlight aura
            val torchPixelRadius = torchRadius * cellW
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TorchOrange.copy(alpha = 0.3f),
                        GoldLight.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(px, py),
                    radius = torchPixelRadius
                ),
                radius = torchPixelRadius,
                center = Offset(px, py)
            )

            // Player Hero Token (Golden crest)
            drawCircle(
                color = GoldPrimary,
                radius = minOf(cellW, cellH) * 0.42f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xFF1E1B18),
                radius = minOf(cellW, cellH) * 0.32f,
                center = Offset(px, py)
            )
            drawCircle(
                color = ArcaneCyan,
                radius = minOf(cellW, cellH) * 0.18f,
                center = Offset(px, py)
            )
        }
    }
}
