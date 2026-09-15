package com.example.data

import com.example.model.*
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sqrt

object DungeonGenerator {

    fun generateMapForTheme(theme: EnvironmentTheme, width: Int = 18, height: Int = 14): DungeonMap {
        val tiles = mutableListOf<DungeonTile>()
        val grid = Array(height) { Array(width) { TileType.WALL } }

        // Create standard dungeon structure with rooms & corridors
        val rooms = listOf(
            RoomRect(1, 1, 5, 5),   // Spawn room
            RoomRect(8, 1, 8, 4),   // North burial chamber / ossuary
            RoomRect(2, 8, 5, 4),   // South crypt chamber
            RoomRect(9, 7, 7, 5)    // Boss chamber / Great hall
        )

        // Carve rooms
        for (room in rooms) {
            for (y in room.y until room.y + room.height) {
                for (x in room.x until room.x + room.width) {
                    if (y in 0 until height && x in 0 until width) {
                        grid[y][x] = TileType.FLOOR
                    }
                }
            }
        }

        // Carve corridors
        // Room 0 to Room 1 (Horizontal corridor at y=3)
        for (x in 5..8) grid[3][x] = TileType.FLOOR
        // Room 0 to Room 2 (Vertical corridor at x=3)
        for (y in 5..8) grid[y][3] = TileType.FLOOR
        // Room 1 to Room 3 (Vertical corridor at x=12)
        for (y in 4..7) grid[y][12] = TileType.FLOOR
        // Room 2 to Room 3 (Horizontal corridor at y=9)
        for (x in 6..9) grid[9][x] = TileType.FLOOR

        // Place theme-specific interactive props & hazards
        when (theme) {
            EnvironmentTheme.FORGOTTEN_CATACOMBS -> {
                grid[2][10] = TileType.SARCOPHAGUS
                grid[2][14] = TileType.SARCOPHAGUS
                grid[2][12] = TileType.CHEST
                grid[9][4] = TileType.ALTAR
                grid[3][7] = TileType.TRAP_SPIKE
                grid[9][8] = TileType.TRAP_POISON
                grid[10][14] = TileType.EXIT_STAIRS
                grid[1][1] = TileType.TORCH_STAND
                grid[8][1] = TileType.TORCH_STAND
                grid[7][9] = TileType.TORCH_STAND
                grid[5][3] = TileType.SECRET_DOOR
            }
            EnvironmentTheme.ROYAL_CRYPT -> {
                grid[2][11] = TileType.SARCOPHAGUS
                grid[2][13] = TileType.SARCOPHAGUS
                grid[3][12] = TileType.ALTAR
                grid[9][3] = TileType.FOUNTAIN
                grid[10][13] = TileType.CHEST
                grid[10][14] = TileType.EXIT_STAIRS
                grid[3][6] = TileType.TRAP_SPIKE
                grid[9][7] = TileType.TRAP_SPIKE
                grid[1][4] = TileType.TORCH_STAND
                grid[7][15] = TileType.TORCH_STAND
            }
            EnvironmentTheme.ABANDONED_DUNGEON -> {
                grid[3][6] = TileType.DOOR
                grid[6][3] = TileType.DOOR
                grid[9][7] = TileType.DOOR
                grid[2][12] = TileType.RUBBLE
                grid[10][11] = TileType.CHEST
                grid[9][4] = TileType.FISSURE
                grid[10][14] = TileType.EXIT_STAIRS
                grid[2][3] = TileType.TORCH_STAND
                grid[8][10] = TileType.TORCH_STAND
            }
            EnvironmentTheme.NATURAL_CAVERNS -> {
                grid[2][12] = TileType.FOUNTAIN // Luminescent pool
                grid[3][10] = TileType.TOXIC_GAS
                grid[9][4] = TileType.TOXIC_GAS
                grid[10][12] = TileType.CHEST
                grid[9][8] = TileType.FISSURE
                grid[10][14] = TileType.EXIT_STAIRS
            }
            EnvironmentTheme.SUBTERRANEAN_TEMPLE -> {
                grid[2][12] = TileType.ALTAR
                grid[9][12] = TileType.ALTAR
                grid[9][3] = TileType.FOUNTAIN
                grid[2][14] = TileType.CHEST
                grid[10][14] = TileType.EXIT_STAIRS
                grid[3][7] = TileType.SECRET_DOOR
            }
            EnvironmentTheme.ANCIENT_RUINS -> {
                grid[2][10] = TileType.RUBBLE
                grid[3][14] = TileType.CHEST
                grid[9][3] = TileType.ALTAR
                grid[8][12] = TileType.RUBBLE
                grid[10][14] = TileType.EXIT_STAIRS
                grid[3][6] = TileType.TRAP_SPIKE
            }
        }

        // Build DungeonTile objects with initial visibility
        for (y in 0 until height) {
            for (x in 0 until width) {
                val tileType = grid[y][x]
                // Initial exploration: spawn area around (2,2)
                val distFromSpawn = calculateDistance(x, y, 2, 2)
                val isVisible = distFromSpawn <= 3.5f
                val isExplored = isVisible

                tiles.add(
                    DungeonTile(
                        x = x,
                        y = y,
                        type = tileType,
                        isExplored = isExplored,
                        isVisible = isVisible,
                        hasTorchLit = (tileType == TileType.TORCH_STAND)
                    )
                )
            }
        }

        val loreNotes = listOf(
            "\"Aqui jazem aqueles que desafiaram a escuridão antes de nós. Que a chama nunca se apague.\"",
            "\"As runas na parede pulsavam com o sangue dos antigos reis...\"",
            "\"Cuidado com as fendas onde o gás venenoso e os vermes de pedra espreitam.\""
        )

        return DungeonMap(
            id = UUID.randomUUID().toString(),
            theme = theme,
            width = width,
            height = height,
            tiles = tiles,
            name = theme.title,
            loreInscriptions = loreNotes
        )
    }

    fun updateFogOfWar(
        map: DungeonMap,
        playerX: Int,
        playerY: Int,
        torchRadius: Float = 3.5f
    ): DungeonMap {
        val updatedTiles = map.tiles.map { tile ->
            val dist = calculateDistance(tile.x, tile.y, playerX, playerY)
            val isVisible = dist <= torchRadius
            val isExplored = tile.isExplored || isVisible
            tile.copy(isVisible = isVisible, isExplored = isExplored)
        }
        return map.copy(tiles = updatedTiles)
    }

    fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        val dx = (x1 - x2).toFloat()
        val dy = (y1 - y2).toFloat()
        return sqrt(dx * dx + dy * dy)
    }

    fun getManhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return abs(x1 - x2) + abs(y1 - y2)
    }

    private data class RoomRect(val x: Int, val y: Int, val width: Int, val height: Int)
}
