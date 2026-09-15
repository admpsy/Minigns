package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    val state by viewModel.uiState.collectAsState()

                    when (state.currentScreen) {
                        GameScreen.HOME -> {
                            HomeScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelectScenario = { viewModel.selectScenario(it) },
                                onUseTorch = { viewModel.useTorch() },
                                onRest = { viewModel.performShortRest() }
                            )
                        }
                        GameScreen.SCENARIO_SELECT -> {
                            ScenarioSelectScreen(
                                onSelectScenario = { viewModel.selectScenario(it) },
                                onBack = { viewModel.navigateTo(GameScreen.HOME) }
                            )
                        }
                        GameScreen.EXPLORATION -> {
                            ExplorationScreen(
                                state = state,
                                onMove = { dx, dy -> viewModel.movePlayer(dx, dy) },
                                onInteract = { viewModel.interactWithTile() },
                                onWaitTurn = { viewModel.waitExplorationTurn() },
                                onUseTorch = { viewModel.useTorch() },
                                onRest = { viewModel.performShortRest() },
                                onStartCombat = { viewModel.startCombat(isBoss = false) },
                                onNavigate = { viewModel.navigateTo(it) },
                                onDismissBanner = { viewModel.dismissBanner() }
                            )
                        }
                        GameScreen.COMBAT -> {
                            TacticalCombatScreen(
                                state = state,
                                onSelectHero = { viewModel.selectHeroTab(it) },
                                onSelectEnemy = { viewModel.selectEnemy(it) },
                                onSelectSkill = { viewModel.selectSkill(it) },
                                onExecuteAction = { viewModel.executeHeroAction() },
                                onGuard = { viewModel.executeGuard() },
                                onShove = { viewModel.executeShove() },
                                onMoveHero = { x, y -> viewModel.moveHeroOnGrid(x, y) },
                                onItemPotion = { viewModel.useCombatItem("potion") },
                                onItemFire = { viewModel.useCombatItem("alchemist_fire") },
                                onItemSmoke = { viewModel.useCombatItem("smoke_bomb") },
                                onDismissDiceRoll = { viewModel.dismissDiceRoll() },
                                onEndTurn = { viewModel.endPlayerTurn() },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                        GameScreen.PARTY_MANAGEMENT -> {
                            PartyManagementScreen(
                                state = state,
                                onEquipItem = { heroIndex, item -> viewModel.equipItem(heroIndex, item) },
                                onSocketGem = { heroIndex, gem -> viewModel.socketGem(heroIndex, gem) },
                                onUsePotion = { heroIndex -> viewModel.useHealingPotion(heroIndex) },
                                onBack = { viewModel.navigateTo(GameScreen.HOME) }
                            )
                        }
                        GameScreen.DUNGEON_MASTER -> {
                            DungeonMasterScreen(
                                state = state,
                                onSelectBrush = { viewModel.setDmBrush(it) },
                                onSelectTheme = { viewModel.setDmTheme(it) },
                                onPaintTile = { x, y -> viewModel.paintDmTile(x, y) },
                                onPlayCustomMap = { viewModel.playCustomDmMap() },
                                onBack = { viewModel.navigateTo(GameScreen.HOME) }
                            )
                        }
                        GameScreen.CODEX -> {
                            CodexScreen(
                                onBack = { viewModel.navigateTo(GameScreen.HOME) }
                            )
                        }
                        GameScreen.VICTORY_REWARD -> {
                            VictoryRewardScreen(
                                state = state,
                                onContinue = { viewModel.continueAfterVictory() }
                            )
                        }
                        GameScreen.DEFEAT -> {
                            DefeatScreen(
                                onRetry = { viewModel.retryAfterDefeat() }
                            )
                        }
                    }
                }
            }
        }
    }
}
