@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EnvironmentTheme
import com.example.ui.components.PartyResourceHeader
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigate: (GameScreen) -> Unit,
    onSelectScenario: (EnvironmentTheme) -> Unit,
    onUseTorch: () -> Unit,
    onRest: () -> Unit,
    onToggleSound: () -> Unit = {},
    onToggleHaptics: () -> Unit = {},
    onDismissBanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
    ) {
        // Hero Visual Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_dungeon_catacombs),
                contentDescription = "Crypts & Dungeons Arte",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay for smooth dark integration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DarkBg.copy(alpha = 0.6f),
                                DarkBg
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GoldSecondary.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "RPG TÁTICO DE MESA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Crypts & Dungeons",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldLight,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Exploração de Masmorras • Combate por Turnos em Grid",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        // Top Resources bar
        PartyResourceHeader(
            resources = state.resources,
            onRestClick = onRest,
            onTorchClick = onUseTorch,
            onPartyClick = { onNavigate(GameScreen.PARTY_MANAGEMENT) }
        )

        // Aviso contextual (ex.: masmorra conquistada)
        if (state.activeBannerMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = GoldSecondary.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, GoldPrimary),
                onClick = onDismissBanner
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldLight, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(state.activeBannerMessage, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Close, contentDescription = "Fechar aviso", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Preferências rápidas: som e vibração
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.soundEnabled,
                onClick = onToggleSound,
                label = { Text(if (state.soundEnabled) "Som ligado" else "Som desligado", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        if (state.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("toggle_sound")
            )
            FilterChip(
                selected = state.hapticsEnabled,
                onClick = onToggleHaptics,
                label = { Text(if (state.hapticsEnabled) "Vibração ligada" else "Vibração desligada", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        if (state.hapticsEnabled) Icons.Default.Vibration else Icons.Default.PhonelinkErase,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("toggle_haptics")
            )
        }

        Spacer(Modifier.height(8.dp))

        // Main Navigation Hub
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Play Campaign Button (Primary Action)
            Button(
                onClick = { onNavigate(GameScreen.SCENARIO_SELECT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_campaign_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color(0xFF181105)
                )
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("CAMPANHA & CENÁRIOS", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Explore as 6 Grandes Criptas e Masmorras", fontSize = 11.sp, color = Color(0xFF382305))
                }
            }

            // Quick Continue Button if already inside a dungeon
            OutlinedButton(
                onClick = { onNavigate(GameScreen.EXPLORATION) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("quick_explore_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ArcaneCyan.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ArcaneCyan
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Entrar na Masmorra Atual (${state.selectedTheme.title})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dungeon Master Mode Button
                MainMenuCard(
                    title = "Dungeon Master",
                    subtitle = "Criador de Mapas & Monstros",
                    icon = Icons.Default.EditLocationAlt,
                    accentColor = ArcanePurple,
                    onClick = { onNavigate(GameScreen.DUNGEON_MASTER) },
                    modifier = Modifier.weight(1f).testTag("dm_mode_button")
                )

                // Party Management Button
                MainMenuCard(
                    title = "Grupo & Itens",
                    subtitle = "Guerreiro, Mago, Clérigo...",
                    icon = Icons.Default.Shield,
                    accentColor = PoisonGreen,
                    onClick = { onNavigate(GameScreen.PARTY_MANAGEMENT) },
                    modifier = Modifier.weight(1f).testTag("party_management_button")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Codex & Bestiary
                MainMenuCard(
                    title = "Códice & Bestiário",
                    subtitle = "Criaturas, Chefes & Lore",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = GoldLight,
                    onClick = { onNavigate(GameScreen.CODEX) },
                    modifier = Modifier.weight(1f).testTag("codex_button")
                )

                // Quick Battle Test
                MainMenuCard(
                    title = "Combate Rápido",
                    subtitle = "Teste tático em grid",
                    icon = Icons.Default.SportsKabaddi,
                    accentColor = BloodCrimson,
                    onClick = { onNavigate(GameScreen.COMBAT) },
                    modifier = Modifier.weight(1f).testTag("quick_combat_button")
                )
            }

            Spacer(Modifier.height(8.dp))

            // Active Party Summary Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grupo de Aventureiros Ativo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldLight)
                        Text("${state.party.count { it.isAlive }}/4 Vivos", fontSize = 11.sp, color = PoisonGreen)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.party.forEach { hero ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated,
                                border = BorderStroke(1.dp, if (hero.isAlive) DarkBorder else BloodCrimson)
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        hero.name.split(" ").first(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hero.isAlive) Color.White else Color.Gray,
                                        maxLines = 1
                                    )
                                    Text(
                                        hero.heroClass.displayName,
                                        fontSize = 9.sp,
                                        color = when (hero.heroClass) {
                                            com.example.model.HeroClass.WARRIOR -> ClassWarrior
                                            com.example.model.HeroClass.MAGE -> ClassMage
                                            com.example.model.HeroClass.CLERIC -> ClassCleric
                                            com.example.model.HeroClass.ROGUE -> ClassRogue
                                            com.example.model.HeroClass.RANGER -> ClassRanger
                                            com.example.model.HeroClass.BARD -> ClassBard
                                        }
                                    )
                                    Text(
                                        "HP ${hero.hp}",
                                        fontSize = 9.sp,
                                        color = if (hero.hp < 30) BloodCrimson else PoisonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MainMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}
