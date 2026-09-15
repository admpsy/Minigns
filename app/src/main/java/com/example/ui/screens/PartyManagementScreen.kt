package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyManagementScreen(
    state: GameUiState,
    onEquipItem: (Int, Item) -> Unit,
    onSocketGem: (Int, Item) -> Unit,
    onUsePotion: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHeroIndex by remember { mutableStateOf(0) }
    var selectedInventoryTab by remember { mutableStateOf(ItemType.WEAPON) }
    val currentHero = state.party.getOrNull(selectedHeroIndex) ?: state.party.first()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Gestão do Grupo & Inventário", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("party_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = GoldLight)
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
            // Hero Switcher Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.party.forEachIndexed { index, hero ->
                    val isSelected = selectedHeroIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedHeroIndex = index }
                            .testTag("select_hero_tab_$index"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) GoldSecondary.copy(alpha = 0.3f) else DarkSurface,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) GoldPrimary else DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = hero.name.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GoldLight else Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = hero.heroClass.displayName,
                                fontSize = 9.sp,
                                color = when (hero.heroClass) {
                                    HeroClass.WARRIOR -> ClassWarrior
                                    HeroClass.MAGE -> ClassMage
                                    HeroClass.CLERIC -> ClassCleric
                                    HeroClass.ROGUE -> ClassRogue
                                    HeroClass.RANGER -> ClassRanger
                                    HeroClass.BARD -> ClassBard
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Character Details & Equipped Gear
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(currentHero.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(currentHero.heroClass.title, fontSize = 11.sp, color = GoldSecondary)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = GoldSecondary.copy(alpha = 0.2f)) {
                            Text("Nível ${currentHero.level} • XP: ${currentHero.xp}/${currentHero.maxXp}", fontSize = 10.sp, color = GoldLight, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Stats Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatBadge("ATK", "${currentHero.totalAtk}", BloodCrimson)
                        StatBadge("DEF", "${currentHero.totalDef}", ArcaneCyan)
                        StatBadge("MAG", "${currentHero.totalMag}", ArcanePurple)
                        StatBadge("SPD", "${currentHero.totalSpd}", PoisonGreen)
                        StatBadge("CRIT", "${currentHero.totalCrit}%", GoldLight)
                        StatBadge("SAN", "${currentHero.sanity}%", Color(0xFFCBD5E1))
                    }

                    Spacer(Modifier.height(8.dp))

                    // Equipped Equipment Slots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GearSlot(
                            slotName = "Arma",
                            item = currentHero.equippedWeapon,
                            modifier = Modifier.weight(1f)
                        )
                        GearSlot(
                            slotName = "Armadura",
                            item = currentHero.equippedArmor,
                            modifier = Modifier.weight(1f)
                        )
                        GearSlot(
                            slotName = "Relíquia",
                            item = currentHero.equippedAccessory,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Potion Quick Heal
                    if (state.resources.healingPotions > 0 && currentHero.hp < currentHero.maxHp) {
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { onUsePotion(selectedHeroIndex) },
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, PoisonGreen)
                        ) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = PoisonGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Usar Poção de Cura (+60 HP)", fontSize = 10.sp, color = PoisonGreen)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Inventory / Stash Section
            Text("Baú do Grupo & Itens Disponíveis:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)

            // Category Tab Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(ItemType.WEAPON, "Armas"),
                    Pair(ItemType.ARMOR, "Armaduras"),
                    Pair(ItemType.ACCESSORY, "Acessórios"),
                    Pair(ItemType.GEM, "Gemas"),
                    Pair(ItemType.CONSUMABLE, "Poções")
                ).forEach { (type, label) ->
                    val isTabSelected = selectedInventoryTab == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedInventoryTab = type },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isTabSelected) GoldPrimary else DarkSurfaceElevated
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTabSelected) Color.Black else Color.LightGray,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            val filteredItems = state.inventory.filter { it.type == selectedInventoryTab }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DarkSurface, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum item nesta categoria no momento.", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems) { item ->
                        InventoryItemCard(
                            item = item,
                            onEquip = { onEquipItem(selectedHeroIndex, item) },
                            onSocket = { onSocketGem(selectedHeroIndex, item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(0.5.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color.Gray)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun GearSlot(slotName: String, item: Item?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, if (item != null) GoldSecondary.copy(alpha = 0.5f) else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(slotName, fontSize = 9.sp, color = GoldLight, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            if (item != null) {
                Text(item.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(
                    if (item.atkBonus > 0) "+${item.atkBonus} ATK" else if (item.defBonus > 0) "+${item.defBonus} DEF" else "+${item.magBonus} MAG",
                    fontSize = 9.sp,
                    color = PoisonGreen
                )
            } else {
                Text("Vazio", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: Item,
    onEquip: () -> Unit,
    onSocket: () -> Unit
) {
    val rarityColor = when (item.rarity) {
        Rarity.COMMON -> RarityCommon
        Rarity.UNCOMMON -> RarityUncommon
        Rarity.RARE -> RarityRare
        Rarity.EPIC -> RarityEpic
        Rarity.LEGENDARY -> RarityLegendary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = rarityColor)
                    Spacer(Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(3.dp), color = rarityColor.copy(alpha = 0.2f)) {
                        Text(item.rarity.displayName, fontSize = 8.sp, color = rarityColor, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Text(item.description, fontSize = 10.sp, color = Color.LightGray, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.atkBonus > 0) Text("+${item.atkBonus} ATK", fontSize = 9.sp, color = BloodCrimson)
                    if (item.defBonus > 0) Text("+${item.defBonus} DEF", fontSize = 9.sp, color = ArcaneCyan)
                    if (item.magBonus > 0) Text("+${item.magBonus} MAG", fontSize = 9.sp, color = ArcanePurple)
                    if (item.critBonus > 0) Text("+${item.critBonus}% CRIT", fontSize = 9.sp, color = GoldLight)
                }
            }

            if (item.type in listOf(ItemType.WEAPON, ItemType.ARMOR, ItemType.ACCESSORY)) {
                Button(
                    onClick = onEquip,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Equipar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else if (item.type == ItemType.GEM) {
                Button(
                    onClick = onSocket,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple, contentColor = Color.White)
                ) {
                    Text("Encrustar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
