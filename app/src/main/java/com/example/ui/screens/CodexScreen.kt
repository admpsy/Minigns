package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EnemyCategory
import com.example.ui.theme.*

data class CodexMonster(
    val name: String,
    val category: EnemyCategory,
    val dangerLevel: String,
    val description: String,
    val weakAgainst: String,
    val specialAttack: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodexScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    val monsters = listOf(
        CodexMonster(
            name = "Rei Esqueleto Eterno",
            category = EnemyCategory.BOSS,
            dangerLevel = "Chefe • Nível 4",
            description = "Monarca sepultado no grande sarcófago central. Empunha uma espada rúnica que absorve a alma dos aventureiros e ergue guardas das pilhas de crânios.",
            weakAgainst = "Magia Sagrada (Clérico), Dano Contundente",
            specialAttack = "Convocar Esqueletos & Presença Aterrorizante"
        ),
        CodexMonster(
            name = "Esqueleto Guerreiro",
            category = EnemyCategory.UNDEAD,
            dangerLevel = "Comum • Nível 1",
            description = "Ossada reanimada por energias necromânticas nas catacumbas. Ataca incansavelmente em corredores estreitos.",
            weakAgainst = "Radiância Divina, Esmagamento de Maça",
            specialAttack = "Cutilada Fúnebre"
        ),
        CodexMonster(
            name = "Aranha Gigante Bioluminescente",
            category = EnemyCategory.SUBTERRANEAN,
            dangerLevel = "Elite • Nível 8",
            description = "Habitante dos tetos e fendas de estalactites. Dispara teias pegajosas e inocula veneno paralisante na presa desavisada.",
            weakAgainst = "Fogo Mágico (Mago Bola de Fogo)",
            specialAttack = "Mordida Peçonhenta & Teia Paralisante"
        ),
        CodexMonster(
            name = "Sacerdote das Runas Malditas",
            category = EnemyCategory.CULTIST,
            dangerLevel = "Alto Risco • Nível 12",
            description = "Líder de seita profana que realiza rituais sangrentos nos altares subterrâneos para invocar entidades abissais.",
            weakAgainst = "Ataques Furtivos (Ladino), Silenciamento",
            specialAttack = "Invocação Demoníaca & Maldição de Sangue"
        ),
        CodexMonster(
            name = "Titã Golem de Pedra",
            category = EnemyCategory.BOSS,
            dangerLevel = "Lendário • Nível 16",
            description = "Guardião colossal construído pelos artífices das Ruínas Antigas. Sua carapaça de granito ricocheteia flechas comuns.",
            weakAgainst = "Corrente de Relâmpagos & Ataques no Núcleo de Força",
            specialAttack = "Pisão Sísmico & Esmagamento Devastador"
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Códice dos Horrores & Regras", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("codex_back_btn")) {
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = GoldLight
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bestiário", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mecânicas Táticas", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(monsters) { monster ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface,
                            border = BorderStroke(1.dp, if (monster.category == EnemyCategory.BOSS) GoldPrimary else DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(monster.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldLight)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (monster.category == EnemyCategory.BOSS) BloodCrimson.copy(alpha = 0.2f) else DarkSurfaceElevated
                                    ) {
                                        Text(monster.dangerLevel, fontSize = 9.sp, color = if (monster.category == EnemyCategory.BOSS) BloodCrimson else Color.LightGray, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(monster.description, fontSize = 10.sp, color = Color(0xFFCBD5E1), lineHeight = 14.sp)
                                Spacer(Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Fraqueza:", fontSize = 9.sp, color = ArcaneCyan, fontWeight = FontWeight.Bold)
                                        Text(monster.weakAgainst, fontSize = 9.sp, color = Color.White)
                                    }
                                    Column {
                                        Text("Habilidade Especial:", fontSize = 9.sp, color = BloodCrimson, fontWeight = FontWeight.Bold)
                                        Text(monster.specialAttack, fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        RuleCard(
                            title = "Sistema de Vantagem / Desvantagem",
                            desc = "Ao atacar a partir de furtividade (Ladino) ou contra um inimigo provocado pelo Guerreiro, rolam-se 2 dados D20 e mantém-se o maior! Quando com pouca vida ou desorientado, rola-se com desvantagem (mantém o menor)."
                        )
                    }
                    item {
                        RuleCard(
                            title = "Sinergias & Combos de Equipe",
                            desc = "Combine habilidades entre classes para dano massivo: \n• Fogo (Mago) + Veneno (Ranger/Ladino) = Conflagração Tóxica (+70% Dano)\n• Luz Sagrada (Clérico) + Alvo Congelado = Estilhaço Sagrado (+50% Dano)\n• Canção Heroica (Bardo) = Bônus em todas as rolagens do grupo."
                        )
                    }
                    item {
                        RuleCard(
                            title = "Tochas & Sanidade Mental",
                            desc = "Nas profundezas das catacumbas, as tochas se desgastam a cada 12 passos. Caminhar no breu absoluto drena a sanidade do grupo e atrai emboscadas mortais. Mantenha tochas acesas e use descansos com rações."
                        )
                    }
                    item {
                        RuleCard(
                            title = "Descanso Curto vs Longo",
                            desc = "Use o Descanso Curto consumindo 1 Ração de comida para restaurar 40% de HP e 30% de MP. Fontes sagradas nas masmorras purificam completamente todo o grupo."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, desc: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldLight)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 10.sp, color = Color(0xFFCBD5E1), lineHeight = 14.sp)
        }
    }
}
