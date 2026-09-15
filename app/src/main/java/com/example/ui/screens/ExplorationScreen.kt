package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.TileType
import com.example.ui.components.CombatLogView
import com.example.ui.components.IsometricDungeonCanvas
import com.example.ui.components.PartyResourceHeader
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationScreen(
    state: GameUiState,
    onMove: (Int, Int) -> Unit,
    onInteract: () -> Unit,
    onWaitTurn: () -> Unit,
    onUseTorch: () -> Unit,
    onRest: () -> Unit,
    onStartCombat: () -> Unit,
    onNavigate: (GameScreen) -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTile = state.dungeonMap.tiles.find {
        it.x == state.playerDungeonX && it.y == state.playerDungeonY
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(state.dungeonMap.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ArcaneCyan.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, ArcaneCyan)
                            ) {
                                Text(
                                    "Turno #${state.dungeonTurn}",
                                    fontSize = 10.sp,
                                    color = ArcaneCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Pos: (${state.playerDungeonX}, ${state.playerDungeonY}) • Tocha: ${"%.1f".format(state.torchLightRadius)}m • Patrulhas no mapa: ${state.dungeonPatrols.count { it.isAlive }}",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(GameScreen.HOME) }, modifier = Modifier.testTag("explore_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = GoldLight)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(GameScreen.PARTY_MANAGEMENT) }, modifier = Modifier.testTag("explore_party_btn")) {
                        Icon(Icons.Default.Group, contentDescription = "Grupo", tint = ArcaneCyan)
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
            // Resource Header
            PartyResourceHeader(
                resources = state.resources,
                onRestClick = onRest,
                onTorchClick = onUseTorch,
                onPartyClick = { onNavigate(GameScreen.PARTY_MANAGEMENT) }
            )

            Spacer(Modifier.height(4.dp))

            // Notification / Banner message if present
            if (state.activeBannerMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = GoldSecondary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, GoldPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(state.activeBannerMessage, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = onDismissBanner, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Isometric Dungeon Tabletop Grid Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                IsometricDungeonCanvas(
                    map = state.dungeonMap,
                    playerX = state.playerDungeonX,
                    playerY = state.playerDungeonY,
                    enemies = state.dungeonPatrols,
                    torchRadius = state.torchLightRadius,
                    modifier = Modifier.fillMaxSize()
                )

                // Current tile overlay tag in bottom left of the map
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, DarkBorder)
                ) {
                    val tileDesc = when (currentTile?.type) {
                        TileType.CHEST -> "Baú de Tesouro (Pressione Interagir)"
                        TileType.SARCOPHAGUS -> "Sarcófago de Pedra (Inspecionar)"
                        TileType.ALTAR -> "Altar Ancestral Sagrado (Orar)"
                        TileType.FOUNTAIN -> "Fonte de Águas Luminescentes"
                        TileType.TRAP_SPIKE -> "Armadilha de Espinho (Cuidado!)"
                        TileType.TRAP_POISON -> "Vapor Tóxico Subterrâneo"
                        TileType.EXIT_STAIRS -> "Escadaria para a Câmara do Chefe"
                        TileType.TORCH_STAND -> "Suporte de Tochas na Parede"
                        else -> "Piso de Pedra Antiga • Turno por Ação"
                    }
                    Text(
                        text = tileDesc,
                        fontSize = 10.sp,
                        color = GoldLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Interactive Controls Row & DPAD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Directional DPAD Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    // UP (North)
                    IconButton(
                        onClick = { onMove(0, -1) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkSurfaceElevated, CircleShape)
                            .testTag("dpad_up")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Norte", tint = GoldLight)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // LEFT (West)
                        IconButton(
                            onClick = { onMove(-1, 0) },
                            modifier = Modifier
                                .size(42.dp)
                                .background(DarkSurfaceElevated, CircleShape)
                                .testTag("dpad_left")
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Oeste", tint = GoldLight)
                        }

                        // CENTER (Interact)
                        val isInteractable = currentTile?.type?.isInteractive == true
                        IconButton(
                            onClick = onInteract,
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (isInteractable) GoldPrimary else DarkSurface, CircleShape)
                                .testTag("dpad_interact")
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = "Interagir",
                                tint = if (isInteractable) Color.Black else GoldLight
                            )
                        }

                        // RIGHT (East)
                        IconButton(
                            onClick = { onMove(1, 0) },
                            modifier = Modifier
                                .size(42.dp)
                                .background(DarkSurfaceElevated, CircleShape)
                                .testTag("dpad_right")
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Leste", tint = GoldLight)
                        }
                    }

                    // DOWN (South)
                    IconButton(
                        onClick = { onMove(0, 1) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkSurfaceElevated, CircleShape)
                            .testTag("dpad_down")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Sul", tint = GoldLight)
                    }
                }

                // Exploration Actions Panel
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Wait Turn Button (Turn-based exploration mechanic)
                    OutlinedButton(
                        onClick = onWaitTurn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("explore_wait_turn_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ArcaneCyan.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcaneCyan)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aguardar 1 Turno (Vigília)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Start Battle / Attack button
                    Button(
                        onClick = onStartCombat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("explore_fight_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Combate Tático", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Short Rest Button
                    OutlinedButton(
                        onClick = onRest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("explore_rest_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PoisonGreen.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PoisonGreen)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Descanso (-1 Ração)", fontSize = 10.sp)
                    }

                    // Use Torch Button
                    OutlinedButton(
                        onClick = onUseTorch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("explore_torch_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TorchOrange.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TorchOrange)
                    ) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tocha (${state.resources.torches})", fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Mini Combat Log
            CombatLogView(
                logs = state.combatLogs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
