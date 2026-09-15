package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.SoundEngine
import com.example.ui.screens.*
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Jogo de tema escuro: ícones claros nas barras do sistema, sempre.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    // Coleta pausada automaticamente quando o app está em segundo plano (economia de bateria).
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    // Botão/gesto voltar do Android navega dentro do jogo; só sai do app a partir do menu.
                    BackHandler(enabled = state.currentScreen != GameScreen.HOME) {
                        viewModel.handleBack()
                    }

                    // Mantém a tela ligada durante exploração e combate.
                    val view = LocalView.current
                    val keepScreenOn = state.currentScreen == GameScreen.EXPLORATION || state.currentScreen == GameScreen.COMBAT
                    DisposableEffect(keepScreenOn) {
                        view.keepScreenOn = keepScreenOn
                        onDispose { view.keepScreenOn = false }
                    }

                    Crossfade(
                        targetState = state.currentScreen,
                        animationSpec = tween(durationMillis = 180),
                        label = "screen"
                    ) { screen ->
                        when (screen) {
                            GameScreen.HOME -> {
                                HomeScreen(
                                    state = state,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onSelectScenario = { viewModel.selectScenario(it) },
                                    onUseTorch = { viewModel.useTorch() },
                                    onRest = { viewModel.performShortRest() },
                                    onToggleSound = { viewModel.toggleSound() },
                                    onToggleHaptics = { viewModel.toggleHaptics() },
                                    onDismissBanner = { viewModel.dismissBanner() },
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
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
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onRetreat = { viewModel.retreatFromCombat() }
                                )
                            }
                            GameScreen.PARTY_MANAGEMENT -> {
                                PartyManagementScreen(
                                    state = state,
                                    onEquipItem = { heroIndex, item -> viewModel.equipItem(heroIndex, item) },
                                    onSocketGem = { heroIndex, gem -> viewModel.socketGem(heroIndex, gem) },
                                    onUsePotion = { heroIndex -> viewModel.useHealingPotion(heroIndex) },
                                    onBack = { viewModel.handleBack() }
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
                                    onContinue = { viewModel.continueAfterVictory() },
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                )
                            }
                            GameScreen.DEFEAT -> {
                                DefeatScreen(
                                    onRetry = { viewModel.retryAfterDefeat() },
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        // App foi para segundo plano: salva o progresso e libera o áudio nativo.
        viewModel.saveProgress()
        SoundEngine.release()
        super.onStop()
    }
}
