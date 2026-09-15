package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.GameDatabase
import com.example.engine.CombatEngine
import com.example.model.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AllScreensScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun combatState(): GameUiState {
        val party = GameDatabase.createInitialParty().mapIndexed { index, hero ->
            val gx = if (index % 2 == 0) 1 else 0
            val gy = index % 4
            hero.copy(combatGridX = gx, combatGridY = gy, ap = hero.maxAp)
        }
        val enemies = GameDatabase.getEnemiesForTheme(
            EnvironmentTheme.FORGOTTEN_CATACOMBS, isBossFight = false
        ).mapIndexed { index, enemy ->
            val gx = if (index % 2 == 0) 4 else 5
            val gy = index % 4
            enemy.copy(
                combatGridX = gx,
                combatGridY = gy,
                currentIntent = CombatEngine.generateEnemyIntent(enemy, party.filter { it.isAlive })
            )
        }
        val initiative = mutableListOf<CombatTurnEntity>()
        party.filter { it.isAlive }.forEach { hero ->
            initiative.add(CombatTurnEntity(
                id = hero.id, name = hero.name, isHero = true,
                speed = 15 + hero.totalSpd, iconKey = hero.heroClass.name.lowercase()
            ))
        }
        enemies.forEach { enemy ->
            initiative.add(CombatTurnEntity(
                id = enemy.id, name = enemy.name, isHero = false,
                speed = 12 + enemy.spd, iconKey = "enemy"
            ))
        }
        val sorted = initiative.sortedByDescending { it.speed }

        return GameUiState(
            currentScreen = GameScreen.COMBAT,
            party = party,
            combatEnemies = enemies,
            initiativeQueue = sorted,
            currentTurnIndex = 0,
            activeHeroIndex = 0,
            selectedEnemyIndex = 0,
            selectedSkill = party.first().skills.firstOrNull(),
            combatRound = 1,
            isEnemyTurnProcessing = false,
            combatGridArena = defaultArenaTiles(),
            combatLogs = listOf(
                CombatLogEntry("log1", "⚔ Combate Tático Iniciado! Iniciativas roladas.", LogType.SYSTEM),
                CombatLogEntry("log2", "Rodada 1 • Turno de ${sorted.firstOrNull()?.name ?: "—"}", LogType.SYSTEM)
            )
        )
    }

    private fun victoryState(): GameUiState {
        val loot = GameDatabase.allLootPool.take(2)
        return GameUiState(
            currentScreen = GameScreen.VICTORY_REWARD,
            gainedXp = 250,
            gainedGold = 180,
            gainedLoot = loot
        )
    }

    private fun explorationState(): GameUiState {
        val patrols = GameDatabase.getEnemiesForTheme(
            EnvironmentTheme.FORGOTTEN_CATACOMBS, isBossFight = false
        ).take(2).mapIndexed { i, e ->
            e.copy(posX = 9 + i * 4, posY = 3 + i * 5)
        }
        return GameUiState(
            currentScreen = GameScreen.EXPLORATION,
            dungeonTurn = 3,
            dungeonPatrols = patrols,
            activeBannerMessage = "Turno #3: Vigiando os corredores sombrios.",
            combatLogs = listOf(
                CombatLogEntry("e1", "O grupo adentrou Catacumbas Esquecidas.", LogType.SYSTEM),
                CombatLogEntry("e2", "Tochas acesas projetam sombras nas paredes.", LogType.LOOT)
            )
        )
    }

    @Test
    fun home_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    state = GameUiState(),
                    onNavigate = {},
                    onSelectScenario = {},
                    onUseTorch = {},
                    onRest = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home.png")
    }

    @Test
    fun scenario_select_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ScenarioSelectScreen(
                    onSelectScenario = {},
                    onBack = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/scenario_select.png")
    }

    @Test
    fun exploration_screen() {
        val state = explorationState()
        composeTestRule.setContent {
            MyApplicationTheme {
                ExplorationScreen(
                    state = state,
                    onMove = { _, _ -> },
                    onInteract = {},
                    onWaitTurn = {},
                    onUseTorch = {},
                    onRest = {},
                    onStartCombat = {},
                    onNavigate = {},
                    onDismissBanner = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/exploration.png")
    }

    @Test
    fun tactical_combat_screen() {
        val state = combatState()
        composeTestRule.setContent {
            MyApplicationTheme {
                TacticalCombatScreen(
                    state = state,
                    onSelectHero = {},
                    onSelectEnemy = {},
                    onSelectSkill = {},
                    onExecuteAction = {},
                    onGuard = {},
                    onShove = {},
                    onMoveHero = { _, _ -> },
                    onItemPotion = {},
                    onItemFire = {},
                    onItemSmoke = {},
                    onDismissDiceRoll = {},
                    onEndTurn = {},
                    onNavigate = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/combat.png")
    }

    @Test
    fun party_management_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PartyManagementScreen(
                    state = GameUiState(),
                    onEquipItem = { _, _ -> },
                    onSocketGem = { _, _ -> },
                    onUsePotion = {},
                    onBack = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/party_management.png")
    }

    @Test
    fun dungeon_master_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                DungeonMasterScreen(
                    state = GameUiState(),
                    onSelectBrush = {},
                    onSelectTheme = {},
                    onPaintTile = { _, _ -> },
                    onPlayCustomMap = {},
                    onBack = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dungeon_master.png")
    }

    @Test
    fun codex_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CodexScreen(onBack = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/codex.png")
    }

    @Test
    fun victory_screen() {
        val state = victoryState()
        composeTestRule.setContent {
            MyApplicationTheme {
                VictoryRewardScreen(state = state, onContinue = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/victory.png")
    }

    @Test
    fun defeat_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                DefeatScreen(onRetry = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/defeat.png")
    }
}
