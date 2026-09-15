package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DungeonGenerator
import com.example.data.GameDatabase
import com.example.engine.CombatEngine
import com.example.engine.SoundEngine
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class GameScreen {
    HOME,
    SCENARIO_SELECT,
    EXPLORATION,
    COMBAT,
    PARTY_MANAGEMENT,
    DUNGEON_MASTER,
    CODEX,
    VICTORY_REWARD,
    DEFEAT
}

data class CombatTurnEntity(
    val id: String,
    val name: String,
    val isHero: Boolean,
    val speed: Int,
    val iconKey: String,
    val isAlive: Boolean = true
)

data class GameUiState(
    val currentScreen: GameScreen = GameScreen.HOME,
    val selectedTheme: EnvironmentTheme = EnvironmentTheme.FORGOTTEN_CATACOMBS,
    val activeQuest: Quest? = null,
    val dungeonMap: DungeonMap = DungeonGenerator.generateMapForTheme(EnvironmentTheme.FORGOTTEN_CATACOMBS),
    val party: List<Hero> = GameDatabase.createInitialParty(),
    val activeHeroIndex: Int = 0,
    val playerDungeonX: Int = 2,
    val playerDungeonY: Int = 2,
    val resources: PartyResources = PartyResources(),
    val torchLightRadius: Float = 3.5f,
    val stepsTakenInDark: Int = 0,
    val inventory: List<Item> = GameDatabase.allLootPool.take(4),

    // Turn-Based Exploration State
    val dungeonTurn: Int = 1,
    val dungeonPatrols: List<Enemy> = emptyList(),

    // Turn-Based Tactical Combat State
    val combatRound: Int = 1,
    val combatEnemies: List<Enemy> = emptyList(),
    val selectedEnemyIndex: Int? = null,
    val selectedSkill: Skill? = null,
    val initiativeQueue: List<CombatTurnEntity> = emptyList(),
    val currentTurnIndex: Int = 0,
    val isEnemyTurnProcessing: Boolean = false,
    val combatLogs: List<CombatLogEntry> = emptyList(),
    val activeBannerMessage: String? = null,
    val activeDiceRoll: ActiveDiceRoll? = null,
    val combatGridArena: List<TacticalTile> = defaultArenaTiles(),
    val isArenaExpanded: Boolean = true,

    // Rewards
    val gainedXp: Int = 0,
    val gainedGold: Int = 0,
    val gainedLoot: List<Item> = emptyList(),

    // DM Custom Map Editor State
    val dmSelectedBrush: TileType = TileType.WALL,
    val dmCustomMap: DungeonMap = DungeonGenerator.generateMapForTheme(EnvironmentTheme.FORGOTTEN_CATACOMBS),
    val dmSelectedTheme: EnvironmentTheme = EnvironmentTheme.ROYAL_CRYPT
) {
    val activeTurnEntity: CombatTurnEntity? get() = initiativeQueue.getOrNull(currentTurnIndex)
    val isPlayerTurn: Boolean get() = activeTurnEntity?.isHero == true && !isEnemyTurnProcessing
}

fun defaultArenaTiles(): List<TacticalTile> {
    val list = mutableListOf<TacticalTile>()
    for (x in 0 until 6) {
        for (y in 0 until 4) {
            val isCover = (x == 2 && y == 1) || (x == 3 && y == 2)
            val isTrap = (x == 2 && y == 2) || (x == 3 && y == 1)
            val label = when {
                isCover -> "Entulho (Cobertura +DEF)"
                isTrap -> "Espinhos (Armadilha)"
                x <= 1 -> "Zona dos Heróis"
                x >= 4 -> "Zona Inimiga"
                else -> "Chão de Pedra"
            }
            list.add(TacticalTile(x = x, y = y, isCover = isCover, isTrap = isTrap, label = label))
        }
    }
    return list
}

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val initialPatrols = createPatrolsForTheme(_uiState.value.selectedTheme)
        _uiState.update { it.copy(dungeonPatrols = initialPatrols) }
        updateExplorationVision()
    }

    private fun createPatrolsForTheme(theme: EnvironmentTheme): List<Enemy> {
        val pool = GameDatabase.getEnemiesForTheme(theme, isBossFight = false)
        val e1 = pool.getOrElse(0) { pool.first() }.copy(
            id = UUID.randomUUID().toString(),
            posX = 9,
            posY = 3,
            isAlive = true
        )
        val e2 = pool.getOrElse(1) { pool.first() }.copy(
            id = UUID.randomUUID().toString(),
            posX = 13,
            posY = 8,
            isAlive = true
        )
        return listOf(e1, e2)
    }

    fun navigateTo(screen: GameScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectScenario(theme: EnvironmentTheme) {
        val quest = GameDatabase.sampleQuests.find { it.theme == theme }
            ?: GameDatabase.sampleQuests.first()
        val newMap = DungeonGenerator.generateMapForTheme(theme)
        val patrols = createPatrolsForTheme(theme)

        _uiState.update {
            it.copy(
                selectedTheme = theme,
                activeQuest = quest,
                dungeonMap = newMap,
                playerDungeonX = 2,
                playerDungeonY = 2,
                dungeonTurn = 1,
                dungeonPatrols = patrols,
                currentScreen = GameScreen.EXPLORATION,
                combatLogs = listOf(
                    CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "O grupo adentrou ${theme.title}. Exploração por Turnos Iniciada! ${theme.ambientDescription}",
                        LogType.SYSTEM
                    )
                )
            )
        }
        updateExplorationVision()
    }

    // ==================== TURN-BASED DUNGEON EXPLORATION ====================

    fun advanceDungeonTurn(turns: Int = 1, actionDescription: String = "Avanço") {
        val state = _uiState.value
        val newTurn = state.dungeonTurn + turns
        var steps = state.stepsTakenInDark + turns
        var updatedResources = state.resources
        var torchRadius = state.torchLightRadius
        var newParty = state.party
        val map = state.dungeonMap

        // Torch consumption
        if (steps >= 10) {
            steps = 0
            if (updatedResources.torches > 0) {
                updatedResources = updatedResources.copy(torches = updatedResources.torches - 1)
                torchRadius = 3.5f
            } else {
                torchRadius = 1.8f // Dim light
                newParty = newParty.map { hero ->
                    hero.copy(sanity = maxOf(10, hero.sanity - 2))
                }
            }
        }

        // Move wandering patrol monsters 1 step per turn on walkable tiles
        var ambushPatrol: Enemy? = null
        val movedPatrols = state.dungeonPatrols.map { patrol ->
            if (!patrol.isAlive) return@map patrol

            val dist = kotlin.math.abs(patrol.posX - state.playerDungeonX) + kotlin.math.abs(patrol.posY - state.playerDungeonY)
            if (dist <= 1) {
                ambushPatrol = patrol
                return@map patrol
            }

            val validNeighbors = listOf(
                Pair(patrol.posX + 1, patrol.posY),
                Pair(patrol.posX - 1, patrol.posY),
                Pair(patrol.posX, patrol.posY + 1),
                Pair(patrol.posX, patrol.posY - 1)
            ).filter { (x, y) ->
                val tile = map.tiles.find { it.x == x && it.y == y }
                tile != null && tile.type.isWalkable
            }

            if (validNeighbors.isNotEmpty()) {
                val chosen = if (dist <= 6) {
                    validNeighbors.minByOrNull { (x, y) ->
                        kotlin.math.abs(x - state.playerDungeonX) + kotlin.math.abs(y - state.playerDungeonY)
                    } ?: validNeighbors.random()
                } else {
                    validNeighbors.random()
                }
                patrol.copy(posX = chosen.first, posY = chosen.second)
            } else {
                patrol
            }
        }

        _uiState.update {
            it.copy(
                dungeonTurn = newTurn,
                stepsTakenInDark = steps,
                resources = updatedResources,
                torchLightRadius = torchRadius,
                party = newParty,
                dungeonPatrols = movedPatrols
            )
        }

        updateExplorationVision()

        if (ambushPatrol != null) {
            val survivingPatrols = movedPatrols.filter { it.id != ambushPatrol?.id }
            _uiState.update {
                it.copy(
                    dungeonPatrols = survivingPatrols,
                    activeBannerMessage = "⚔ EMBOSCADA! A patrulha de ${ambushPatrol?.name} alcançou seu grupo no Turno #$newTurn!",
                    combatLogs = it.combatLogs + CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "⚔ EMBOSCADA! Inimigos errantes interceptaram o grupo no Turno #$newTurn!",
                        LogType.ATTACK
                    )
                )
            }
            startCombat(isBoss = false, specificEnemies = listOf(ambushPatrol!!))
        }
    }

    fun waitExplorationTurn() {
        SoundEngine.playStep()
        advanceDungeonTurn(1, "Aguardar em Guarda")
        _uiState.update {
            it.copy(
                activeBannerMessage = "Turno #${it.dungeonTurn}: Grupo aguardou em alerta.",
                combatLogs = it.combatLogs + CombatLogEntry(
                    UUID.randomUUID().toString(),
                    "Turno #${it.dungeonTurn}: O grupo manteve vigília silenciosa enquanto o tempo passava.",
                    LogType.SYSTEM
                )
            )
        }
    }

    fun movePlayer(dx: Int, dy: Int) {
        val state = _uiState.value
        val newX = state.playerDungeonX + dx
        val newY = state.playerDungeonY + dy

        val map = state.dungeonMap
        if (newX !in 0 until map.width || newY !in 0 until map.height) return

        val targetTile = map.tiles.find { it.x == newX && it.y == newY } ?: return

        // Check walkability
        if (!targetTile.type.isWalkable && targetTile.type != TileType.DOOR && targetTile.type != TileType.SECRET_DOOR) {
            return
        }

        SoundEngine.playStep()

        var newParty = state.party
        var hazardLog: CombatLogEntry? = null
        if (targetTile.type == TileType.TRAP_SPIKE && !targetTile.isTriggered) {
            hazardLog = CombatLogEntry(UUID.randomUUID().toString(), "Armadilha de Espinho ativada! O grupo sofreu 12 de dano físico.", LogType.ATTACK)
            newParty = newParty.map { it.copy(hp = maxOf(1, it.hp - 12)) }
            SoundEngine.playAttack()
        } else if (targetTile.type == TileType.TRAP_POISON && !targetTile.isTriggered) {
            hazardLog = CombatLogEntry(UUID.randomUUID().toString(), "Nuvem tóxica disparada! Veneno espalhado no ar.", LogType.STATUS)
            newParty = newParty.map { it.copy(hp = maxOf(1, it.hp - 8)) }
            SoundEngine.playSpell()
        }

        val updatedTiles = map.tiles.map {
            if (it.x == newX && it.y == newY) {
                if (it.type == TileType.DOOR || it.type == TileType.SECRET_DOOR) {
                    it.copy(type = TileType.FLOOR, isTriggered = true)
                } else if (it.type == TileType.TRAP_SPIKE || it.type == TileType.TRAP_POISON) {
                    it.copy(isTriggered = true)
                } else it
            } else it
        }

        val updatedMap = map.copy(tiles = updatedTiles)

        _uiState.update {
            it.copy(
                playerDungeonX = newX,
                playerDungeonY = newY,
                dungeonMap = updatedMap,
                party = newParty,
                combatLogs = if (hazardLog != null) it.combatLogs + hazardLog else it.combatLogs
            )
        }

        advanceDungeonTurn(1, "Movimento")
        checkTileEncounter(newX, newY)
    }

    private fun updateExplorationVision() {
        val state = _uiState.value
        val updatedMap = DungeonGenerator.updateFogOfWar(
            state.dungeonMap,
            state.playerDungeonX,
            state.playerDungeonY,
            state.torchLightRadius
        )
        _uiState.update { it.copy(dungeonMap = updatedMap) }
    }

    private fun checkTileEncounter(x: Int, y: Int) {
        val state = _uiState.value
        val tile = state.dungeonMap.tiles.find { it.x == x && it.y == y } ?: return

        if (tile.type == TileType.EXIT_STAIRS) {
            // Reached boss / exit
            startCombat(isBoss = true)
        } else if ((x == 7 && y == 4) || (x == 10 && y == 5)) {
            // Preset encounter zones in chambers
            startCombat(isBoss = false)
        }
    }

    fun interactWithTile() {
        val state = _uiState.value
        val x = state.playerDungeonX
        val y = state.playerDungeonY
        val tile = state.dungeonMap.tiles.find { it.x == x && it.y == y } ?: return

        when (tile.type) {
            TileType.CHEST -> {
                if (!tile.lootClaimed) {
                    val rewardGold = (40..90).random()
                    val randomLoot = GameDatabase.allLootPool.random()
                    val newLogs = state.combatLogs + CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "Baú Antigo Aberto! Encontrado: ${randomLoot.name} e $rewardGold Moedas de Ouro!",
                        LogType.LOOT
                    )
                    SoundEngine.playLoot()

                    val updatedTiles = state.dungeonMap.tiles.map {
                        if (it.x == x && it.y == y) it.copy(lootClaimed = true, type = TileType.FLOOR) else it
                    }

                    _uiState.update {
                        it.copy(
                            resources = it.resources.copy(gold = it.resources.gold + rewardGold),
                            inventory = it.inventory + randomLoot,
                            dungeonMap = it.dungeonMap.copy(tiles = updatedTiles),
                            combatLogs = newLogs,
                            activeBannerMessage = "Baú Aberto: +$rewardGold Ouro, ${randomLoot.name}!"
                        )
                    }
                }
            }
            TileType.SARCOPHAGUS -> {
                if (!tile.lootClaimed) {
                    val isTrap = (0..1).random() == 1
                    if (isTrap) {
                        startCombat(isBoss = false)
                    } else {
                        val gold = (30..70).random()
                        SoundEngine.playLoot()
                        _uiState.update {
                            it.copy(
                                resources = it.resources.copy(gold = it.resources.gold + gold),
                                combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "Sarcófago inspecionado. Encontradas relíquias valiosas de $gold ouro.", LogType.LOOT),
                                activeBannerMessage = "Sarcófago Real saqueado: +$gold Ouro"
                            )
                        }
                    }
                }
            }
            TileType.ALTAR -> {
                // Holy blessing
                SoundEngine.playHeal()
                val blessedParty = state.party.map {
                    it.copy(
                        hp = minOf(it.maxHp, it.hp + 40),
                        mp = minOf(it.maxMp, it.mp + 30),
                        sanity = minOf(it.maxSanity, it.sanity + 25)
                    )
                }
                _uiState.update {
                    it.copy(
                        party = blessedParty,
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "Altar Ancestral ativado! Graça Divina restaura vida, mana e sanidade do grupo!", LogType.HEAL),
                        activeBannerMessage = "Bênção Ancestral Recebida!"
                    )
                }
            }
            TileType.FOUNTAIN -> {
                // Luminescent pool
                SoundEngine.playHeal()
                val healedParty = state.party.map { it.copy(hp = it.maxHp, mp = it.maxMp) }
                _uiState.update {
                    it.copy(
                        party = healedParty,
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "Fonte de Águas Luminescentes purifica todas as feridas do grupo!", LogType.HEAL),
                        activeBannerMessage = "Vida e Mana Totalmente Restaurados!"
                    )
                }
            }
            TileType.TORCH_STAND -> {
                _uiState.update {
                    it.copy(
                        resources = it.resources.copy(torches = it.resources.torches + 1),
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "Tocha coletada do suporte na parede.", LogType.LOOT)
                    )
                }
            }
            else -> {}
        }
    }

    // ==================== REST & SURVIVAL ====================

    fun performShortRest() {
        val state = _uiState.value
        if (state.resources.rations <= 0) {
            _uiState.update {
                it.copy(activeBannerMessage = "Sem rações suficientes para um descanso curto!")
            }
            return
        }

        SoundEngine.playRest()
        val updatedParty = state.party.map { hero ->
            hero.copy(
                hp = minOf(hero.maxHp, hero.hp + (hero.maxHp * 0.4f).toInt()),
                mp = minOf(hero.maxMp, hero.mp + (hero.maxMp * 0.3f).toInt()),
                sanity = minOf(hero.maxSanity, hero.sanity + 15)
            )
        }

        _uiState.update {
            it.copy(
                resources = it.resources.copy(rations = it.resources.rations - 1),
                party = updatedParty,
                combatLogs = it.combatLogs + CombatLogEntry(
                    UUID.randomUUID().toString(),
                    "Descanso Curto realizado. Rações consumidas (-1). Vida, mana e sanidade revigorados.",
                    LogType.SYSTEM
                ),
                activeBannerMessage = "Descanso Curto Concluído (-1 Ração)"
            )
        }
    }

    fun useTorch() {
        val state = _uiState.value
        if (state.resources.torches > 0) {
            _uiState.update {
                it.copy(
                    resources = it.resources.copy(torches = it.resources.torches - 1),
                    torchLightRadius = 4.5f,
                    stepsTakenInDark = 0,
                    activeBannerMessage = "Nova Tocha Acesa! Visão restaurada."
                )
            }
            updateExplorationVision()
        }
    }

    fun useHealingPotion(heroIndex: Int) {
        val state = _uiState.value
        if (state.resources.healingPotions > 0 && heroIndex in state.party.indices) {
            SoundEngine.playHeal()
            val target = state.party[heroIndex]
            val updatedHero = target.copy(hp = minOf(target.maxHp, target.hp + 60))
            val newParty = state.party.toMutableList().apply { set(heroIndex, updatedHero) }
            _uiState.update {
                it.copy(
                    resources = it.resources.copy(healingPotions = it.resources.healingPotions - 1),
                    party = newParty,
                    activeBannerMessage = "Poção de Cura usada em ${target.name} (+60 HP)"
                )
            }
        }
    }

    // ==================== TACTICAL COMBAT ====================

    fun startCombat(isBoss: Boolean = false, specificEnemies: List<Enemy>? = null) {
        val state = _uiState.value
        val rawEnemies = specificEnemies ?: GameDatabase.getEnemiesForTheme(state.selectedTheme, isBoss)

        // Deploy heroes onto grid (columns 0 & 1)
        val positionedParty = state.party.mapIndexed { index, hero ->
            val gx = if (index % 2 == 0) 1 else 0
            val gy = (index % 4)
            hero.copy(
                combatGridX = gx,
                combatGridY = gy,
                isGuarding = false,
                hasMovedThisTurn = false,
                ap = hero.maxAp
            )
        }

        // Deploy enemies onto grid (columns 4 & 5) with telegraphed intents
        val enemies = rawEnemies.mapIndexed { index, enemy ->
            val gx = if (index % 2 == 0) 4 else 5
            val gy = (index % 4)
            val intent = CombatEngine.generateEnemyIntent(enemy, positionedParty.filter { it.isAlive })
            enemy.copy(
                combatGridX = gx,
                combatGridY = gy,
                currentIntent = intent,
                isGuarding = false
            )
        }

        // Build Initiative (d20 + speed)
        val entities = mutableListOf<CombatTurnEntity>()
        positionedParty.forEach { hero ->
            if (hero.isAlive) {
                val initRoll = (1..20).random() + hero.totalSpd
                entities.add(CombatTurnEntity(hero.id, hero.name, isHero = true, speed = initRoll, iconKey = hero.heroClass.name.lowercase(), isAlive = true))
            }
        }
        enemies.forEach { enemy ->
            val initRoll = (1..20).random() + enemy.spd
            entities.add(CombatTurnEntity(enemy.id, enemy.name, isHero = false, speed = initRoll, iconKey = "enemy", isAlive = true))
        }

        val sortedInitiative = entities.sortedByDescending { it.speed }
        val firstEntity = sortedInitiative.firstOrNull()
        val firstIsHero = firstEntity?.isHero == true
        val initialHeroIdx = if (firstIsHero && firstEntity != null) {
            positionedParty.indexOfFirst { it.id == firstEntity.id }.coerceAtLeast(0)
        } else {
            positionedParty.indexOfFirst { it.isAlive }.coerceAtLeast(0)
        }

        val initLog = "Iniciativas: " + sortedInitiative.joinToString(", ") { "${it.name.split(' ').first()}: ${it.speed}" }

        _uiState.update {
            it.copy(
                currentScreen = GameScreen.COMBAT,
                party = positionedParty,
                combatEnemies = enemies,
                initiativeQueue = sortedInitiative,
                combatRound = 1,
                currentTurnIndex = 0,
                selectedEnemyIndex = 0,
                selectedSkill = positionedParty.getOrNull(initialHeroIdx)?.skills?.firstOrNull(),
                activeHeroIndex = initialHeroIdx,
                isEnemyTurnProcessing = !firstIsHero,
                activeDiceRoll = null,
                combatLogs = it.combatLogs + listOf(
                    CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "⚔ $initLog",
                        LogType.SYSTEM
                    ),
                    CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "Rodada 1 • Turno de ${firstEntity?.name}!",
                        LogType.SYSTEM
                    )
                ),
                activeBannerMessage = if (firstIsHero) "Sua vez! Turno de ${firstEntity?.name}." else "Turno de ${firstEntity?.name}..."
            )
        }

        if (!firstIsHero && firstEntity != null) {
            viewModelScope.launch {
                delay(600)
                processSingleEnemyTurn(firstEntity)
            }
        }
    }

    fun selectEnemy(index: Int) {
        _uiState.update { it.copy(selectedEnemyIndex = index) }
    }

    fun selectSkill(skill: Skill) {
        _uiState.update { it.copy(selectedSkill = skill) }
    }

    fun selectHeroTab(index: Int) {
        if (index in _uiState.value.party.indices) {
            val hero = _uiState.value.party[index]
            _uiState.update {
                it.copy(
                    activeHeroIndex = index,
                    selectedSkill = hero.skills.firstOrNull()
                )
            }
        }
    }

    fun dismissDiceRoll() {
        _uiState.update { it.copy(activeDiceRoll = null) }
    }

    fun moveHeroOnGrid(targetX: Int, targetY: Int) {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        if (!activeHero.isAlive || state.isEnemyTurnProcessing) return

        if (activeHero.ap <= 0 && activeHero.hasMovedThisTurn) {
            _uiState.update { it.copy(activeBannerMessage = "Sem Pontos de Ação (AP) para mover!") }
            return
        }

        if (targetX !in 0..5 || targetY !in 0..3) return

        // Check if tile is already occupied
        val isOccupied = state.party.any { it.id != activeHero.id && it.isAlive && it.combatGridX == targetX && it.combatGridY == targetY } ||
                state.combatEnemies.any { it.isAlive && it.combatGridX == targetX && it.combatGridY == targetY }

        if (isOccupied) {
            _uiState.update { it.copy(activeBannerMessage = "Posição já ocupada!") }
            return
        }

        val dist = kotlin.math.abs(activeHero.combatGridX - targetX) + kotlin.math.abs(activeHero.combatGridY - targetY)
        if (dist > 2) {
            _uiState.update { it.copy(activeBannerMessage = "Alcance de movimento máximo: 2 quadros!") }
            return
        }

        val apCost = if (activeHero.hasMovedThisTurn) 1 else 0
        val updatedHero = activeHero.copy(
            combatGridX = targetX,
            combatGridY = targetY,
            hasMovedThisTurn = true,
            ap = maxOf(0, activeHero.ap - apCost)
        )
        val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

        _uiState.update {
            it.copy(
                party = updatedParty,
                combatLogs = it.combatLogs + CombatLogEntry(
                    UUID.randomUUID().toString(),
                    "${activeHero.name} moveu-se taticamente para ($targetX, $targetY).",
                    LogType.SYSTEM
                )
            )
        }
    }

    fun executeGuard() {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        if (!activeHero.isAlive || activeHero.ap <= 0 || state.isEnemyTurnProcessing) {
            _uiState.update { it.copy(activeBannerMessage = "Sem AP suficiente para postura defensiva!") }
            return
        }

        SoundEngine.playShieldBlock()
        val updatedHero = activeHero.copy(
            ap = activeHero.ap - 1,
            isGuarding = true,
            statusEffects = activeHero.statusEffects + ActiveStatus(StatusEffectType.SHIELDED, 1)
        )
        val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

        val log = CombatLogEntry(
            UUID.randomUUID().toString(),
            "${activeHero.name} assumiu Postura Defensiva (+6 DEF e Redução de Dano até o próximo turno)!",
            LogType.STATUS
        )

        _uiState.update {
            it.copy(
                party = updatedParty,
                combatLogs = it.combatLogs + log,
                activeBannerMessage = "${activeHero.name} em Guarda Defensiva!"
            )
        }

        checkPassTurnIfNoAp()
    }

    fun executeShove() {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        val targetIndex = state.selectedEnemyIndex ?: 0
        val targetEnemy = state.combatEnemies.getOrNull(targetIndex) ?: return

        if (!activeHero.isAlive || activeHero.ap <= 0 || state.isEnemyTurnProcessing) {
            _uiState.update { it.copy(activeBannerMessage = "Sem AP suficiente para empurrar!") }
            return
        }

        val dist = kotlin.math.abs(activeHero.combatGridX - targetEnemy.combatGridX) +
                   kotlin.math.abs(activeHero.combatGridY - targetEnemy.combatGridY)
        if (dist > 1) {
            _uiState.update { it.copy(activeBannerMessage = "Muito longe para empurrar! Aproxime-se a 1 quadro do inimigo.") }
            return
        }

        SoundEngine.playShove()
        val newX = minOf(5, targetEnemy.combatGridX + 1)
        val hitWall = targetEnemy.combatGridX >= 5
        val hitTrap = (newX == 2 && targetEnemy.combatGridY == 2) || (newX == 3 && targetEnemy.combatGridY == 1)

        val result = CombatEngine.shoveEnemy(activeHero, targetEnemy, hitWall = hitWall, hitTrap = hitTrap)
        val newHp = maxOf(0, targetEnemy.hp - result.damage)
        val isEnemyDead = newHp <= 0
        val updatedEnemy = targetEnemy.copy(
            combatGridX = newX,
            hp = newHp,
            isAlive = !isEnemyDead
        )
        val updatedEnemies = state.combatEnemies.toMutableList().apply { set(targetIndex, updatedEnemy) }
        val updatedHero = activeHero.copy(ap = activeHero.ap - 1)
        val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

        _uiState.update {
            it.copy(
                combatEnemies = updatedEnemies,
                party = updatedParty,
                combatLogs = it.combatLogs + result.logs,
                activeDiceRoll = result.diceRoll
            )
        }

        if (updatedEnemies.all { !it.isAlive }) {
            handleCombatVictory(updatedEnemies)
            return
        }

        checkPassTurnIfNoAp()
    }

    fun useCombatItem(itemKey: String) {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        if (!activeHero.isAlive || activeHero.ap <= 0 || state.isEnemyTurnProcessing) {
            _uiState.update { it.copy(activeBannerMessage = "Sem AP para usar item!") }
            return
        }

        when (itemKey) {
            "potion" -> {
                if (state.resources.healingPotions <= 0) {
                    _uiState.update { it.copy(activeBannerMessage = "Sem Poções de Cura restantes!") }
                    return
                }
                SoundEngine.playHeal()
                val updatedHero = activeHero.copy(
                    hp = minOf(activeHero.maxHp, activeHero.hp + 60),
                    ap = activeHero.ap - 1
                )
                val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }
                _uiState.update {
                    it.copy(
                        resources = it.resources.copy(healingPotions = it.resources.healingPotions - 1),
                        party = updatedParty,
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "${activeHero.name} bebeu uma Poção de Cura (+60 HP)!", LogType.HEAL),
                        activeBannerMessage = "+60 HP restaurados!"
                    )
                }
            }
            "alchemist_fire" -> {
                val targetIndex = state.selectedEnemyIndex ?: 0
                val targetEnemy = state.combatEnemies.getOrNull(targetIndex) ?: return
                SoundEngine.playCrit()
                val dmg = 26
                val newHp = maxOf(0, targetEnemy.hp - dmg)
                val updatedEnemy = targetEnemy.copy(
                    hp = newHp,
                    isAlive = newHp > 0,
                    statusEffects = targetEnemy.statusEffects + ActiveStatus(StatusEffectType.BURNING, durationTurns = 2, magnitude = 8)
                )
                val updatedEnemies = state.combatEnemies.toMutableList().apply { set(targetIndex, updatedEnemy) }
                val updatedHero = activeHero.copy(ap = activeHero.ap - 1)
                val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

                val roll = ActiveDiceRoll(
                    d20 = 20, modifier = 6, total = 26, isCrit = true, isMiss = false,
                    advantage = false, disadvantage = false, actionName = "Fogo dos Alquimistas",
                    actorName = activeHero.name, targetName = targetEnemy.name, damage = dmg,
                    comboName = "Detonação Flamejante + Incêndio Ativo",
                    outcomeDescription = "O frasco detonou espalhando chamas vorazes!"
                )

                _uiState.update {
                    it.copy(
                        combatEnemies = updatedEnemies,
                        party = updatedParty,
                        activeDiceRoll = roll,
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "${activeHero.name} arremessou Fogo dos Alquimistas em ${targetEnemy.name} (26 Dano Flamejante + Queimadura)!", LogType.CRIT)
                    )
                }

                if (updatedEnemies.all { !it.isAlive }) {
                    handleCombatVictory(updatedEnemies)
                    return
                }
            }
            "smoke_bomb" -> {
                SoundEngine.playSpell()
                val updatedHero = activeHero.copy(
                    ap = activeHero.ap - 1,
                    statusEffects = activeHero.statusEffects + ActiveStatus(StatusEffectType.STEALTHED, durationTurns = 2)
                )
                val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }
                _uiState.update {
                    it.copy(
                        party = updatedParty,
                        combatLogs = it.combatLogs + CombatLogEntry(UUID.randomUUID().toString(), "${activeHero.name} detonou Bomba de Fumaça e entrou em Furtividade!", LogType.STATUS),
                        activeBannerMessage = "${activeHero.name} está Furtivo nas Sombras!"
                    )
                }
            }
        }
        checkPassTurnIfNoAp()
    }

    fun executeHeroAction() {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        val skill = state.selectedSkill ?: activeHero.skills.firstOrNull() ?: return
        val targetEnemyIndex = state.selectedEnemyIndex ?: 0
        val targetEnemy = state.combatEnemies.getOrNull(targetEnemyIndex) ?: return

        if (!activeHero.isAlive || activeHero.ap <= 0 || state.isEnemyTurnProcessing) {
            _uiState.update { it.copy(activeBannerMessage = "Sem Pontos de Ação (AP) restantes neste turno!") }
            return
        }

        if (activeHero.mp < skill.manaCost) {
            _uiState.update { it.copy(activeBannerMessage = "Mana insuficiente para ${skill.name}!") }
            return
        }

        // Execute Attack / Skill
        if (skill.targetType == SkillTarget.SINGLE_ALLY || skill.targetType == SkillTarget.ALL_ALLIES) {
            SoundEngine.playHeal()
            val healedParty = state.party.map { hero ->
                if (skill.targetType == SkillTarget.ALL_ALLIES || hero.id == activeHero.id) {
                    hero.copy(
                        hp = minOf(hero.maxHp, hero.hp + skill.power),
                        statusEffects = hero.statusEffects + ActiveStatus(StatusEffectType.BLESSED, 2)
                    )
                } else hero
            }
            val log = CombatLogEntry(
                UUID.randomUUID().toString(),
                "${activeHero.name} canalizou ${skill.name} curando e abençoando o grupo!",
                LogType.HEAL
            )
            val updatedHero = activeHero.copy(
                ap = activeHero.ap - skill.apCost,
                mp = activeHero.mp - skill.manaCost
            )
            val updatedParty = healedParty.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

            _uiState.update {
                it.copy(
                    party = updatedParty,
                    combatLogs = it.combatLogs + log
                )
            }
        } else {
            // Offensive Skill on Enemy
            val result = CombatEngine.calculateHeroAttack(activeHero, targetEnemy, skill, state.party)
            SoundEngine.playDiceRoll()
            if (result.isCrit) SoundEngine.playCrit() else SoundEngine.playAttack()

            val newEnemyHp = maxOf(0, targetEnemy.hp - result.damage)
            val isEnemyDead = newEnemyHp <= 0
            val updatedEnemy = targetEnemy.copy(
                hp = newEnemyHp,
                isAlive = !isEnemyDead,
                statusEffects = if (result.appliedStatus != null) targetEnemy.statusEffects + result.appliedStatus else targetEnemy.statusEffects
            )

            val updatedEnemies = state.combatEnemies.toMutableList().apply {
                set(targetEnemyIndex, updatedEnemy)
            }

            val updatedHero = activeHero.copy(
                ap = activeHero.ap - skill.apCost,
                mp = activeHero.mp - skill.manaCost
            )
            val updatedParty = state.party.toMutableList().apply { set(state.activeHeroIndex, updatedHero) }

            val additionalLogs = result.logs.toMutableList()
            if (isEnemyDead) {
                additionalLogs.add(CombatLogEntry(UUID.randomUUID().toString(), "${targetEnemy.name} foi derrotado!", LogType.LOOT))
            }

            _uiState.update {
                it.copy(
                    combatEnemies = updatedEnemies,
                    party = updatedParty,
                    combatLogs = it.combatLogs + additionalLogs,
                    activeDiceRoll = result.diceRoll
                )
            }

            // Check if all enemies dead -> Victory
            if (updatedEnemies.all { !it.isAlive }) {
                handleCombatVictory(updatedEnemies)
                return
            }
        }

        checkPassTurnIfNoAp()
    }

    private fun checkPassTurnIfNoAp() {
        val state = _uiState.value
        val activeHero = state.party.getOrNull(state.activeHeroIndex) ?: return
        if (activeHero.ap <= 0) {
            _uiState.update {
                it.copy(activeBannerMessage = "${activeHero.name} gastou todos os AP. Passando turno...")
            }
            viewModelScope.launch {
                delay(700)
                advanceToNextTurn()
            }
        }
    }

    fun endPlayerTurn() {
        val state = _uiState.value
        if (state.isEnemyTurnProcessing) return
        advanceToNextTurn()
    }

    fun advanceToNextTurn() {
        val state = _uiState.value
        if (state.initiativeQueue.isEmpty()) return

        // Update initiative queue liveness from current party and enemies
        val aliveHeroIds = state.party.filter { it.isAlive }.map { it.id }.toSet()
        val aliveEnemyIds = state.combatEnemies.filter { it.isAlive }.map { it.id }.toSet()
        val updatedQueue = state.initiativeQueue.map { entity ->
            val isNowAlive = if (entity.isHero) aliveHeroIds.contains(entity.id) else aliveEnemyIds.contains(entity.id)
            entity.copy(isAlive = isNowAlive)
        }

        // Check victory or defeat first
        if (aliveHeroIds.isEmpty()) {
            _uiState.update { it.copy(currentScreen = GameScreen.DEFEAT, isEnemyTurnProcessing = false) }
            return
        }
        if (aliveEnemyIds.isEmpty()) {
            handleCombatVictory(state.combatEnemies)
            return
        }

        // Advance to next living entity
        var nextIdx = (state.currentTurnIndex + 1) % updatedQueue.size
        var newRound = state.combatRound
        var wrapped = false

        if (nextIdx == 0) {
            newRound++
            wrapped = true
        }

        // Search for next alive entity
        var attempts = 0
        while (!updatedQueue[nextIdx].isAlive && attempts < updatedQueue.size) {
            nextIdx = (nextIdx + 1) % updatedQueue.size
            if (nextIdx == 0) {
                newRound++
                wrapped = true
            }
            attempts++
        }

        val nextEntity = updatedQueue[nextIdx]

        // If wrapped to a new round, refresh AP and enemy intents
        val refreshedParty = if (wrapped) {
            state.party.map { it.copy(ap = it.maxAp, isGuarding = false, hasMovedThisTurn = false) }
        } else state.party

        val refreshedEnemies = if (wrapped) {
            state.combatEnemies.map { enemy ->
                if (!enemy.isAlive) enemy else enemy.copy(
                    currentIntent = CombatEngine.generateEnemyIntent(enemy, refreshedParty.filter { it.isAlive }),
                    isGuarding = false
                )
            }
        } else state.combatEnemies

        val nextIsHero = nextEntity.isHero
        val nextHeroIndex = if (nextIsHero) {
            refreshedParty.indexOfFirst { it.id == nextEntity.id }.coerceAtLeast(0)
        } else state.activeHeroIndex

        val roundMsg = if (wrapped) "--- Início da Rodada $newRound ---" else null

        val newLogs = state.combatLogs.toMutableList()
        if (roundMsg != null) {
            newLogs.add(CombatLogEntry(UUID.randomUUID().toString(), roundMsg, LogType.SYSTEM))
        }
        newLogs.add(CombatLogEntry(UUID.randomUUID().toString(), "Turno de ${nextEntity.name} (Iniciativa ${nextEntity.speed})", LogType.SYSTEM))

        _uiState.update {
            it.copy(
                initiativeQueue = updatedQueue,
                currentTurnIndex = nextIdx,
                combatRound = newRound,
                party = refreshedParty,
                combatEnemies = refreshedEnemies,
                activeHeroIndex = nextHeroIndex,
                selectedSkill = refreshedParty.getOrNull(nextHeroIndex)?.skills?.firstOrNull(),
                isEnemyTurnProcessing = !nextIsHero,
                activeDiceRoll = null,
                combatLogs = newLogs,
                activeBannerMessage = if (nextIsHero) "Sua vez! Turno de ${nextEntity.name}." else "Turno de ${nextEntity.name}..."
            )
        }

        if (!nextIsHero) {
            viewModelScope.launch {
                delay(700)
                processSingleEnemyTurn(nextEntity)
            }
        }
    }

    private suspend fun processSingleEnemyTurn(enemyEntity: CombatTurnEntity) {
        val state = _uiState.value
        val enemy = state.combatEnemies.find { it.id == enemyEntity.id }
        if (enemy == null || !enemy.isAlive) {
            advanceToNextTurn()
            return
        }

        // Check if enemy is frozen/stunned
        if (enemy.statusEffects.any { it.type == StatusEffectType.FROZEN || it.type == StatusEffectType.STUNNED }) {
            _uiState.update {
                it.copy(
                    combatLogs = it.combatLogs + CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "${enemy.name} está congelado/atordoado e perdeu seu turno!",
                        LogType.STATUS
                    ),
                    activeBannerMessage = "${enemy.name} atordoado - Turno pulado!"
                )
            }
            delay(500)
            advanceToNextTurn()
            return
        }

        var currentParty = state.party.toMutableList()
        val aliveHeroes = currentParty.filter { it.isAlive }
        if (aliveHeroes.isEmpty()) {
            _uiState.update { it.copy(currentScreen = GameScreen.DEFEAT, isEnemyTurnProcessing = false) }
            return
        }

        // Focus taunted hero or designated intent target
        val tauntedHero = aliveHeroes.find { h -> enemy.statusEffects.any { it.type == StatusEffectType.TAUNTED } }
        val targetHero = tauntedHero
            ?: aliveHeroes.find { it.name == enemy.currentIntent?.targetHeroName }
            ?: (aliveHeroes.find { it.heroClass == HeroClass.WARRIOR } ?: aliveHeroes.random())

        val targetIndex = currentParty.indexOfFirst { it.id == targetHero.id }

        val attackRes = CombatEngine.calculateEnemyAttack(enemy, targetHero)
        SoundEngine.playDiceRoll()
        SoundEngine.playAttack()

        val newHeroHp = maxOf(0, targetHero.hp - attackRes.damage)
        val updatedTargetHero = targetHero.copy(
            hp = newHeroHp,
            isAlive = newHeroHp > 0
        )
        currentParty[targetIndex] = updatedTargetHero

        _uiState.update {
            it.copy(
                party = currentParty.toList(),
                combatLogs = it.combatLogs + attackRes.logs,
                activeDiceRoll = attackRes.diceRoll,
                activeBannerMessage = "${enemy.name} agiu contra ${targetHero.name} (-${attackRes.damage} HP)!"
            )
        }

        delay(900)

        // Check defeat
        if (currentParty.all { !it.isAlive }) {
            _uiState.update {
                it.copy(
                    currentScreen = GameScreen.DEFEAT,
                    isEnemyTurnProcessing = false,
                    activeDiceRoll = null
                )
            }
            return
        }

        advanceToNextTurn()
    }

    private fun handleCombatVictory(defeatedEnemies: List<Enemy>) {
        SoundEngine.playLevelUp()
        val totalXp = defeatedEnemies.sumOf { it.xpReward }
        val totalGold = defeatedEnemies.sumOf { it.goldReward }
        val randomDrop = if ((0..100).random() > 40) GameDatabase.allLootPool.random() else null
        val lootList = listOfNotNull(randomDrop)

        // Award XP & Check Level Up
        val updatedParty = _uiState.value.party.map { hero ->
            if (!hero.isAlive) hero else {
                val newXp = hero.xp + totalXp
                if (newXp >= hero.maxXp && hero.level < 20) {
                    hero.copy(
                        level = hero.level + 1,
                        xp = newXp - hero.maxXp,
                        maxXp = (hero.maxXp * 1.5f).toInt(),
                        maxHp = hero.maxHp + 15,
                        hp = hero.maxHp + 15,
                        baseAtk = hero.baseAtk + 3,
                        baseDef = hero.baseDef + 2,
                        baseMag = hero.baseMag + 3,
                        skillPoints = hero.skillPoints + 1
                    )
                } else {
                    hero.copy(xp = newXp)
                }
            }
        }

        _uiState.update {
            it.copy(
                currentScreen = GameScreen.VICTORY_REWARD,
                party = updatedParty,
                resources = it.resources.copy(gold = it.resources.gold + totalGold),
                inventory = it.inventory + lootList,
                gainedXp = totalXp,
                gainedGold = totalGold,
                gainedLoot = lootList,
                combatLogs = it.combatLogs + CombatLogEntry(
                    UUID.randomUUID().toString(),
                    "★ Vitória Tática! Inimigos aniquilados. +$totalXp XP, +$totalGold Ouro.",
                    LogType.LOOT
                )
            )
        }
    }

    fun continueAfterVictory() {
        _uiState.update { it.copy(currentScreen = GameScreen.EXPLORATION) }
    }

    fun retryAfterDefeat() {
        val freshParty = GameDatabase.createInitialParty()
        _uiState.update {
            it.copy(
                party = freshParty,
                currentScreen = GameScreen.HOME
            )
        }
    }

    // ==================== PARTY & GEAR MANAGEMENT ====================

    fun equipItem(heroIndex: Int, item: Item) {
        val state = _uiState.value
        val hero = state.party.getOrNull(heroIndex) ?: return

        var newWeapon = hero.equippedWeapon
        var newArmor = hero.equippedArmor
        var newAccessory = hero.equippedAccessory
        val remainingInventory = state.inventory.toMutableList()

        when (item.type) {
            ItemType.WEAPON -> {
                if (newWeapon != null) remainingInventory.add(newWeapon)
                newWeapon = item
                remainingInventory.remove(item)
            }
            ItemType.ARMOR -> {
                if (newArmor != null) remainingInventory.add(newArmor)
                newArmor = item
                remainingInventory.remove(item)
            }
            ItemType.ACCESSORY -> {
                if (newAccessory != null) remainingInventory.add(newAccessory)
                newAccessory = item
                remainingInventory.remove(item)
            }
            else -> {}
        }

        val updatedHero = hero.copy(
            equippedWeapon = newWeapon,
            equippedArmor = newArmor,
            equippedAccessory = newAccessory
        )
        val newParty = state.party.toMutableList().apply { set(heroIndex, updatedHero) }

        _uiState.update {
            it.copy(
                party = newParty,
                inventory = remainingInventory,
                activeBannerMessage = "${item.name} equipado em ${hero.name}!"
            )
        }
    }

    fun socketGem(heroIndex: Int, gem: Item) {
        val state = _uiState.value
        val hero = state.party.getOrNull(heroIndex) ?: return
        val currentWeapon = hero.equippedWeapon ?: return

        val enchantedWeapon = currentWeapon.copy(
            name = "${currentWeapon.name} (+Gema)",
            atkBonus = currentWeapon.atkBonus + gem.atkBonus,
            magBonus = currentWeapon.magBonus + gem.magBonus,
            socketedGem = gem.name
        )

        val updatedHero = hero.copy(equippedWeapon = enchantedWeapon)
        val newParty = state.party.toMutableList().apply { set(heroIndex, updatedHero) }
        val newInventory = state.inventory.toMutableList().apply { remove(gem) }

        SoundEngine.playSpell()
        _uiState.update {
            it.copy(
                party = newParty,
                inventory = newInventory,
                activeBannerMessage = "Gema ${gem.name} encrustada na ${currentWeapon.name} com sucesso!"
            )
        }
    }

    // ==================== DUNGEON MASTER MODE ====================

    fun setDmBrush(tileType: TileType) {
        _uiState.update { it.copy(dmSelectedBrush = tileType) }
    }

    fun setDmTheme(theme: EnvironmentTheme) {
        val newMap = DungeonGenerator.generateMapForTheme(theme)
        _uiState.update {
            it.copy(
                dmSelectedTheme = theme,
                dmCustomMap = newMap
            )
        }
    }

    fun paintDmTile(x: Int, y: Int) {
        val state = _uiState.value
        val brush = state.dmSelectedBrush
        val map = state.dmCustomMap

        val updatedTiles = map.tiles.map { tile ->
            if (tile.x == x && tile.y == y) {
                tile.copy(type = brush, isExplored = true, isVisible = true)
            } else tile
        }

        _uiState.update {
            it.copy(dmCustomMap = map.copy(tiles = updatedTiles))
        }
    }

    fun playCustomDmMap() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                dungeonMap = it.dmCustomMap,
                selectedTheme = it.dmSelectedTheme,
                playerDungeonX = 2,
                playerDungeonY = 2,
                currentScreen = GameScreen.EXPLORATION,
                combatLogs = listOf(
                    CombatLogEntry(
                        UUID.randomUUID().toString(),
                        "Explorando Masmorra Customizada criada no Modo Dungeon Master!",
                        LogType.SYSTEM
                    )
                )
            )
        }
        updateExplorationVision()
    }

    fun dismissBanner() {
        _uiState.update { it.copy(activeBannerMessage = null) }
    }
}
