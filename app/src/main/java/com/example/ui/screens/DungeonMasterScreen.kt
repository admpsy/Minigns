package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EnvironmentTheme
import com.example.model.TileType
import com.example.ui.components.IsometricDungeonCanvas
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonMasterScreen(
    state: GameUiState,
    onSelectBrush: (TileType) -> Unit,
    onSelectTheme: (EnvironmentTheme) -> Unit,
    onPaintTile: (Int, Int) -> Unit,
    onPlayCustomMap: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dungeon Master (Criador)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        Text("Pinte salas, armadilhas, baús e monstros", fontSize = 11.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dm_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = GoldLight)
                    }
                },
                actions = {
                    Button(
                        onClick = onPlayCustomMap,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(34.dp)
                            .testTag("dm_play_map_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Jogar Masmorra", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // Theme selection row
            Text("Tema do Ambiente:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldLight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EnvironmentTheme.values().forEach { theme ->
                    val isSelected = state.dmSelectedTheme == theme
                    val themeColor = Color(theme.primaryColorHex)

                    Surface(
                        modifier = Modifier
                            .clickable { onSelectTheme(theme) }
                            .testTag("dm_theme_${theme.name.lowercase()}"),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) themeColor.copy(alpha = 0.3f) else DarkSurface,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) themeColor else DarkBorder)
                    ) {
                        Text(
                            text = theme.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GoldLight else Color.LightGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Brush Palette
            Text("Pincel de Elementos (Toque no mapa para posicionar):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldLight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair(TileType.WALL, "Parede"),
                    Pair(TileType.FLOOR, "Piso"),
                    Pair(TileType.SARCOPHAGUS, "Sarcófago"),
                    Pair(TileType.CHEST, "Baú"),
                    Pair(TileType.ALTAR, "Altar"),
                    Pair(TileType.TRAP_SPIKE, "Espinhos"),
                    Pair(TileType.TRAP_POISON, "Veneno"),
                    Pair(TileType.FOUNTAIN, "Fonte"),
                    Pair(TileType.TORCH_STAND, "Tocha"),
                    Pair(TileType.EXIT_STAIRS, "Saída")
                ).forEach { (tileType, label) ->
                    val isSelected = state.dmSelectedBrush == tileType

                    Surface(
                        modifier = Modifier
                            .clickable { onSelectBrush(tileType) }
                            .testTag("brush_${tileType.name.lowercase()}"),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) GoldLight else DarkBorder)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Interactive Drawing Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            ) {
                IsometricDungeonCanvas(
                    map = state.dmCustomMap,
                    playerX = 2,
                    playerY = 2,
                    torchRadius = 20f, // Full light in DM mode
                    onTileClick = onPaintTile,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated
            ) {
                Text(
                    text = "Dica do Mestre: Pinte paredes para desenhar corredores, espalhe baús e altares para recompensar aventureiros, e defina a saída com escadarias.",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
