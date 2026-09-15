package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalCombatScreen(
    state: GameUiState,
    onSelectHero: (Int) -> Unit,
    onSelectEnemy: (Int) -> Unit,
    onSelectSkill: (Skill) -> Unit,
    onExecuteAction: () -> Unit,
    onGuard: () -> Unit,
    onShove: () -> Unit,
    onMoveHero: (Int, Int) -> Unit,
    onItemPotion: () -> Unit,
    onItemFire: () -> Unit,
    onItemSmoke: () -> Unit,
    onDismissDiceRoll: () -> Unit,
    onEndTurn: () -> Unit,
    onNavigate: (GameScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeHero = state.party.getOrNull(state.activeHeroIndex)
    val selectedEnemy = state.selectedEnemyIndex?.let { state.combatEnemies.getOrNull(it) }
    val selectedSkill = state.selectedSkill ?: activeHero?.skills?.firstOrNull()
    val scrollState = rememberScrollState()

    // D20 Modal Overlay
    if (state.activeDiceRoll != null) {
        D20RollDialog(
            roll = state.activeDiceRoll,
            onDismiss = onDismissDiceRoll
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Combate Tático 3D Grid", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        Text(
                            if (state.isEnemyTurnProcessing) "Turno dos Inimigos (Calculando Intenções)..." else "Turno do Jogador: ${activeHero?.name ?: ""}",
                            fontSize = 11.sp,
                            color = if (state.isEnemyTurnProcessing) BloodCrimson else PoisonGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(GameScreen.EXPLORATION) }, modifier = Modifier.testTag("combat_retreat_btn")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Recuar", tint = Color.LightGray)
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Initiative Tracker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GoldSecondary.copy(alpha = 0.25f),
                        border = BorderStroke(0.5.dp, GoldPrimary)
                    ) {
                        Text(
                            "R${state.combatRound}",
                            fontSize = 10.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    state.initiativeQueue.forEachIndexed { idx, entity ->
                        val isCurrentTurn = idx == state.currentTurnIndex
                        val isAlive = entity.isAlive

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                !isAlive -> Color(0xFF1E1E22).copy(alpha = 0.5f)
                                isCurrentTurn -> GoldPrimary.copy(alpha = 0.25f)
                                entity.isHero -> DarkSurfaceElevated
                                else -> Color(0xFF3B1E22)
                            },
                            border = BorderStroke(
                                if (isCurrentTurn) 1.5.dp else 0.5.dp,
                                when {
                                    !isAlive -> Color.DarkGray
                                    isCurrentTurn -> GoldPrimary
                                    entity.isHero -> ArcaneCyan.copy(alpha = 0.6f)
                                    else -> BloodCrimson.copy(alpha = 0.6f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCurrentTurn) {
                                    Text("▶ ", fontSize = 9.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                !isAlive -> Color.Gray
                                                entity.isHero -> ArcaneCyan
                                                else -> BloodCrimson
                                            }
                                        )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    entity.name.split(" ").first(),
                                    fontSize = 10.sp,
                                    color = if (!isAlive) Color.Gray else if (isCurrentTurn) GoldLight else Color.White,
                                    fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    if (!isAlive) "☠" else "(${entity.speed})",
                                    fontSize = 9.sp,
                                    color = if (!isAlive) Color.Red else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tactical Arena Grid (Interactive 6x4 battlefield with cover & traps)
            TacticalArenaGrid(
                tiles = state.combatGridArena,
                party = state.party,
                enemies = state.combatEnemies,
                activeHero = activeHero,
                selectedEnemy = selectedEnemy,
                onTileClick = { x, y -> onMoveHero(x, y) },
                onHeroClick = { hero ->
                    val idx = state.party.indexOfFirst { it.id == hero.id }
                    if (idx != -1) onSelectHero(idx)
                },
                onEnemyClick = { enemy ->
                    val idx = state.combatEnemies.indexOfFirst { it.id == enemy.id }
                    if (idx != -1) onSelectEnemy(idx)
                }
            )

            // 3. Enemies Section (Selectable targets with Telegraphed Intentions)
            Text("Inimigos na Sala (Intenções Telegrafadas):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BloodCrimson)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                state.combatEnemies.forEachIndexed { index, enemy ->
                    EnemyCombatCard(
                        enemy = enemy,
                        isSelected = state.selectedEnemyIndex == index,
                        onClick = { onSelectEnemy(index) },
                        modifier = Modifier.testTag("enemy_card_$index")
                    )
                }
            }

            // 4. Player Party Status Deck
            Text("Grupo de Heróis:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(state.party) { index, hero ->
                    HeroCombatCard(
                        hero = hero,
                        isSelected = state.activeHeroIndex == index,
                        onClick = { onSelectHero(index) },
                        modifier = Modifier.testTag("hero_card_$index")
                    )
                }
            }

            // 5. Tactical Action & Skill Deck for Active Hero
            if (activeHero != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ações de ${activeHero.name}:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GoldLight
                            )
                            Text(
                                "AP: ${activeHero.ap}/${activeHero.maxAp} • MP: ${activeHero.mp}/${activeHero.maxMp}",
                                fontSize = 11.sp,
                                color = ArcaneCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Skills Selection Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeHero.skills.forEach { skill ->
                                val isChosen = selectedSkill?.id == skill.id
                                val hasEnoughMp = activeHero.mp >= skill.manaCost

                                Surface(
                                    modifier = Modifier
                                        .clickable(enabled = hasEnoughMp && activeHero.ap >= skill.apCost) {
                                            onSelectSkill(skill)
                                        }
                                        .testTag("skill_${skill.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChosen) GoldSecondary.copy(alpha = 0.3f) else DarkSurfaceElevated,
                                    border = BorderStroke(
                                        if (isChosen) 2.dp else 1.dp,
                                        if (isChosen) GoldPrimary else if (!hasEnoughMp) Color.DarkGray else DarkBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = skill.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (hasEnoughMp) Color.White else Color.Gray
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Poder: ${skill.power}", fontSize = 9.sp, color = GoldLight)
                                            if (skill.manaCost > 0) {
                                                Text("${skill.manaCost} MP", fontSize = 9.sp, color = ArcaneCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Selected Skill Details & Team Synergy hints
                        if (selectedSkill != null) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(selectedSkill.description, fontSize = 10.sp, color = Color.LightGray)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Tipo: ${selectedSkill.damageType.name} • Alcance: ${selectedSkill.range} quadros • Alvo: ${selectedSkill.targetType.name}",
                                        fontSize = 9.sp,
                                        color = GoldSecondary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Full Tactical Actions Bar (Attack, Guard, Shove, Quick Items, Pass)
                        TacticalActionsBar(
                            activeHero = activeHero,
                            onAttackClick = onExecuteAction,
                            onGuardClick = onGuard,
                            onShoveClick = onShove,
                            onItemPotionClick = onItemPotion,
                            onItemFireClick = onItemFire,
                            onItemSmokeClick = onItemSmoke,
                            onPassTurnClick = onEndTurn,
                            healingPotionsCount = state.resources.healingPotions
                        )
                    }
                }
            }

            // 6. Combat Event Log
            Text("Registro de Combate (Rolagens D20, Terreno & Sinergias):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldLight)
            CombatLogView(
                logs = state.combatLogs,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
