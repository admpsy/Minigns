package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DungeonGenerator
import com.example.data.GameDatabase
import com.example.data.SaveData
import com.example.data.SaveManager
import com.example.engine.CombatEngine
import com.example.engine.Haptics
import com.example.engine.SoundEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

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

    // Exploração por turnos
    val dungeonTurn: Int = 1,
    val dungeonPatrols: List<Enemy> = emptyList(),
    val clearedEncounters: Set<String> = emptySet(),
    val bossDefeated: Boolean = false,

    // Combate tático por turnos
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
    val isBossCombat: Boolean = false,
    val combatOrigin: GameScreen = GameScreen.EXPLORATION,

    // Recompensas
    val gainedXp: Int = 0,
    val gainedGold: Int = 0,
    val gainedLoot: List<Item> = emptyList(),

    // Progresso & preferências (persistidos)
    val completedQuestIds: Set<String> = emptySet(),
    val totalVictories: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,

    // Editor do Dungeon Master
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

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val saveManager = SaveManager(application)

    /** Único job de turno ativo: evita turnos duplicados/pulados por toques rápidos. */
    private var turnJob: Job? = null

    init {
        Haptics.init(application)
        val saved = saveManager.load()
        val initialPatrols = createPatrolsForTheme(_uiState.value.selectedTheme, _uiState.value.dungeonMap)
        update {
            var s = it.copy(dungeonPatrols = initialPatrols)
            if (saved != null) {
                s = s.copy(
                    party = saved.party,
                    resources = saved.resources,
                    inventory = saved.inventory,
                    soundEnabled = saved.soundEnabled,
                    hapticsEnabled = saved.hapticsEnabled,
                    completedQuestIds = saved.completedQuestIds,
                    totalVictories = saved.totalVictories
                )
            }
            s
        }
        SoundEngine.enabled = _uiState.value.soundEnabled
        Haptics.enabled = _uiState.value.hapticsEnabled
        updateExplorationVision()
    }

    override fun onCleared() {
        turnJob?.cancel()
        saveProgress()
        super.onCleared()
    }

    // ==================== INFRA ====================

    /** Atualiza o estado limitando o histórico de logs (evita crescimento infinito de memória). */
    private inline fun update(crossinline transform: (GameUiState) -> GameUiState) {
        _uiState.update { current ->
            val next = transform(current)
            if (next.combatLogs.size > MAX_LOGS) next.copy(combatLogs = next.combatLogs.takeLast(MAX_LOGS)) else next
        }
    }

    private fun log(text: String, type: LogType) = CombatLogEntry(UUID.randomUUID().toString(), text, type)

    private fun scheduleTurn(delayMs: Long, block: suspend () -> Unit) {
        turnJob?.cancel()
        turnJob = viewModelScope.launch {
            delay(delayMs)
            block()
        }
    }

    private fun tileAt(map: DungeonMap, x: Int, y: Int): DungeonTile? {
        if (x !in 0 until map.width || y !in 0 until map.height) return null
        val byIndex = map.tiles.getOrNull(y * map.width + x)
        if (byIndex != null && byIndex.x == x && byIndex.y == y) return byIndex
        return map.tiles.find { it.x == x && it.y == y }
    }

    fun saveProgress() {
        val s = _uiState.value
        saveManager.save(
            SaveData(
                party = s.party,
                resources = s.resources,
                inventory = s.inventory,
                soundEnabled = s.soundEnabled,
                hapticsEnabled = s.hapticsEnabled,
                completedQuestIds = s.completedQuestIds,
                totalVictories = s.totalVictories
            )
        )
    }

    fun toggleSound() {
        val enabled = !_uiState.value.soundEnabled
        SoundEngine.enabled = enabled
        update { it.copy(soundEnabled = enabled, activeBannerMessage = if (enabled) "Som ativado" else "Som desativado") }
        if (enabled) SoundEngine.playLoot()
        saveProgress()
    }

    fun toggleHaptics() {
        val enabled = !_uiState.value.hapticsEnabled
        Haptics.enabled = enabled
        update { it.copy(hapticsEnabled = enabled, activeBannerMessage = if (enabled) "Vibração ativada" else "Vibração desativada") }
        if (enabled) Haptics.hit()
        saveProgress()
    }

    private fun createPatrolsForTheme(theme: EnvironmentTheme, map: DungeonMap): List<Enemy> {
        val pool = GameDatabase.getEnemiesForTheme(theme, isBossFight = false)
        if (pool.isEmpty()) return emptyList()
        val spots = listOf(Pair(9, 3), Pair(13, 8))
        return spots.mapIndexedNotNull { index, (x, y) ->
            val tile = tileAt(map, x, y)
            if (tile == null || !tile.type.isWalkable) return@mapIndexedNotNull null
            pool.getOrElse(index) { pool.first() }.copy(
                id = UUID.randomUUID().toString(),
                posX = x,
                posY = y,
                isAlive = true
            )
        }
    }

    // ==================== NAVEGAÇÃO ====================

    fun navigateTo(screen: GameScreen) {
        if (screen == GameScreen.COMBAT && _uiState.value.currentScreen != GameScreen.COMBAT) {
            // Nunca abrir a tela de combate vazia: inicia um combate de verdade.
            startCombat(isBoss = false)
            return
        }
        update { it.copy(currentScreen = screen) }
    }

    /** Botão voltar do Android. Retorna false quando o app deve seguir o comportamento padrão (sair). */
    fun handleBack(): Boolean {
        val s = _uiState.value
        when (s.currentScreen) {
            GameScreen.HOME -> return false
            GameScreen.COMBAT -> {
                update { it.copy(activeBannerMessage = "Use o botão Recuar (canto superior) para fugir do combate.") }
            }
            GameScreen.VICTORY_REWARD -> continueAfterVictory()
            GameScreen.DEFEAT -> retryAfterDefeat()
            GameScreen.PARTY_MANAGEMENT -> {
                val inDungeon = s.activeQuest != null || s.dungeonTurn > 1
                update { it.copy(currentScreen = if (inDungeon) GameScreen.EXPLORATION else GameScreen.HOME) }
            }
            else -> update { it.copy(currentScreen = GameScreen.HOME) }
        }
        return true
    }

    fun selectScenario(theme: EnvironmentTheme) {
        turnJob?.cancel()
        val quest = GameDatabase.sampleQuests.find { it.theme == theme }
            ?: GameDatabase.sampleQuests.first()
        val newMap = DungeonGenerator.generateMapForTheme(theme)
        val patrols = createPatrolsForTheme(theme, newMap)

        update {
            it.copy(
                selectedTheme = theme,
                activeQuest = quest,
                dungeonMap = newMap,
                playerDungeonX = 2,
                playerDungeonY = 2,
                dungeonTurn = 1,
                stepsTakenInDark = 0,
                torchLightRadius = 3.5f,
                dungeonPatrols = patrols,
                clearedEncounters = emptySet(),
                bossDefeated = false,
                currentScreen = GameScreen.EXPLORATION,
                activeBannerMessage = null,
                combatLogs = listOf(
                    log("O grupo adentrou ${theme.title}. Exploração por Turnos Iniciada! ${theme.ambientDescription}", LogType.SYSTEM)
                )
            )
        }
        updateExplorationVision()
    }

    // ==================== EXPLORAÇÃO POR TURNOS ====================

    /** Avança o tempo na masmorra. Retorna true se uma patrulha emboscou o grupo. */
    fun advanceDungeonTurn(turns: Int = 1, actionDescription: String = "Avanço"): Boolean {
        val state = _uiState.value
        val newTurn = state.dungeonTurn + turns
        var steps = state.stepsTakenInDark + turns
        var updatedResources = state.resources
        var torchRadius = state.torchLightRadius
        var newParty = state.party
        val map = state.dungeonMap

        // Consumo de tocha
        if (steps >= 10) {
            steps = 0
            if (updatedResources.torches > 0) {
                updatedResources = updatedResources.copy(torches = updatedResources.torches - 1)
                torchRadius = 3.5f
            } else {
                torchRadius = 1.8f
                newParty = newParty.map { hero -> hero.copy(sanity = maxOf(10, hero.sanity - 2)) }
            }
        }

        // Patrulhas errantes andam 1 passo por turno
        var ambushPatrol: Enemy? = null
        val movedPatrols = mutableListOf<Enemy>()
        for (patrol in state.dungeonPatrols) {
            if (!patrol.isAlive) {
                movedPatrols.add(patrol)
                continue
            }
            val dist = abs(patrol.posX - state.playerDungeonX) + abs(patrol.posY - state.playerDungeonY)
            if (dist <= 1) {
                if (ambushPatrol == null) ambushPatrol = patrol
                movedPatrols.add(patrol)
                continue
            }
            val validNeighbors = listOf(
                Pair(patrol.posX + 1, patrol.posY),
                Pair(patrol.posX - 1, patrol.posY),
                Pair(patrol.posX, patrol.posY + 1),
                Pair(patrol.posX, patrol.posY - 1)
            ).filter { (x, y) -> tileAt(map, x, y)?.type?.isWalkable == true }

            if (validNeighbors.isNotEmpty()) {
                val chosen = if (dist <= 6) {
                    validNeighbors.minByOrNull { (x, y) ->
                        abs(x - state.playerDungeonX) + abs(y - state.playerDungeonY)
                    } ?: validNeighbors.random()
                } else {
                    validNeighbors.random()
                }
                movedPatrols.add(patrol.copy(posX = chosen.first, posY = chosen.second))
            } else {
                movedPatrols.add(patrol)
            }
        }

        update {
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

        val ambusher = ambushPatrol ?: return false
        update {
            it.copy(
                dungeonPatrols = movedPatrols.filter { p -> p.id != ambusher.id },
                activeBannerMessage = "⚔ EMBOSCADA! A patrulha de ${ambusher.name} alcançou seu grupo no Turno #$newTurn!",
                combatLogs = it.combatLogs + log("⚔ EMBOSCADA! Inimigos errantes interceptaram o grupo no Turno #$newTurn!", LogType.ATTACK)
            )
        }
        Haptics.heavy()
        startCombat(isBoss = false, specificEnemies = listOf(ambusher))
        return true
    }

    fun waitExplorationTurn() {
        if (_uiState.value.currentScreen != GameScreen.EXPLORATION) return
        SoundEngine.playStep()
        Haptics.tick()
        val ambushed = advanceDungeonTurn(1, "Aguardar em Guarda")
        if (ambushed) return
        update {
            it.copy(
                activeBannerMessage = "Turno #${it.dungeonTurn}: Grupo aguardou em alerta.",
                combatLogs = it.combatLogs + log("Turno #${it.dungeonTurn}: O grupo manteve vigília silenciosa enquanto o tempo passava.", LogType.SYSTEM)
            )
        }
    }

    fun movePlayer(dx: Int, dy: Int) {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.EXPLORATION) return
        val newX = state.playerDungeonX + dx
        val newY = state.playerDungeonY + dy
        val map = state.dungeonMap
        val targetTile = tileAt(map, newX, newY) ?: return

        if (!targetTile.type.isWalkable && targetTile.type != TileType.DOOR && targetTile.type != TileType.SECRET_DOOR) {
            Haptics.tick()
            return
        }

        SoundEngine.playStep()
        Haptics.tick()

        var newParty = state.party
        var hazardLog: CombatLogEntry? = null
        if (targetTile.type == TileType.TRAP_SPIKE && !targetTile.isTriggered) {
            hazardLog = log("Armadilha de Espinho ativada! O grupo sofreu 12 de dano físico.", LogType.ATTACK)
            newParty = newParty.map { if (it.isAlive) it.copy(hp = maxOf(1, it.hp - 12)) else it }
            SoundEngine.playTrapTrigger()
            Haptics.heavy()
        } else if (targetTile.type == TileType.TRAP_POISON && !targetTile.isTriggered) {
            hazardLog = log("Nuvem tóxica disparada! Veneno espalhado no ar.", LogType.STATUS)
            newParty = newParty.map { if (it.isAlive) it.copy(hp = maxOf(1, it.hp - 8)) else it }
            SoundEngine.playTrapTrigger()
            Haptics.hit()
        }

        val updatedTiles = map.tiles.map {
            if (it.x == newX && it.y == newY) {
                when (it.type) {
                    TileType.DOOR, TileType.SECRET_DOOR -> it.copy(type = TileType.FLOOR, isTriggered = true)
                    TileType.TRAP_SPIKE, TileType.TRAP_POISON -> it.copy(isTriggered = true)
                    else -> it
                }
            } else it
        }

        update {
            it.copy(
                playerDungeonX = newX,
                playerDungeonY = newY,
                dungeonMap = map.copy(tiles = updatedTiles),
                party = newParty,
                combatLogs = if (hazardLog != null) it.combatLogs + hazardLog else it.combatLogs
            )
        }

        val ambushed = advanceDungeonTurn(1, "Movimento")
        if (!ambushed) checkTileEncounter(newX, newY)
    }

    private fun updateExplorationVision() {
        val state = _uiState.value
        val updatedMap = DungeonGenerator.updateFogOfWar(
            state.dungeonMap,
            state.playerDungeonX,
            state.playerDungeonY,
            state.torchLightRadius
        )
        update { it.copy(dungeonMap = updatedMap) }
    }

    private fun checkTileEncounter(x: Int, y: Int) {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.EXPLORATION) return
        val tile = tileAt(state.dungeonMap, x, y) ?: return

        if (tile.type == TileType.EXIT_STAIRS) {
            if (state.bossDefeated) {
                update { it.copy(activeBannerMessage = "O chefe desta masmorra já foi derrotado. Escolha um novo cenário no menu!") }
            } else {
                startCombat(isBoss = true)
            }
            return
        }

        val encounterKey = "${state.dungeonMap.id}:$x,$y"
        val isPresetZone = (x == 7 && y == 4) || (x == 10 && y == 5)
        if (isPresetZone && encounterKey !in state.clearedEncounters) {
            update { it.copy(clearedEncounters = it.clearedEncounters + encounterKey) }
            startCombat(isBoss = false)
        }
    }

    private fun Hero.healedOutOfCombat(hpGain: Int, mpGain: Int, sanityGain: Int): Hero {
        val newHp = minOf(maxHp, hp + hpGain)
        return copy(
            hp = newHp,
            mp = minOf(maxMp, mp + mpGain),
            sanity = minOf(maxSanity, sanity + sanityGain),
            isAlive = newHp > 0,
            statusEffects = emptyList()
        )
    }

    fun interactWithTile() {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.EXPLORATION) return
        val x = state.playerDungeonX
        val y = state.playerDungeonY
        val tile = tileAt(state.dungeonMap, x, y) ?: return

        if (tile.type.isInteractive && tile.lootClaimed) {
            update { it.copy(activeBannerMessage = "Este local já foi utilizado.") }
            return
        }

        fun claimTile(newType: TileType? = null): DungeonMap {
            val tiles = state.dungeonMap.tiles.map {
                if (it.x == x && it.y == y) it.copy(lootClaimed = true, type = newType ?: it.type) else it
            }
            return state.dungeonMap.copy(tiles = tiles)
        }

        when (tile.type) {
            TileType.CHEST -> {
                val rewardGold = (40..90).random()
                val randomLoot = GameDatabase.allLootPool.random()
                SoundEngine.playLoot()
                Haptics.success()
                val claimedMap = claimTile(TileType.FLOOR)
                update {
                    it.copy(
                        resources = it.resources.copy(gold = it.resources.gold + rewardGold),
                        inventory = it.inventory + randomLoot,
                        dungeonMap = claimedMap,
                        combatLogs = it.combatLogs + log("Baú Antigo Aberto! Encontrado: ${randomLoot.name} e $rewardGold Moedas de Ouro!", LogType.LOOT),
                        activeBannerMessage = "Baú Aberto: +$rewardGold Ouro, ${randomLoot.name}!"
                    )
                }
            }
            TileType.SARCOPHAGUS -> {
                val claimedMap = claimTile()
                val isTrap = (0..1).random() == 1
                if (isTrap) {
                    update {
                        it.copy(
                            dungeonMap = claimedMap,
                            combatLogs = it.combatLogs + log("A tampa do sarcófago se abriu... algo despertou!", LogType.ATTACK)
                        )
                    }
                    Haptics.heavy()
                    startCombat(isBoss = false)
                } else {
                    val gold = (30..70).random()
                    SoundEngine.playLoot()
                    Haptics.hit()
                    update {
                        it.copy(
                            dungeonMap = claimedMap,
                            resources = it.resources.copy(gold = it.resources.gold + gold),
                            combatLogs = it.combatLogs + log("Sarcófago inspecionado. Encontradas relíquias valiosas de $gold ouro.", LogType.LOOT),
                            activeBannerMessage = "Sarcófago Real saqueado: +$gold Ouro"
                        )
                    }
                }
            }
            TileType.ALTAR -> {
                SoundEngine.playHeal()
                Haptics.success()
                val claimedMap = claimTile()
                update {
                    it.copy(
                        dungeonMap = claimedMap,
                        party = it.party.map { h -> h.healedOutOfCombat(40, 30, 25) },
                        combatLogs = it.combatLogs + log("Altar Ancestral ativado! Graça Divina restaura vida, mana, sanidade e ergue os caídos!", LogType.HEAL),
                        activeBannerMessage = "Bênção Ancestral Recebida!"
                    )
                }
            }
            TileType.FOUNTAIN -> {
                SoundEngine.playHeal()
                Haptics.success()
                val claimedMap = claimTile()
                update {
                    it.copy(
                        dungeonMap = claimedMap,
                        party = it.party.map { h -> h.healedOutOfCombat(h.maxHp, h.maxMp, 0) },
                        combatLogs = it.combatLogs + log("Fonte de Águas Luminescentes purifica todas as feridas do grupo!", LogType.HEAL),
                        activeBannerMessage = "Vida e Mana Totalmente Restaurados!"
                    )
                }
            }
            TileType.TORCH_STAND -> {
                Haptics.tick()
                SoundEngine.playLoot()
                val claimedMap = claimTile()
                update {
                    it.copy(
                        dungeonMap = claimedMap,
                        resources = it.resources.copy(torches = it.resources.torches + 1),
                        combatLogs = it.combatLogs + log("Tocha coletada do suporte na parede.", LogType.LOOT),
                        activeBannerMessage = "+1 Tocha"
                    )
                }
            }
            else -> {
                update { it.copy(activeBannerMessage = "Nada para interagir aqui.") }
            }
        }
    }

    // ==================== DESCANSO & SOBREVIVÊNCIA ====================

    fun performShortRest() {
        val state = _uiState.value
        if (state.currentScreen == GameScreen.COMBAT) return
        if (state.resources.rations <= 0) {
            update { it.copy(activeBannerMessage = "Sem rações suficientes para um descanso curto!") }
            return
        }

        SoundEngine.playRest()
        Haptics.tick()
        update {
            it.copy(
                resources = it.resources.copy(rations = it.resources.rations - 1),
                party = it.party.map { hero ->
                    hero.healedOutOfCombat((hero.maxHp * 0.4f).toInt(), (hero.maxMp * 0.3f).toInt(), 15)
                },
                combatLogs = it.combatLogs + log("Descanso Curto realizado. Rações consumidas (-1). Vida, mana e sanidade revigorados.", LogType.SYSTEM),
                activeBannerMessage = "Descanso Curto Concluído (-1 Ração)"
            )
        }
    }

    fun useTorch() {
        val state = _uiState.value
        if (state.resources.torches > 0) {
            Haptics.tick()
            update {
                it.copy(
                    resources = it.resources.copy(torches = it.resources.torches - 1),
                    torchLightRadius = 4.5f,
                    stepsTakenInDark = 0,
                    activeBannerMessage = "Nova Tocha Acesa! Visão restaurada."
                )
            }
            updateExplorationVision()
        } else {
            update { it.copy(activeBannerMessage = "Sem tochas restantes!") }
        }
    }

    fun useHealingPotion(heroIndex: Int) {
        val state = _uiState.value
        if (state.resources.healingPotions > 0 && heroIndex in state.party.indices) {
            val target = state.party[heroIndex]
            if (target.hp >= target.maxHp && target.isAlive) {
                update { it.copy(activeBannerMessage = "${target.name} já está com a vida cheia.") }
                return
            }
            SoundEngine.playHeal()
            Haptics.tick()
            val newHp = minOf(target.maxHp, target.hp + 60)
            val updatedHero = target.copy(hp = newHp, isAlive = newHp > 0)
            update {
                it.copy(
                    resources = it.resources.copy(healingPotions = it.resources.healingPotions - 1),
                    party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                    activeBannerMessage = "Poção de Cura usada em ${target.name} (+60 HP)"
                )
            }
            saveProgress()
        }
    }

    // ==================== COMBATE TÁTICO ====================

    fun startCombat(isBoss: Boolean = false, specificEnemies: List<Enemy>? = null) {
        val state = _uiState.value
        if (state.currentScreen == GameScreen.COMBAT) return
        turnJob?.cancel()

        val rawEnemies = specificEnemies ?: GameDatabase.getEnemiesForTheme(state.selectedTheme, isBoss)
        if (rawEnemies.isEmpty()) return

        val origin = if (state.currentScreen == GameScreen.EXPLORATION) GameScreen.EXPLORATION else GameScreen.HOME

        val positionedParty = state.party.mapIndexed { index, hero ->
            hero.copy(
                combatGridX = if (index % 2 == 0) 1 else 0,
                combatGridY = index % 4,
                isGuarding = false,
                hasMovedThisTurn = false,
                ap = hero.maxAp,
                statusEffects = emptyList()
            )
        }

        val enemies = rawEnemies.mapIndexed { index, enemy ->
            enemy.copy(
                id = if (specificEnemies == null) UUID.randomUUID().toString() else enemy.id,
                combatGridX = if (index % 2 == 0) 4 else 5,
                combatGridY = index % 4,
                currentIntent = CombatEngine.generateEnemyIntent(enemy, positionedParty.filter { it.isAlive }),
                isGuarding = false,
                statusEffects = emptyList()
            )
        }

        // Iniciativa (d20 + velocidade)
        val entities = mutableListOf<CombatTurnEntity>()
        positionedParty.forEach { hero ->
            if (hero.isAlive) {
                entities.add(CombatTurnEntity(hero.id, hero.name, isHero = true, speed = (1..20).random() + hero.totalSpd, iconKey = hero.heroClass.name.lowercase()))
            }
        }
        enemies.forEach { enemy ->
            entities.add(CombatTurnEntity(enemy.id, enemy.name, isHero = false, speed = (1..20).random() + enemy.spd, iconKey = "enemy"))
        }

        val sortedInitiative = entities.sortedByDescending { it.speed }
        val firstEntity = sortedInitiative.firstOrNull() ?: return
        val firstIsHero = firstEntity.isHero
        val initialHeroIdx = if (firstIsHero) {
            positionedParty.indexOfFirst { it.id == firstEntity.id }.coerceAtLeast(0)
        } else {
            positionedParty.indexOfFirst { it.isAlive }.coerceAtLeast(0)
        }

        val initLog = "Iniciativas: " + sortedInitiative.joinToString(", ") { "${it.name.split(' ').first()}: ${it.speed}" }

        update {
            it.copy(
                currentScreen = GameScreen.COMBAT,
                combatOrigin = origin,
                isBossCombat = isBoss,
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
                    log("⚔ $initLog", LogType.SYSTEM),
                    log("Rodada 1 • Turno de ${firstEntity.name}!", LogType.SYSTEM)
                ),
                activeBannerMessage = if (firstIsHero) "Sua vez! Turno de ${firstEntity.name}." else "Turno de ${firstEntity.name}..."
            )
        }

        if (!firstIsHero) {
            scheduleTurn(600) { processSingleEnemyTurn(firstEntity) }
        }
    }

    /** Herói que realmente pode agir agora (respeita a ordem de iniciativa). */
    private fun actingHero(): Pair<Int, Hero>? {
        val s = _uiState.value
        if (s.currentScreen != GameScreen.COMBAT || s.isEnemyTurnProcessing) return null
        val entity = s.activeTurnEntity ?: return null
        if (!entity.isHero) return null
        val idx = s.party.indexOfFirst { it.id == entity.id }
        if (idx < 0) return null
        val hero = s.party[idx]
        return if (hero.isAlive) Pair(idx, hero) else null
    }

    private fun firstAliveEnemyIndex(enemies: List<Enemy>): Int? =
        enemies.indexOfFirst { it.isAlive }.takeIf { it >= 0 }

    /** Garante um alvo vivo: se o selecionado morreu, escolhe o próximo vivo. */
    private fun resolveTargetIndex(state: GameUiState): Int? {
        val selected = state.selectedEnemyIndex
        if (selected != null && state.combatEnemies.getOrNull(selected)?.isAlive == true) return selected
        return firstAliveEnemyIndex(state.combatEnemies)
    }

    fun selectEnemy(index: Int) {
        val enemy = _uiState.value.combatEnemies.getOrNull(index) ?: return
        if (!enemy.isAlive) {
            update { it.copy(activeBannerMessage = "${enemy.name} já foi derrotado.") }
            return
        }
        Haptics.tick()
        update { it.copy(selectedEnemyIndex = index) }
    }

    fun selectSkill(skill: Skill) {
        Haptics.tick()
        update { it.copy(selectedSkill = skill) }
    }

    fun selectHeroTab(index: Int) {
        val s = _uiState.value
        if (index !in s.party.indices) return
        if (s.currentScreen == GameScreen.COMBAT) {
            val acting = actingHero()
            if (acting != null && acting.first != index) {
                update { it.copy(activeBannerMessage = "Agora é o turno de ${acting.second.name} (ordem de iniciativa).") }
            }
            return
        }
        val hero = s.party[index]
        update { it.copy(activeHeroIndex = index, selectedSkill = hero.skills.firstOrNull()) }
    }

    fun dismissDiceRoll() {
        update { it.copy(activeDiceRoll = null) }
    }

    fun moveHeroOnGrid(targetX: Int, targetY: Int) {
        val state = _uiState.value
        val (heroIndex, activeHero) = actingHero() ?: return

        if (activeHero.ap <= 0 && activeHero.hasMovedThisTurn) {
            update { it.copy(activeBannerMessage = "Sem Pontos de Ação (AP) para mover!") }
            return
        }
        if (targetX !in 0..5 || targetY !in 0..3) return
        if (activeHero.combatGridX == targetX && activeHero.combatGridY == targetY) return

        val isOccupied = state.party.any { it.id != activeHero.id && it.isAlive && it.combatGridX == targetX && it.combatGridY == targetY } ||
            state.combatEnemies.any { it.isAlive && it.combatGridX == targetX && it.combatGridY == targetY }
        if (isOccupied) {
            update { it.copy(activeBannerMessage = "Posição já ocupada!") }
            return
        }

        val dist = abs(activeHero.combatGridX - targetX) + abs(activeHero.combatGridY - targetY)
        if (dist > 2) {
            update { it.copy(activeBannerMessage = "Alcance de movimento máximo: 2 quadros!") }
            return
        }

        Haptics.tick()
        val apCost = if (activeHero.hasMovedThisTurn) 1 else 0
        val updatedHero = activeHero.copy(
            combatGridX = targetX,
            combatGridY = targetY,
            hasMovedThisTurn = true,
            ap = maxOf(0, activeHero.ap - apCost)
        )
        update {
            it.copy(
                party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                combatLogs = it.combatLogs + log("${activeHero.name} moveu-se taticamente para ($targetX, $targetY).", LogType.SYSTEM)
            )
        }
        checkPassTurnIfNoAp()
    }

    fun executeGuard() {
        val acting = actingHero()
        if (acting == null || acting.second.ap <= 0) {
            update { it.copy(activeBannerMessage = "Sem AP suficiente para postura defensiva!") }
            return
        }
        val (heroIndex, activeHero) = acting

        SoundEngine.playShieldBlock()
        Haptics.hit()
        val updatedHero = activeHero.copy(
            ap = activeHero.ap - 1,
            isGuarding = true,
            statusEffects = activeHero.statusEffects + ActiveStatus(StatusEffectType.SHIELDED, 1)
        )
        update {
            it.copy(
                party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                combatLogs = it.combatLogs + log("${activeHero.name} assumiu Postura Defensiva (+6 DEF e Redução de Dano até o próximo turno)!", LogType.STATUS),
                activeBannerMessage = "${activeHero.name} em Guarda Defensiva!"
            )
        }
        checkPassTurnIfNoAp()
    }

    fun executeShove() {
        val state = _uiState.value
        val acting = actingHero()
        if (acting == null || acting.second.ap <= 0) {
            update { it.copy(activeBannerMessage = "Sem AP suficiente para empurrar!") }
            return
        }
        val (heroIndex, activeHero) = acting
        val targetIndex = resolveTargetIndex(state) ?: return
        val targetEnemy = state.combatEnemies[targetIndex]

        val dist = abs(activeHero.combatGridX - targetEnemy.combatGridX) + abs(activeHero.combatGridY - targetEnemy.combatGridY)
        if (dist > 1) {
            update { it.copy(activeBannerMessage = "Muito longe para empurrar! Aproxime-se a 1 quadro do inimigo.") }
            return
        }

        SoundEngine.playShove()
        Haptics.hit()
        val pushedX = minOf(5, targetEnemy.combatGridX + 1)
        val blockedByAlly = state.combatEnemies.any { it.id != targetEnemy.id && it.isAlive && it.combatGridX == pushedX && it.combatGridY == targetEnemy.combatGridY }
        val hitWall = targetEnemy.combatGridX >= 5 || blockedByAlly
        val newX = if (hitWall) targetEnemy.combatGridX else pushedX
        val hitTrap = !hitWall && ((newX == 2 && targetEnemy.combatGridY == 2) || (newX == 3 && targetEnemy.combatGridY == 1))

        val result = CombatEngine.shoveEnemy(activeHero, targetEnemy, hitWall = hitWall, hitTrap = hitTrap)
        val newHp = maxOf(0, targetEnemy.hp - result.damage)
        val updatedEnemy = targetEnemy.copy(combatGridX = newX, hp = newHp, isAlive = newHp > 0)
        val updatedEnemies = state.combatEnemies.toMutableList().apply { set(targetIndex, updatedEnemy) }
        val updatedHero = activeHero.copy(ap = activeHero.ap - 1)

        update {
            it.copy(
                combatEnemies = updatedEnemies,
                party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                selectedEnemyIndex = if (updatedEnemy.isAlive) targetIndex else firstAliveEnemyIndex(updatedEnemies),
                combatLogs = it.combatLogs + result.logs,
                activeDiceRoll = result.diceRoll
            )
        }

        if (updatedEnemies.none { it.isAlive }) {
            handleCombatVictory(updatedEnemies)
            return
        }
        checkPassTurnIfNoAp()
    }

    fun useCombatItem(itemKey: String) {
        val state = _uiState.value
        val acting = actingHero()
        if (acting == null || acting.second.ap <= 0) {
            update { it.copy(activeBannerMessage = "Sem AP para usar item!") }
            return
        }
        val (heroIndex, activeHero) = acting

        when (itemKey) {
            "potion" -> {
                if (state.resources.healingPotions <= 0) {
                    update { it.copy(activeBannerMessage = "Sem Poções de Cura restantes!") }
                    return
                }
                SoundEngine.playHeal()
                Haptics.tick()
                val updatedHero = activeHero.copy(hp = minOf(activeHero.maxHp, activeHero.hp + 60), ap = activeHero.ap - 1)
                update {
                    it.copy(
                        resources = it.resources.copy(healingPotions = it.resources.healingPotions - 1),
                        party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                        combatLogs = it.combatLogs + log("${activeHero.name} bebeu uma Poção de Cura (+60 HP)!", LogType.HEAL),
                        activeBannerMessage = "+60 HP restaurados!"
                    )
                }
            }
            "alchemist_fire" -> {
                val targetIndex = resolveTargetIndex(state) ?: return
                val targetEnemy = state.combatEnemies[targetIndex]
                SoundEngine.playCrit()
                Haptics.heavy()
                val dmg = 26
                val newHp = maxOf(0, targetEnemy.hp - dmg)
                val updatedEnemy = targetEnemy.copy(
                    hp = newHp,
                    isAlive = newHp > 0,
                    statusEffects = targetEnemy.statusEffects + ActiveStatus(StatusEffectType.BURNING, durationTurns = 2, magnitude = 8)
                )
                val updatedEnemies = state.combatEnemies.toMutableList().apply { set(targetIndex, updatedEnemy) }
                val updatedHero = activeHero.copy(ap = activeHero.ap - 1)

                val roll = ActiveDiceRoll(
                    d20 = 20, modifier = 6, total = 26, isCrit = true, isMiss = false,
                    advantage = false, disadvantage = false, actionName = "Fogo dos Alquimistas",
                    actorName = activeHero.name, targetName = targetEnemy.name, damage = dmg,
                    comboName = "Detonação Flamejante + Incêndio Ativo",
                    outcomeDescription = "O frasco detonou espalhando chamas vorazes!"
                )

                update {
                    it.copy(
                        combatEnemies = updatedEnemies,
                        party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                        selectedEnemyIndex = if (updatedEnemy.isAlive) targetIndex else firstAliveEnemyIndex(updatedEnemies),
                        activeDiceRoll = roll,
                        combatLogs = it.combatLogs + log("${activeHero.name} arremessou Fogo dos Alquimistas em ${targetEnemy.name} (26 Dano Flamejante + Queimadura)!", LogType.CRIT)
                    )
                }

                if (updatedEnemies.none { it.isAlive }) {
                    handleCombatVictory(updatedEnemies)
                    return
                }
            }
            "smoke_bomb" -> {
                SoundEngine.playSpell()
                Haptics.tick()
                val updatedHero = activeHero.copy(
                    ap = activeHero.ap - 1,
                    statusEffects = activeHero.statusEffects + ActiveStatus(StatusEffectType.STEALTHED, durationTurns = 2)
                )
                update {
                    it.copy(
                        party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                        combatLogs = it.combatLogs + log("${activeHero.name} detonou Bomba de Fumaça e entrou em Furtividade!", LogType.STATUS),
                        activeBannerMessage = "${activeHero.name} está Furtivo nas Sombras!"
                    )
                }
            }
            else -> return
        }
        checkPassTurnIfNoAp()
    }

    fun executeHeroAction() {
        val state = _uiState.value
        val acting = actingHero()
        if (acting == null) {
            update { it.copy(activeBannerMessage = "Aguarde o turno de um herói!") }
            return
        }
        val (heroIndex, activeHero) = acting
        val skill = state.selectedSkill?.takeIf { s -> activeHero.skills.any { it.id == s.id } }
            ?: activeHero.skills.firstOrNull() ?: return

        if (activeHero.ap < skill.apCost || activeHero.ap <= 0) {
            update { it.copy(activeBannerMessage = "Sem Pontos de Ação (AP) restantes neste turno!") }
            return
        }
        if (activeHero.mp < skill.manaCost) {
            update { it.copy(activeBannerMessage = "Mana insuficiente para ${skill.name}!") }
            return
        }

        if (skill.targetType == SkillTarget.SINGLE_ALLY || skill.targetType == SkillTarget.ALL_ALLIES || skill.targetType == SkillTarget.SELF) {
            SoundEngine.playHeal()
            Haptics.tick()
            // Cura individual vai para o aliado vivo mais ferido; em grupo, todos os vivos.
            val singleTargetId = when (skill.targetType) {
                SkillTarget.SINGLE_ALLY -> state.party.filter { it.isAlive }
                    .minByOrNull { it.hp.toFloat() / it.maxHp.coerceAtLeast(1) }?.id ?: activeHero.id
                else -> activeHero.id
            }
            val healedParty = state.party.map { hero ->
                val affected = hero.isAlive && (skill.targetType == SkillTarget.ALL_ALLIES || hero.id == singleTargetId)
                if (affected) {
                    hero.copy(
                        hp = minOf(hero.maxHp, hero.hp + skill.power),
                        statusEffects = hero.statusEffects.filter { it.type != StatusEffectType.BLESSED } + ActiveStatus(StatusEffectType.BLESSED, 2)
                    )
                } else hero
            }.toMutableList()
            val healedActor = healedParty[heroIndex]
            healedParty[heroIndex] = healedActor.copy(
                ap = (healedActor.ap - skill.apCost).coerceAtLeast(0),
                mp = healedActor.mp - skill.manaCost
            )
            val targetName = healedParty.find { it.id == singleTargetId }?.name ?: activeHero.name
            val text = if (skill.targetType == SkillTarget.ALL_ALLIES) {
                "${activeHero.name} canalizou ${skill.name} curando e abençoando o grupo (+${skill.power} HP)!"
            } else {
                "${activeHero.name} canalizou ${skill.name} em $targetName (+${skill.power} HP)!"
            }
            update {
                it.copy(
                    party = healedParty,
                    combatLogs = it.combatLogs + log(text, LogType.HEAL),
                    activeBannerMessage = text
                )
            }
        } else {
            val targetIndex = resolveTargetIndex(state) ?: return
            val primary = state.combatEnemies[targetIndex]

            // Alvos: único, todos (ALL_ENEMIES) ou área ao redor do alvo (AREA_GRID)
            val targetIndices = state.combatEnemies.indices.filter { i ->
                val e = state.combatEnemies[i]
                e.isAlive && when (skill.targetType) {
                    SkillTarget.ALL_ENEMIES -> true
                    SkillTarget.AREA_GRID -> i == targetIndex ||
                        abs(e.combatGridX - primary.combatGridX) + abs(e.combatGridY - primary.combatGridY) <= skill.aoeRadius.coerceAtLeast(1)
                    else -> i == targetIndex
                }
            }

            val updatedEnemies = state.combatEnemies.toMutableList()
            val newLogs = mutableListOf<CombatLogEntry>()
            var primaryRoll: ActiveDiceRoll? = null
            var anyCrit = false

            for (i in targetIndices) {
                val enemy = updatedEnemies[i]
                val result = CombatEngine.calculateHeroAttack(activeHero, enemy, skill, state.party)
                val damage = if (i == targetIndex) result.damage else (result.damage * 0.7f).toInt()
                val newHp = maxOf(0, enemy.hp - damage)
                updatedEnemies[i] = enemy.copy(
                    hp = newHp,
                    isAlive = newHp > 0,
                    statusEffects = if (result.appliedStatus != null) enemy.statusEffects + result.appliedStatus else enemy.statusEffects
                )
                if (i == targetIndex) {
                    primaryRoll = result.diceRoll
                    newLogs.addAll(result.logs)
                } else {
                    newLogs.add(log("${skill.name} atingiu também ${enemy.name} (-$damage HP).", LogType.SPELL))
                }
                if (result.isCrit) anyCrit = true
                if (newHp <= 0) newLogs.add(log("${enemy.name} foi derrotado!", LogType.LOOT))
            }

            SoundEngine.playDiceRoll()
            if (anyCrit) {
                SoundEngine.playCrit()
                Haptics.heavy()
            } else {
                SoundEngine.playAttack()
                Haptics.hit()
            }

            val updatedHero = activeHero.copy(
                ap = (activeHero.ap - skill.apCost).coerceAtLeast(0),
                mp = activeHero.mp - skill.manaCost,
                // Atacar revela o herói furtivo
                statusEffects = activeHero.statusEffects.filter { it.type != StatusEffectType.STEALTHED }
            )

            update {
                it.copy(
                    combatEnemies = updatedEnemies,
                    party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                    selectedEnemyIndex = if (updatedEnemies[targetIndex].isAlive) targetIndex else firstAliveEnemyIndex(updatedEnemies),
                    combatLogs = it.combatLogs + newLogs,
                    activeDiceRoll = primaryRoll
                )
            }

            if (updatedEnemies.none { it.isAlive }) {
                handleCombatVictory(updatedEnemies)
                return
            }
        }

        checkPassTurnIfNoAp()
    }

    private fun checkPassTurnIfNoAp() {
        val acting = actingHero() ?: return
        if (acting.second.ap <= 0) {
            update { it.copy(activeBannerMessage = "${acting.second.name} gastou todos os AP. Passando turno...") }
            val turnIndex = _uiState.value.currentTurnIndex
            val round = _uiState.value.combatRound
            scheduleTurn(700) {
                val s = _uiState.value
                // Só avança se ainda for o mesmo turno (o jogador pode ter passado manualmente).
                if (s.currentScreen == GameScreen.COMBAT && s.currentTurnIndex == turnIndex && s.combatRound == round) {
                    advanceToNextTurn()
                }
            }
        }
    }

    fun endPlayerTurn() {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.COMBAT || state.isEnemyTurnProcessing) return
        turnJob?.cancel()
        Haptics.tick()
        advanceToNextTurn()
    }

    /** Fuga do combate: cancela turnos pendentes e volta à origem com segurança. */
    fun retreatFromCombat() {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.COMBAT) return
        turnJob?.cancel()
        if (state.isBossCombat) {
            update { it.copy(activeBannerMessage = "Não há fuga da câmara do chefe!") }
            return
        }
        Haptics.hit()
        update {
            it.copy(
                currentScreen = it.combatOrigin,
                isEnemyTurnProcessing = false,
                activeDiceRoll = null,
                initiativeQueue = emptyList(),
                combatEnemies = emptyList(),
                party = it.party.map { h -> h.copy(isGuarding = false, statusEffects = emptyList()) },
                combatLogs = it.combatLogs + log("O grupo recuou do combate às pressas.", LogType.SYSTEM),
                activeBannerMessage = "O grupo recuou em segurança."
            )
        }
    }

    private fun tickStatuses(effects: List<ActiveStatus>): List<ActiveStatus> =
        effects.map { it.copy(durationTurns = it.durationTurns - 1) }.filter { it.durationTurns > 0 }

    private fun damageOverTime(effects: List<ActiveStatus>): Int =
        effects.filter { it.type == StatusEffectType.BURNING || it.type == StatusEffectType.POISONED }
            .sumOf { if (it.magnitude > 0) it.magnitude else 6 }

    fun advanceToNextTurn() {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.COMBAT || state.initiativeQueue.isEmpty()) return

        val aliveHeroIds = state.party.filter { it.isAlive }.map { it.id }.toSet()
        val aliveEnemyIds = state.combatEnemies.filter { it.isAlive }.map { it.id }.toSet()

        if (aliveHeroIds.isEmpty()) {
            handleCombatDefeat()
            return
        }
        if (aliveEnemyIds.isEmpty()) {
            handleCombatVictory(state.combatEnemies)
            return
        }

        val updatedQueue = state.initiativeQueue.map { entity ->
            entity.copy(isAlive = if (entity.isHero) entity.id in aliveHeroIds else entity.id in aliveEnemyIds)
        }

        var nextIdx = (state.currentTurnIndex + 1) % updatedQueue.size
        var newRound = state.combatRound
        var wrapped = false
        if (nextIdx == 0) {
            newRound++
            wrapped = true
        }
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
        val newLogs = mutableListOf<CombatLogEntry>()

        var refreshedParty = state.party
        var refreshedEnemies = state.combatEnemies

        if (wrapped) {
            newLogs.add(log("--- Início da Rodada $newRound ---", LogType.SYSTEM))

            // Heróis: renovam AP, efeitos expiram, dano contínuo (nunca mata fora do golpe inimigo)
            refreshedParty = state.party.map { hero ->
                if (!hero.isAlive) return@map hero
                val dot = damageOverTime(hero.statusEffects)
                if (dot > 0) newLogs.add(log("${hero.name} sofre $dot de dano contínuo.", LogType.STATUS))
                hero.copy(
                    ap = hero.maxAp,
                    isGuarding = false,
                    hasMovedThisTurn = false,
                    hp = if (dot > 0) maxOf(1, hero.hp - dot) else hero.hp,
                    statusEffects = tickStatuses(hero.statusEffects)
                )
            }

            // Inimigos: queimadura/veneno causam dano, congelamento e atordoamento expiram
            refreshedEnemies = state.combatEnemies.map { enemy ->
                if (!enemy.isAlive) return@map enemy
                val dot = damageOverTime(enemy.statusEffects)
                val newHp = maxOf(0, enemy.hp - dot)
                if (dot > 0) newLogs.add(log("${enemy.name} sofre $dot de dano por queimadura/veneno.", LogType.STATUS))
                if (dot > 0 && newHp <= 0) newLogs.add(log("${enemy.name} sucumbiu aos ferimentos!", LogType.LOOT))
                val ticked = enemy.copy(hp = newHp, isAlive = newHp > 0, statusEffects = tickStatuses(enemy.statusEffects), isGuarding = false)
                if (ticked.isAlive) {
                    ticked.copy(currentIntent = CombatEngine.generateEnemyIntent(ticked, refreshedParty.filter { it.isAlive }))
                } else ticked
            }

            if (refreshedEnemies.none { it.isAlive }) {
                update { it.copy(combatEnemies = refreshedEnemies, party = refreshedParty, combatLogs = it.combatLogs + newLogs) }
                handleCombatVictory(refreshedEnemies)
                return
            }
        }

        val nextIsHero = nextEntity.isHero
        val nextHeroIndex = if (nextIsHero) {
            refreshedParty.indexOfFirst { it.id == nextEntity.id }.coerceAtLeast(0)
        } else state.activeHeroIndex

        newLogs.add(log("Turno de ${nextEntity.name} (Iniciativa ${nextEntity.speed})", LogType.SYSTEM))

        val safeSelectedEnemy = state.selectedEnemyIndex?.takeIf { refreshedEnemies.getOrNull(it)?.isAlive == true }
            ?: firstAliveEnemyIndex(refreshedEnemies)

        update {
            it.copy(
                initiativeQueue = updatedQueue,
                currentTurnIndex = nextIdx,
                combatRound = newRound,
                party = refreshedParty,
                combatEnemies = refreshedEnemies,
                activeHeroIndex = nextHeroIndex,
                selectedEnemyIndex = safeSelectedEnemy,
                selectedSkill = refreshedParty.getOrNull(nextHeroIndex)?.skills?.firstOrNull(),
                isEnemyTurnProcessing = !nextIsHero,
                activeDiceRoll = null,
                combatLogs = it.combatLogs + newLogs,
                activeBannerMessage = if (nextIsHero) "Sua vez! Turno de ${nextEntity.name}." else "Turno de ${nextEntity.name}..."
            )
        }

        if (!nextIsHero) {
            scheduleTurn(700) { processSingleEnemyTurn(nextEntity) }
        }
    }

    private suspend fun processSingleEnemyTurn(enemyEntity: CombatTurnEntity) {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.COMBAT) return
        val enemy = state.combatEnemies.find { it.id == enemyEntity.id }
        if (enemy == null || !enemy.isAlive) {
            advanceToNextTurn()
            return
        }

        if (enemy.statusEffects.any { it.type == StatusEffectType.FROZEN || it.type == StatusEffectType.STUNNED }) {
            update {
                it.copy(
                    combatLogs = it.combatLogs + log("${enemy.name} está congelado/atordoado e perdeu seu turno!", LogType.STATUS),
                    activeBannerMessage = "${enemy.name} atordoado - Turno pulado!"
                )
            }
            delay(500)
            advanceToNextTurn()
            return
        }

        val currentParty = state.party.toMutableList()
        val aliveHeroes = currentParty.filter { it.isAlive }
        if (aliveHeroes.isEmpty()) {
            handleCombatDefeat()
            return
        }

        // Heróis furtivos são ignorados (se houver outro alvo); provocação força o Guerreiro.
        val visibleHeroes = aliveHeroes.filter { h -> h.statusEffects.none { it.type == StatusEffectType.STEALTHED } }
            .ifEmpty { aliveHeroes }
        val isTaunted = enemy.statusEffects.any { it.type == StatusEffectType.TAUNTED }
        val targetHero = (if (isTaunted) visibleHeroes.firstOrNull { it.heroClass == HeroClass.WARRIOR } else null)
            ?: visibleHeroes.find { it.name == enemy.currentIntent?.targetHeroName }
            ?: visibleHeroes.find { it.heroClass == HeroClass.WARRIOR }
            ?: visibleHeroes.random()

        val targetIndex = currentParty.indexOfFirst { it.id == targetHero.id }
        if (targetIndex < 0) {
            advanceToNextTurn()
            return
        }

        val attackRes = CombatEngine.calculateEnemyAttack(enemy, targetHero)
        SoundEngine.playDiceRoll()
        SoundEngine.playAttack()
        if (attackRes.damage >= targetHero.maxHp / 5) Haptics.heavy() else if (attackRes.damage > 0) Haptics.hit()

        val newHeroHp = maxOf(0, targetHero.hp - attackRes.damage)
        currentParty[targetIndex] = targetHero.copy(hp = newHeroHp, isAlive = newHeroHp > 0)
        val fallenLog = if (newHeroHp <= 0) listOf(log("${targetHero.name} caiu em combate!", LogType.ATTACK)) else emptyList()

        update {
            it.copy(
                party = currentParty.toList(),
                combatLogs = it.combatLogs + attackRes.logs + fallenLog,
                activeDiceRoll = attackRes.diceRoll,
                activeBannerMessage = "${enemy.name} agiu contra ${targetHero.name} (-${attackRes.damage} HP)!"
            )
        }

        delay(900)

        if (currentParty.none { it.isAlive }) {
            handleCombatDefeat()
            return
        }
        advanceToNextTurn()
    }

    private fun handleCombatDefeat() {
        turnJob?.cancel()
        SoundEngine.playTrapTrigger()
        Haptics.heavy()
        update {
            it.copy(
                currentScreen = GameScreen.DEFEAT,
                isEnemyTurnProcessing = false,
                activeDiceRoll = null,
                initiativeQueue = emptyList()
            )
        }
    }

    private fun handleCombatVictory(defeatedEnemies: List<Enemy>) {
        turnJob?.cancel()
        val state = _uiState.value
        if (state.currentScreen != GameScreen.COMBAT) return

        SoundEngine.playLevelUp()
        Haptics.success()

        val quest = state.activeQuest
        val questCompleted = state.isBossCombat && quest != null && quest.id !in state.completedQuestIds
        val totalXp = defeatedEnemies.sumOf { it.xpReward } + if (questCompleted) quest!!.rewardXp else 0
        val totalGold = defeatedEnemies.sumOf { it.goldReward } + if (questCompleted) quest!!.rewardGold else 0
        val randomDrop = if (state.isBossCombat || (0..100).random() > 40) GameDatabase.allLootPool.random() else null
        val lootList = listOfNotNull(randomDrop)

        val updatedParty = state.party.map { hero ->
            // Caídos recuperam a consciência com 20% da vida, sem XP desta batalha.
            if (!hero.isAlive) {
                return@map hero.copy(isAlive = true, hp = maxOf(1, hero.maxHp / 5), isGuarding = false, statusEffects = emptyList())
            }
            val newXp = hero.xp + totalXp
            val base = hero.copy(isGuarding = false, statusEffects = emptyList())
            if (newXp >= hero.maxXp && hero.level < 20) {
                base.copy(
                    level = hero.level + 1,
                    xp = newXp - hero.maxXp,
                    maxXp = (hero.maxXp * 1.5f).toInt(),
                    maxHp = hero.maxHp + 15,
                    hp = hero.maxHp + 15,
                    mp = hero.maxMp,
                    baseAtk = hero.baseAtk + 3,
                    baseDef = hero.baseDef + 2,
                    baseMag = hero.baseMag + 3,
                    skillPoints = hero.skillPoints + 1
                )
            } else {
                base.copy(xp = newXp)
            }
        }

        val victoryText = if (questCompleted) {
            "★ MISSÃO CONCLUÍDA: ${quest!!.title}! +$totalXp XP, +$totalGold Ouro."
        } else {
            "★ Vitória Tática! Inimigos aniquilados. +$totalXp XP, +$totalGold Ouro."
        }

        update {
            it.copy(
                currentScreen = GameScreen.VICTORY_REWARD,
                party = updatedParty,
                resources = it.resources.copy(gold = it.resources.gold + totalGold),
                inventory = it.inventory + lootList,
                gainedXp = totalXp,
                gainedGold = totalGold,
                gainedLoot = lootList,
                combatEnemies = defeatedEnemies,
                isEnemyTurnProcessing = false,
                activeDiceRoll = null,
                initiativeQueue = emptyList(),
                bossDefeated = it.bossDefeated || it.isBossCombat,
                completedQuestIds = if (questCompleted) it.completedQuestIds + quest!!.id else it.completedQuestIds,
                totalVictories = it.totalVictories + 1,
                combatLogs = it.combatLogs + log(victoryText, LogType.LOOT)
            )
        }
        saveProgress()
    }

    fun continueAfterVictory() {
        val s = _uiState.value
        if (s.isBossCombat) {
            update {
                it.copy(
                    currentScreen = GameScreen.HOME,
                    isBossCombat = false,
                    activeBannerMessage = "Masmorra conquistada! Escolha um novo cenário para continuar a campanha."
                )
            }
        } else {
            update { it.copy(currentScreen = it.combatOrigin) }
        }
    }

    fun retryAfterDefeat() {
        turnJob?.cancel()
        val theme = _uiState.value.selectedTheme
        val freshMap = DungeonGenerator.generateMapForTheme(theme)
        update {
            it.copy(
                party = GameDatabase.createInitialParty(),
                currentScreen = GameScreen.HOME,
                dungeonMap = freshMap,
                dungeonPatrols = createPatrolsForTheme(theme, freshMap),
                playerDungeonX = 2,
                playerDungeonY = 2,
                dungeonTurn = 1,
                stepsTakenInDark = 0,
                torchLightRadius = 3.5f,
                clearedEncounters = emptySet(),
                bossDefeated = false,
                isBossCombat = false,
                combatEnemies = emptyList(),
                initiativeQueue = emptyList(),
                isEnemyTurnProcessing = false,
                activeDiceRoll = null,
                activeBannerMessage = "Um novo grupo de aventureiros se reúne na taverna..."
            )
        }
        updateExplorationVision()
        saveProgress()
    }

    // ==================== GRUPO & EQUIPAMENTOS ====================

    fun equipItem(heroIndex: Int, item: Item) {
        val state = _uiState.value
        val hero = state.party.getOrNull(heroIndex) ?: return
        if (item !in state.inventory) return

        var newWeapon = hero.equippedWeapon
        var newArmor = hero.equippedArmor
        var newAccessory = hero.equippedAccessory
        val remainingInventory = state.inventory.toMutableList()

        when (item.type) {
            ItemType.WEAPON -> {
                newWeapon?.let { remainingInventory.add(it) }
                newWeapon = item
            }
            ItemType.ARMOR -> {
                newArmor?.let { remainingInventory.add(it) }
                newArmor = item
            }
            ItemType.ACCESSORY -> {
                newAccessory?.let { remainingInventory.add(it) }
                newAccessory = item
            }
            else -> {
                update { it.copy(activeBannerMessage = "${item.name} não pode ser equipado.") }
                return
            }
        }
        remainingInventory.remove(item)

        Haptics.tick()
        val updatedHero = hero.copy(equippedWeapon = newWeapon, equippedArmor = newArmor, equippedAccessory = newAccessory)
        update {
            it.copy(
                party = it.party.toMutableList().apply { set(heroIndex, updatedHero) },
                inventory = remainingInventory,
                activeBannerMessage = "${item.name} equipado em ${hero.name}!"
            )
        }
        saveProgress()
    }

    fun socketGem(heroIndex: Int, gem: Item) {
        val state = _uiState.value
        val hero = state.party.getOrNull(heroIndex) ?: return
        val currentWeapon = hero.equippedWeapon
        if (currentWeapon == null) {
            update { it.copy(activeBannerMessage = "${hero.name} precisa de uma arma equipada para encrustar gemas.") }
            return
        }
        if (currentWeapon.socketedGem != null) {
            update { it.copy(activeBannerMessage = "${currentWeapon.name} já possui uma gema encrustada.") }
            return
        }
        if (gem !in state.inventory) return

        val enchantedWeapon = currentWeapon.copy(
            name = "${currentWeapon.name} (+Gema)",
            atkBonus = currentWeapon.atkBonus + gem.atkBonus,
            magBonus = currentWeapon.magBonus + gem.magBonus,
            critBonus = currentWeapon.critBonus + gem.critBonus,
            socketedGem = gem.name
        )

        SoundEngine.playSpell()
        Haptics.success()
        update {
            it.copy(
                party = it.party.toMutableList().apply { set(heroIndex, hero.copy(equippedWeapon = enchantedWeapon)) },
                inventory = it.inventory.toMutableList().apply { remove(gem) },
                activeBannerMessage = "Gema ${gem.name} encrustada na ${currentWeapon.name} com sucesso!"
            )
        }
        saveProgress()
    }

    // ==================== MODO DUNGEON MASTER ====================

    fun setDmBrush(tileType: TileType) {
        update { it.copy(dmSelectedBrush = tileType) }
    }

    fun setDmTheme(theme: EnvironmentTheme) {
        val newMap = DungeonGenerator.generateMapForTheme(theme)
        update { it.copy(dmSelectedTheme = theme, dmCustomMap = newMap) }
    }

    fun paintDmTile(x: Int, y: Int) {
        val state = _uiState.value
        val brush = state.dmSelectedBrush
        val map = state.dmCustomMap
        val current = tileAt(map, x, y) ?: return
        if (current.type == brush) return
        Haptics.tick()
        val updatedTiles = map.tiles.map { tile ->
            if (tile.x == x && tile.y == y) tile.copy(type = brush, isExplored = true, isVisible = true) else tile
        }
        update { it.copy(dmCustomMap = map.copy(tiles = updatedTiles)) }
    }

    fun playCustomDmMap() {
        turnJob?.cancel()
        val state = _uiState.value
        // Garante que o ponto de partida seja caminhável e remove a névoa anterior.
        val spawnSafeTiles = state.dmCustomMap.tiles.map { tile ->
            val reset = tile.copy(isExplored = false, isVisible = false)
            if (tile.x == 2 && tile.y == 2 && !tile.type.isWalkable) reset.copy(type = TileType.FLOOR) else reset
        }
        val playableMap = state.dmCustomMap.copy(id = UUID.randomUUID().toString(), tiles = spawnSafeTiles, theme = state.dmSelectedTheme)

        update {
            it.copy(
                dungeonMap = playableMap,
                selectedTheme = it.dmSelectedTheme,
                activeQuest = null,
                playerDungeonX = 2,
                playerDungeonY = 2,
                dungeonTurn = 1,
                stepsTakenInDark = 0,
                torchLightRadius = 3.5f,
                dungeonPatrols = createPatrolsForTheme(it.dmSelectedTheme, playableMap),
                clearedEncounters = emptySet(),
                bossDefeated = false,
                currentScreen = GameScreen.EXPLORATION,
                activeBannerMessage = null,
                combatLogs = listOf(log("Explorando Masmorra Customizada criada no Modo Dungeon Master!", LogType.SYSTEM))
            )
        }
        updateExplorationVision()
    }

    fun dismissBanner() {
        update { it.copy(activeBannerMessage = null) }
    }

    companion object {
        private const val MAX_LOGS = 80
    }
}
