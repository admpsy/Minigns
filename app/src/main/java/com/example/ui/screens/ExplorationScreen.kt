package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TileType
import com.example.ui.components.CombatLogView
import com.example.ui.components.IsometricDungeonCanvas
import com.example.ui.components.PartyResourceHeader
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState
import kotlin.math.abs

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
    val map = state.dungeonMap
    val px = state.playerDungeonX
    val py = state.playerDungeonY
    val currentTile = map.tiles.getOrNull(py * map.width + px)?.takeIf { it.x == px && it.y == py }
        ?: map.tiles.find { it.x == px && it.y == py }
    val isInteractable = currentTile?.type?.isInteractive == true && currentTile.lootClaimed.not()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                map.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
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
                            "Tocha ${"%.1f".format(state.torchLightRadius)}m • Patrulhas: ${state.dungeonPatrols.count { it.isAlive }}",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(GameScreen.HOME) }, modifier = Modifier.testTag("explore_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar ao menu", tint = GoldLight)
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
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            PartyResourceHeader(
                resources = state.resources,
                onRestClick = onRest,
                onTorchClick = onUseTorch,
                onPartyClick = { onNavigate(GameScreen.PARTY_MANAGEMENT) }
            )

            if (state.activeBannerMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = GoldSecondary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, GoldPrimary),
                    onClick = onDismissBanner
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            state.activeBannerMessage,
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Close, contentDescription = "Fechar aviso", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Mapa: deslize para andar, toque num quadrado para ir até ele, toque no grupo para interagir.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                IsometricDungeonCanvas(
                    map = map,
                    playerX = px,
                    playerY = py,
                    enemies = state.dungeonPatrols,
                    torchRadius = state.torchLightRadius,
                    onTileClick = { tx, ty ->
                        val dx = tx - px
                        val dy = ty - py
                        when {
                            dx == 0 && dy == 0 -> onInteract()
                            abs(dx) >= abs(dy) -> onMove(if (dx > 0) 1 else -1, 0)
                            else -> onMove(0, if (dy > 0) 1 else -1)
                        }
                    },
                    onSwipe = { dx, dy -> onMove(dx, dy) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("dungeon_map")
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, DarkBorder)
                ) {
                    val used = currentTile?.lootClaimed == true && currentTile.type.isInteractive
                    val tileDesc = when {
                        used -> "Já utilizado"
                        else -> when (currentTile?.type) {
                            TileType.CHEST -> "Baú de Tesouro • toque em Interagir"
                            TileType.SARCOPHAGUS -> "Sarcófago de Pedra • Inspecionar"
                            TileType.ALTAR -> "Altar Ancestral Sagrado • Orar"
                            TileType.FOUNTAIN -> "Fonte de Águas Luminescentes"
                            TileType.TRAP_SPIKE -> "Armadilha de Espinho (Cuidado!)"
                            TileType.TRAP_POISON -> "Vapor Tóxico Subterrâneo"
                            TileType.EXIT_STAIRS -> "Escadaria para a Câmara do Chefe"
                            TileType.TORCH_STAND -> "Suporte de Tochas na Parede"
                            else -> "Deslize ou toque no mapa para andar"
                        }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // D-pad com alvos de toque de 52dp (acima do mínimo de 48dp do Android)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DpadButton(Icons.Default.KeyboardArrowUp, "Mover para o norte", "dpad_up") { onMove(0, -1) }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DpadButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mover para o oeste", "dpad_left") { onMove(-1, 0) }
                        DpadButton(
                            icon = Icons.Default.TouchApp,
                            description = "Interagir",
                            tag = "dpad_interact",
                            background = if (isInteractable) GoldPrimary else DarkSurface,
                            tint = if (isInteractable) Color.Black else GoldLight,
                            onClick = onInteract
                        )
                        DpadButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Mover para o leste", "dpad_right") { onMove(1, 0) }
                    }
                    DpadButton(Icons.Default.KeyboardArrowDown, "Mover para o sul", "dpad_down") { onMove(0, 1) }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onWaitTurn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("explore_wait_turn_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        border = BorderStroke(1.dp, ArcaneCyan.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcaneCyan)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aguardar 1 Turno", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = onStartCombat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("explore_fight_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Combate Tático", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onRest,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("explore_rest_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            border = BorderStroke(1.dp, PoisonGreen.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PoisonGreen)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Descanso (${state.resources.rations})", fontSize = 11.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onUseTorch,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("explore_torch_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            border = BorderStroke(1.dp, TorchOrange.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TorchOrange)
                        ) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tocha (${state.resources.torches})", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            CombatLogView(
                logs = state.combatLogs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DpadButton(
    icon: ImageVector,
    description: String,
    tag: String,
    background: Color = DarkSurfaceElevated,
    tint: Color = GoldLight,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .padding(2.dp)
            .background(background, CircleShape)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(28.dp))
    }
}
