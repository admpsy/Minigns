package com.example.model

enum class HeroClass(
    val displayName: String,
    val title: String,
    val role: String,
    val description: String,
    val baseHp: Int,
    val baseMp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseMag: Int,
    val baseSpd: Int
) {
    WARRIOR(
        displayName = "Guerreiro",
        title = "Guardião de Ferro",
        role = "Tanque & Controle",
        description = "Mestre da espada e escudo. Protege aliados, atrai a fúria inimiga e desfere golpes devastadores de curta distância.",
        baseHp = 120, baseMp = 25, baseAtk = 18, baseDef = 14, baseMag = 4, baseSpd = 9
    ),
    MAGE(
        displayName = "Mago",
        title = "Arcanista das Sombras",
        role = "Dano em Área & Gelo/Fogo",
        description = "Canaliza as correntes místicas subterrâneas para incinerar hordas com Bolas de Fogo e congelar adversários com Nevasca.",
        baseHp = 70, baseMp = 100, baseAtk = 6, baseDef = 5, baseMag = 22, baseSpd = 10
    ),
    CLERIC(
        displayName = "Clérico",
        title = "Sacerdote da Luz Radiante",
        role = "Cura & Dano Sagrado",
        description = "Empunha a bênção solar nas profundezas escuras. Purifica mortos-vivos, cura ferimentos e ergue escudos divinos.",
        baseHp = 95, baseMp = 80, baseAtk = 12, baseDef = 10, baseMag = 16, baseSpd = 8
    ),
    ROGUE(
        displayName = "Ladino",
        title = "Assassino das Sombras",
        role = "Furtividade & Críticos",
        description = "Move-se invisível pelas colunas caídas, desarma armadilhas antigas e desfere golpes fatais pelas costas.",
        baseHp = 80, baseMp = 40, baseAtk = 20, baseDef = 7, baseMag = 6, baseSpd = 15
    ),
    RANGER(
        displayName = "Ranger",
        title = "Patrulheiro das Cavernas",
        role = "Ataque à Distância & Armadilhas",
        description = "Especialista em arco e sobrevivência. Posiciona armadilhas de veneno, atira saraivadas e marca presas para execução.",
        baseHp = 85, baseMp = 45, baseAtk = 17, baseDef = 8, baseMag = 8, baseSpd = 13
    ),
    BARD(
        displayName = "Bardo",
        title = "Menestrel do Abismo",
        role = "Buffs, Debuffs & Velocidade",
        description = "Sua música mágica inspira coragem, eleva a agilidade do grupo, confunde criaturas e restaura a sanidade em momentos sombrios.",
        baseHp = 85, baseMp = 70, baseAtk = 10, baseDef = 8, baseMag = 14, baseSpd = 12
    )
}

enum class Rarity(val displayName: String, val multiplier: Float) {
    COMMON("Comum", 1.0f),
    UNCOMMON("Incomum", 1.3f),
    RARE("Raro", 1.7f),
    EPIC("Épico", 2.3f),
    LEGENDARY("Lendário", 3.2f)
}

enum class ItemType {
    WEAPON, ARMOR, ACCESSORY, CONSUMABLE, GEM
}

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val rarity: Rarity,
    val atkBonus: Int = 0,
    val defBonus: Int = 0,
    val magBonus: Int = 0,
    val hpBonus: Int = 0,
    val spdBonus: Int = 0,
    val critBonus: Int = 0,
    val value: Int = 10,
    val effectTag: String? = null,
    val socketedGem: String? = null
)

enum class SkillTarget {
    SINGLE_ENEMY, ALL_ENEMIES, SINGLE_ALLY, ALL_ALLIES, SELF, AREA_GRID
}

enum class DamageType {
    PHYSICAL, FIRE, FROST, HOLY, SHADOW, POISON, ARCANE, HEAL
}

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val manaCost: Int,
    val apCost: Int = 1,
    val power: Int,
    val range: Int = 1,
    val aoeRadius: Int = 0,
    val targetType: SkillTarget,
    val damageType: DamageType,
    val cooldownMax: Int = 0,
    val currentCooldown: Int = 0,
    val iconKey: String = "attack"
)

enum class StatusEffectType {
    BURNING, POISONED, FROZEN, STUNNED, TAUNTED, BLESSED, STEALTHED, HASTED, TERRIFIED, SHIELDED
}

data class ActiveStatus(
    val type: StatusEffectType,
    val durationTurns: Int,
    val magnitude: Int = 0
)

enum class EnemyIntentType(val title: String, val iconKey: String) {
    ATTACK("Ataque Físico", "sword"),
    HEAVY_SMASH("Golpe Esmagador", "heavy"),
    ARCANE_SPELL("Magia Sombria", "spell"),
    DEFENSIVE_GUARD("Postura Defensiva", "shield"),
    CURSE("Maldição Obscura", "curse"),
    SUMMON("Convocar Lacaios", "summon")
}

data class EnemyIntent(
    val type: EnemyIntentType,
    val description: String,
    val targetHeroName: String? = null,
    val estimatedDamage: Int = 0
)

data class TacticalTile(
    val x: Int,
    val y: Int,
    val isCover: Boolean = false,
    val isTrap: Boolean = false,
    val isWall: Boolean = false,
    val label: String = ""
)

data class ActiveDiceRoll(
    val d20: Int,
    val modifier: Int,
    val total: Int,
    val isCrit: Boolean,
    val isMiss: Boolean,
    val advantage: Boolean,
    val disadvantage: Boolean,
    val actionName: String,
    val actorName: String,
    val targetName: String,
    val damage: Int = 0,
    val comboName: String? = null,
    val outcomeDescription: String = ""
)

data class Hero(
    val id: String,
    val name: String,
    val heroClass: HeroClass,
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val hp: Int,
    val maxHp: Int,
    val mp: Int,
    val maxMp: Int,
    val ap: Int = 2,
    val maxAp: Int = 2,
    val baseAtk: Int,
    val baseDef: Int,
    val baseMag: Int,
    val baseSpd: Int,
    val critRate: Int = 10,
    val sanity: Int = 100,
    val maxSanity: Int = 100,
    val skills: List<Skill> = emptyList(),
    val equippedWeapon: Item? = null,
    val equippedArmor: Item? = null,
    val equippedAccessory: Item? = null,
    val perks: List<String> = emptyList(),
    val skillPoints: Int = 0,
    val posX: Int = 0,
    val posY: Int = 0,
    val combatGridX: Int = 1,
    val combatGridY: Int = 1,
    val isGuarding: Boolean = false,
    val hasMovedThisTurn: Boolean = false,
    val isAlive: Boolean = true,
    val statusEffects: List<ActiveStatus> = emptyList()
) {
    val totalAtk: Int get() = baseAtk + (equippedWeapon?.atkBonus ?: 0) + (equippedAccessory?.atkBonus ?: 0)
    val totalDef: Int get() = baseDef + (equippedArmor?.defBonus ?: 0) + (equippedAccessory?.defBonus ?: 0) + (if (isGuarding) 6 else 0)
    val totalMag: Int get() = baseMag + (equippedWeapon?.magBonus ?: 0) + (equippedAccessory?.magBonus ?: 0)
    val totalSpd: Int get() = baseSpd + (equippedArmor?.spdBonus ?: 0) + (equippedAccessory?.spdBonus ?: 0)
    val totalCrit: Int get() = critRate + (equippedWeapon?.critBonus ?: 0) + (equippedAccessory?.critBonus ?: 0)
}

enum class EnemyCategory(val displayName: String) {
    UNDEAD("Morto-Vivo"),
    SUBTERRANEAN("Monstro Subterrâneo"),
    CULTIST("Cultista & Sacerdote Obscuro"),
    BOSS("Chefe Ancião")
}

data class Enemy(
    val id: String,
    val name: String,
    val title: String,
    val category: EnemyCategory,
    val level: Int = 1,
    val hp: Int,
    val maxHp: Int,
    val mp: Int = 20,
    val maxMp: Int = 20,
    val ap: Int = 2,
    val maxAp: Int = 2,
    val atk: Int,
    val def: Int,
    val mag: Int,
    val spd: Int,
    val xpReward: Int = 40,
    val goldReward: Int = 25,
    val isBoss: Boolean = false,
    val skills: List<Skill> = emptyList(),
    val posX: Int = 0,
    val posY: Int = 0,
    val combatGridX: Int = 4,
    val combatGridY: Int = 1,
    val currentIntent: EnemyIntent? = null,
    val isGuarding: Boolean = false,
    val isAlive: Boolean = true,
    val statusEffects: List<ActiveStatus> = emptyList()
)

enum class EnvironmentTheme(
    val title: String,
    val subtitle: String,
    val description: String,
    val ambientDescription: String,
    val dangerLevel: String,
    val primaryColorHex: Long
) {
    FORGOTTEN_CATACOMBS(
        title = "Catacumbas Esquecidas",
        subtitle = "Ossuários & Nichos Mortuários",
        description = "Corredores estreitos com pilhas de crânios milenares, sarcófagos rachados, teias de aranha densas e portas secretas em relevo de pedra.",
        ambientDescription = "O ar está denso com poeira mortuária. Tochas estalam projetando sombras fantasmagóricas nas paredes.",
        dangerLevel = "Iniciante • Nível 1-4",
        primaryColorHex = 0xFF78716C
    ),
    ROYAL_CRYPT(
        title = "Cripta Real",
        subtitle = "Tumbas de Reis & Vitrais Quebrados",
        description = "Salões grandiosos com colunas ornamentadas, mosaicos seculares no piso e sarcófagos reais guardados por armadilhas mortíferas.",
        ambientDescription = "Fachadas de vitrais partidos deixam passar tênues feixes lunares sobre o mármore manchado de eras.",
        dangerLevel = "Intermediário • Nível 4-7",
        primaryColorHex = 0xFFCA8A04
    ),
    ABANDONED_DUNGEON(
        title = "Masmorra Abandonada",
        subtitle = "Celas Enferrujadas & Poços Profundos",
        description = "Antigo cárcere de tortura com grades de ferro oxidado, poços com correntes penduradas e marcas de garras nas muralhas.",
        ambientDescription = "Gotas de água ecoam na escuridão. Um frio úmido penetra nas armaduras.",
        dangerLevel = "Perigoso • Nível 7-10",
        primaryColorHex = 0xFF475569
    ),
    NATURAL_CAVERNS(
        title = "Cavernas Naturais",
        subtitle = "Fungos Bioluminescentes & Cristais",
        description = "Lagos subterrâneos com água brilhante, cogumelos gigantes que emitem luz azulada, estalactites cristalinas e fendas de gás tóxico.",
        ambientDescription = "Brilho suave azulado reflete nas estalactites. O vento uiva através de abismos sem fundo.",
        dangerLevel = "Avançado • Nível 10-13",
        primaryColorHex = 0xFF0284C7
    ),
    SUBTERRANEAN_TEMPLE(
        title = "Templo Subterrâneo",
        subtitle = "Estátuas Colossais & Runas Arcanas",
        description = "Átrios gigantescos com estátuas de divindades esquecidas, altares de sacrifício com runas brilhantes e incensários místicos.",
        ambientDescription = "Cânticos distantes parecem vibrar nas pedras sagradas. A fumaça mística curva a própria luz.",
        dangerLevel = "Mortal • Nível 13-17",
        primaryColorHex = 0xFF9333EA
    ),
    ANCIENT_RUINS(
        title = "Ruínas Antigas",
        subtitle = "Arcos Quebrados & Maquinário Arcano",
        description = "Civilização pré-humana com maquinário antigo, bibliotecas de pergaminhos proibidos e vegetação subterrânea exótica.",
        ambientDescription = "Engrenagens de bronze do tamanho de torres repousam cobertas de musgo brilhante.",
        dangerLevel = "Lendário • Nível 17-20",
        primaryColorHex = 0xFFD97706
    )
}

enum class TileType(
    val symbol: String,
    val isWalkable: Boolean,
    val isInteractive: Boolean,
    val providesCover: Boolean
) {
    FLOOR(".", true, false, false),
    WALL("#", false, false, true),
    DOOR("+", true, true, false),
    SARCOPHAGUS("S", false, true, true),
    CHEST("C", true, true, false),
    ALTAR("A", false, true, true),
    TRAP_SPIKE("^", true, true, false),
    TRAP_POISON("~", true, true, false),
    SECRET_DOOR("?", true, true, true),
    FOUNTAIN("F", false, true, true),
    RUBBLE("R", true, false, true),
    TORCH_STAND("T", false, true, false),
    EXIT_STAIRS(">", true, true, false),
    FISSURE("X", false, false, false),
    TOXIC_GAS("G", true, false, false)
}

data class DungeonTile(
    val x: Int,
    val y: Int,
    val type: TileType,
    val isExplored: Boolean = false,
    val isVisible: Boolean = false,
    val hasTorchLit: Boolean = false,
    val isTriggered: Boolean = false,
    val lootClaimed: Boolean = false,
    val customNote: String? = null
)

data class DungeonMap(
    val id: String,
    val theme: EnvironmentTheme,
    val width: Int = 18,
    val height: Int = 14,
    val tiles: List<DungeonTile>,
    val name: String,
    val loreInscriptions: List<String> = emptyList()
)

data class PartyResources(
    val gold: Int = 120,
    val rations: Int = 6,
    val torches: Int = 4,
    val lockpicks: Int = 3,
    val healingPotions: Int = 3,
    val manaElixirs: Int = 2,
    val arcaneGems: Int = 1
)

enum class LogType {
    ATTACK, SPELL, HEAL, COMBO, STATUS, CRIT, SYSTEM, LOOT
}

data class CombatLogEntry(
    val id: String,
    val text: String,
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
)

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val theme: EnvironmentTheme,
    val goalText: String,
    val rewardGold: Int,
    val rewardXp: Int,
    val isCompleted: Boolean = false
)
