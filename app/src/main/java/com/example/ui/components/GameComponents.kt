package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun PartyResourceHeader(
    resources: PartyResources,
    onRestClick: () -> Unit,
    onTorchClick: () -> Unit,
    onPartyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gold
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonetizationOn, contentDescription = "Ouro", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("${resources.gold}", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Torches
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onTorchClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Whatshot, contentDescription = "Tochas", tint = TorchOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(3.dp))
                Text("${resources.torches}", color = TorchOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Rations / Food
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onRestClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = "Rações", tint = PoisonGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(3.dp))
                Text("${resources.rations}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            // Healing Potions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, contentDescription = "Poções", tint = BloodCrimson, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(3.dp))
                Text("${resources.healingPotions}", color = BloodCrimson, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Party & Gear Button
            FilledTonalButton(
                onClick = onPartyClick,
                modifier = Modifier
                    .height(32.dp)
                    .testTag("header_party_btn"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DarkBorder,
                    contentColor = GoldLight
                )
            ) {
                Icon(Icons.Default.Group, contentDescription = "Grupo", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Grupo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CombatLogView(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp, max = 130.dp),
        shape = RoundedCornerShape(10.dp),
        color = ParchmentBg,
        border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { entry ->
                val (color, icon) = when (entry.type) {
                    LogType.CRIT -> Pair(GoldLight, Icons.Default.Star)
                    LogType.COMBO -> Pair(ArcaneCyan, Icons.Default.AutoAwesome)
                    LogType.HEAL -> Pair(PoisonGreen, Icons.Default.Favorite)
                    LogType.STATUS -> Pair(ArcanePurple, Icons.Default.Warning)
                    LogType.LOOT -> Pair(GoldPrimary, Icons.Default.CardGiftcard)
                    LogType.ATTACK -> Pair(ParchmentText, Icons.Default.Shield)
                    LogType.SYSTEM -> Pair(Color(0xFF94A3B8), Icons.Default.Info)
                    LogType.SPELL -> Pair(ArcaneCyan, Icons.Default.Bolt)
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = entry.text,
                        color = color,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = if (entry.type == LogType.CRIT || entry.type == LogType.COMBO) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCombatCard(
    hero: Hero,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) GoldPrimary else if (!hero.isAlive) BloodCrimson else DarkBorder

    Surface(
        modifier = modifier
            .width(135.dp)
            .clickable(enabled = hero.isAlive) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) DarkSurfaceElevated else DarkSurface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hero.name.split(" ").first(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (hero.isAlive) Color.White else Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = "Nv.${hero.level}",
                    fontSize = 10.sp,
                    color = GoldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = hero.heroClass.displayName,
                fontSize = 10.sp,
                color = when (hero.heroClass) {
                    HeroClass.WARRIOR -> ClassWarrior
                    HeroClass.MAGE -> ClassMage
                    HeroClass.CLERIC -> ClassCleric
                    HeroClass.ROGUE -> ClassRogue
                    HeroClass.RANGER -> ClassRanger
                    HeroClass.BARD -> ClassBard
                }
            )

            Spacer(Modifier.height(4.dp))

            // HP Bar
            val hpRatio = (hero.hp.toFloat() / hero.maxHp.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HP", fontSize = 9.sp, color = Color.LightGray)
                Text("${hero.hp}/${hero.maxHp}", fontSize = 9.sp, color = Color.White)
            }
            LinearProgressIndicator(
                progress = { hpRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (hpRatio > 0.5f) PoisonGreen else if (hpRatio > 0.2f) TorchOrange else BloodCrimson,
                trackColor = Color(0xFF333333)
            )

            Spacer(Modifier.height(3.dp))

            // MP Bar
            val mpRatio = (hero.mp.toFloat() / hero.maxMp.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { mpRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ArcaneCyan,
                trackColor = Color(0xFF222233)
            )

            Spacer(Modifier.height(4.dp))

            // AP Action Points & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..hero.maxAp) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (i <= hero.ap) GoldLight else Color.DarkGray)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hero.isGuarding) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ArcaneCyan.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, ArcaneCyan)
                        ) {
                            Text("🛡 DEF", fontSize = 8.sp, color = ArcaneCyan, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("(${hero.combatGridX},${hero.combatGridY})", fontSize = 9.sp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun EnemyCombatCard(
    enemy: Enemy,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) BloodCrimson else if (enemy.isBoss) GoldPrimary else DarkBorder

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enemy.isAlive) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF2A1518) else DarkSurface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (enemy.isBoss) BloodCrimson else Color(0xFF3B1E22)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (enemy.isBoss) Icons.Default.Warning else Icons.Default.BugReport,
                        contentDescription = null,
                        tint = if (enemy.isBoss) GoldLight else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = enemy.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (enemy.isAlive) Color.White else Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("(${enemy.combatGridX},${enemy.combatGridY})", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "Nv.${enemy.level}",
                                fontSize = 11.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = enemy.title,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(3.dp))

                    val hpRatio = (enemy.hp.toFloat() / enemy.maxHp.toFloat()).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HP", fontSize = 9.sp, color = Color.Gray)
                        Text("${enemy.hp}/${enemy.maxHp}", fontSize = 9.sp, color = Color.LightGray)
                    }
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (enemy.isBoss) GoldPrimary else BloodCrimson,
                        trackColor = Color(0xFF331B1B)
                    )
                }
            }

            // Telegraphed Enemy Intent Banner
            if (enemy.isAlive && enemy.currentIntent != null) {
                val intent = enemy.currentIntent
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (intent.type) {
                        EnemyIntentType.HEAVY_SMASH -> BloodCrimson.copy(alpha = 0.25f)
                        EnemyIntentType.DEFENSIVE_GUARD -> ArcaneCyan.copy(alpha = 0.2f)
                        EnemyIntentType.CURSE -> PoisonGreen.copy(alpha = 0.2f)
                        EnemyIntentType.ARCANE_SPELL -> Color(0xFF6B21A8).copy(alpha = 0.25f)
                        else -> TorchOrange.copy(alpha = 0.2f)
                    },
                    border = BorderStroke(1.dp, when (intent.type) {
                        EnemyIntentType.HEAVY_SMASH -> BloodCrimson
                        EnemyIntentType.DEFENSIVE_GUARD -> ArcaneCyan
                        EnemyIntentType.CURSE -> PoisonGreen
                        EnemyIntentType.ARCANE_SPELL -> Color(0xFFA855F7)
                        else -> TorchOrange
                    }),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                when (intent.type) {
                                    EnemyIntentType.HEAVY_SMASH -> Icons.Default.LocalFireDepartment
                                    EnemyIntentType.DEFENSIVE_GUARD -> Icons.Default.Shield
                                    EnemyIntentType.CURSE -> Icons.Default.Coronavirus
                                    EnemyIntentType.ARCANE_SPELL -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.FlashOn
                                },
                                contentDescription = null,
                                tint = when (intent.type) {
                                    EnemyIntentType.HEAVY_SMASH -> BloodCrimson
                                    EnemyIntentType.DEFENSIVE_GUARD -> ArcaneCyan
                                    EnemyIntentType.CURSE -> PoisonGreen
                                    EnemyIntentType.ARCANE_SPELL -> Color(0xFFA855F7)
                                    else -> TorchOrange
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Intenção: ${intent.description}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                        if (intent.estimatedDamage > 0) {
                            Text(
                                text = "~${intent.estimatedDamage} Dano",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BloodCrimson
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalArenaGrid(
    tiles: List<TacticalTile>,
    party: List<Hero>,
    enemies: List<Enemy>,
    activeHero: Hero?,
    selectedEnemy: Enemy?,
    onTileClick: (Int, Int) -> Unit,
    onHeroClick: (Hero) -> Unit,
    onEnemyClick: (Enemy) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_arena_grid"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141418),
        border = BorderStroke(1.5.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Arena Header with Guide
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.GridOn, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                    Text(
                        "Arena Tática de Batalha (6x4)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E3D48)))
                        Text("Entulho (+DEF)", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF5A1E1E)))
                        Text("Espinhos", fontSize = 9.sp, color = Color.LightGray)
                    }
                }
            }

            // 4 rows x 6 columns
            for (y in 0 until 4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (x in 0 until 6) {
                        val tile = tiles.find { it.x == x && it.y == y }
                        val heroOnTile = party.find { it.isAlive && it.combatGridX == x && it.combatGridY == y }
                        val enemyOnTile = enemies.find { it.isAlive && it.combatGridX == x && it.combatGridY == y }

                        // Check if tile is a valid reachable move for activeHero
                        val isReachable = if (activeHero != null && heroOnTile == null && enemyOnTile == null) {
                            val dist = kotlin.math.abs(activeHero.combatGridX - x) + kotlin.math.abs(activeHero.combatGridY - y)
                            dist in 1..2 && (activeHero.ap > 0 || !activeHero.hasMovedThisTurn)
                        } else false

                        val tileBg = when {
                            tile?.isTrap == true -> Color(0xFF381515)
                            tile?.isCover == true -> Color(0xFF1E2830)
                            x <= 1 -> Color(0xFF1A1F26)
                            x >= 4 -> Color(0xFF261A1E)
                            else -> Color(0xFF1A1A1E)
                        }

                        val tileBorderColor = when {
                            isReachable -> ArcaneCyan
                            tile?.isTrap == true -> BloodCrimson.copy(alpha = 0.5f)
                            tile?.isCover == true -> ArcaneCyan.copy(alpha = 0.4f)
                            else -> Color(0xFF2B2B33)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(tileBg)
                                .border(
                                    width = if (isReachable) 1.5.dp else 1.dp,
                                    color = tileBorderColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    if (heroOnTile != null) {
                                        onHeroClick(heroOnTile)
                                    } else if (enemyOnTile != null) {
                                        onEnemyClick(enemyOnTile)
                                    } else if (isReachable) {
                                        onTileClick(x, y)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Environmental markers
                            if (tile?.isCover == true && heroOnTile == null && enemyOnTile == null) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Cobertura",
                                    tint = ArcaneCyan.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (tile?.isTrap == true && heroOnTile == null && enemyOnTile == null) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Armadilha",
                                    tint = BloodCrimson.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Movement indicator pulse
                            if (isReachable) {
                                Icon(
                                    Icons.Default.TouchApp,
                                    contentDescription = "Mover Aqui",
                                    tint = ArcaneCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Hero Token
                            if (heroOnTile != null) {
                                val isSelected = activeHero?.id == heroOnTile.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) GoldPrimary else Color(0xFF2A4365))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color.White else GoldLight,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = heroOnTile.name.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                        if (heroOnTile.isGuarding) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(ArcaneCyan)
                                            )
                                        }
                                    }
                                    val hpPercent = (heroOnTile.hp.toFloat() / heroOnTile.maxHp).coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.DarkGray)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(hpPercent)
                                                .background(PoisonGreen)
                                        )
                                    }
                                }
                            }

                            // Enemy Token
                            if (enemyOnTile != null) {
                                val isTarget = selectedEnemy?.id == enemyOnTile.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isTarget) BloodCrimson else Color(0xFF4A1E24))
                                            .border(
                                                width = if (isTarget) 2.dp else 1.dp,
                                                color = if (isTarget) Color.White else BloodCrimson,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (enemyOnTile.isBoss) Icons.Default.Warning else Icons.Default.BugReport,
                                            contentDescription = null,
                                            tint = if (isTarget) Color.White else Color(0xFFE2B2B2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        // Mini Intent Indicator
                                        if (enemyOnTile.currentIntent != null) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(when (enemyOnTile.currentIntent.type) {
                                                        EnemyIntentType.HEAVY_SMASH -> BloodCrimson
                                                        EnemyIntentType.DEFENSIVE_GUARD -> ArcaneCyan
                                                        EnemyIntentType.CURSE -> PoisonGreen
                                                        EnemyIntentType.ARCANE_SPELL -> Color(0xFFA855F7)
                                                        else -> TorchOrange
                                                    })
                                            )
                                        }
                                    }
                                    val enemyHpPercent = (enemyOnTile.hp.toFloat() / enemyOnTile.maxHp).coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.DarkGray)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(enemyHpPercent)
                                                .background(BloodCrimson)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (y < 3) Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun D20RollDialog(
    roll: ActiveDiceRoll,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Continuar Combate", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF16161C),
        tonalElevation = 8.dp,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = roll.actionName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Text(
                    text = "${roll.actorName} ➔ ${roll.targetName}",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large D20 Polygon Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                roll.isCrit -> Color(0xFF3B2E07)
                                roll.isMiss -> Color(0xFF3B1515)
                                else -> Color(0xFF1C2233)
                            }
                        )
                        .border(
                            width = 3.dp,
                            color = when {
                                roll.isCrit -> GoldPrimary
                                roll.isMiss -> BloodCrimson
                                else -> ArcaneCyan
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${roll.d20}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                roll.isCrit -> GoldLight
                                roll.isMiss -> BloodCrimson
                                else -> Color.White
                            }
                        )
                        Text(
                            text = "d20",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.LightGray
                        )
                    }
                }

                // Outcome Callout
                if (roll.isCrit) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Text(
                            "★ SUCESSO CRÍTICO D20! ★",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else if (roll.isMiss) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BloodCrimson.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, BloodCrimson)
                    ) {
                        Text(
                            "☠ FALHA CRÍTICA (ERROU!) ☠",
                            color = BloodCrimson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Breakdown Calculation
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rolagem d20 + Modificador", fontSize = 11.sp, color = Color.Gray)
                            Text("${roll.d20} + ${roll.modifier} = ${roll.total}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (roll.advantage) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vantagem Tática (Flanqueamento)", fontSize = 11.sp, color = PoisonGreen)
                                Text("Ativa", fontSize = 11.sp, color = PoisonGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (roll.comboName != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sinergia / Terreno", fontSize = 11.sp, color = ArcaneCyan)
                                Text(roll.comboName, fontSize = 11.sp, color = ArcaneCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (roll.damage > 0) {
                            Divider(color = DarkBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dano Total Aplicado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("-${roll.damage} Dano", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BloodCrimson)
                            }
                        }

                        Text(
                            text = roll.outcomeDescription,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TacticalActionsBar(
    activeHero: Hero?,
    onAttackClick: () -> Unit,
    onGuardClick: () -> Unit,
    onShoveClick: () -> Unit,
    onItemPotionClick: () -> Unit,
    onItemFireClick: () -> Unit,
    onItemSmokeClick: () -> Unit,
    onPassTurnClick: () -> Unit,
    healingPotionsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF16161D),
        border = BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Main Actions: Attack, Defend/Guard, Shove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onAttackClick,
                    enabled = activeHero != null && activeHero.ap > 0,
                    modifier = Modifier.weight(1.3f).height(44.dp).testTag("combat_attack_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Executar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGuardClick,
                    enabled = activeHero != null && activeHero.ap > 0 && !activeHero.isGuarding,
                    modifier = Modifier.weight(1f).height(44.dp).testTag("combat_guard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233B53)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Guarda", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShoveClick,
                    enabled = activeHero != null && activeHero.ap > 0,
                    modifier = Modifier.weight(1f).height(44.dp).testTag("combat_shove_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3820)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.OpenWith, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Empurrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Tactical Items Belt & End Turn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Potion
                OutlinedButton(
                    onClick = onItemPotionClick,
                    enabled = healingPotionsCount > 0 && activeHero != null && activeHero.ap > 0,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PoisonGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PoisonGreen)
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Cura ($healingPotionsCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Alchemist Fire
                OutlinedButton(
                    onClick = onItemFireClick,
                    enabled = activeHero != null && activeHero.ap > 0,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TorchOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TorchOrange)
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Fogo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Smoke Bomb
                OutlinedButton(
                    onClick = onItemSmokeClick,
                    enabled = activeHero != null && activeHero.ap > 0,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Fumaça", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Pass Turn
                OutlinedButton(
                    onClick = onPassTurnClick,
                    modifier = Modifier.weight(1f).height(38.dp).testTag("combat_end_turn_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Passar", fontSize = 10.sp)
                }
            }
        }
    }
}
