package com.example

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.model.EnvironmentTheme
import com.example.model.TileType
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/** Testes de lógica do jogo: jogam partidas completas sem interface. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameLogicTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("crypts_dungeons_save", 0).edit().clear().commit()
    }

    private fun idle(ms: Long = 2500) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))

    /** Joga o combate até o fim, sempre atacando com o herói da vez. */
    private fun playCombatToEnd(vm: GameViewModel, maxSteps: Int = 400): GameScreen {
        repeat(maxSteps) {
            val s = vm.uiState.value
            if (s.currentScreen != GameScreen.COMBAT) return s.currentScreen
            if (s.isPlayerTurn) {
                val hero = s.party[s.activeHeroIndex]
                val affordable = hero.skills.firstOrNull { it.manaCost <= hero.mp && it.apCost <= hero.ap }
                if (hero.ap > 0 && affordable != null) {
                    vm.selectSkill(affordable)
                    vm.executeHeroAction()
                } else {
                    vm.endPlayerTurn()
                }
            }
            idle()
        }
        return vm.uiState.value.currentScreen
    }

    @Test
    fun `combate rapido nunca abre tela vazia e termina`() {
        val vm = GameViewModel(app)
        vm.navigateTo(GameScreen.COMBAT)
        val s = vm.uiState.value
        assertEquals(GameScreen.COMBAT, s.currentScreen)
        assertTrue("deve haver inimigos", s.combatEnemies.isNotEmpty())
        assertTrue("deve haver iniciativa", s.initiativeQueue.isNotEmpty())

        val end = playCombatToEnd(vm)
        assertTrue("combate deve terminar em vitória ou derrota, terminou em $end",
            end == GameScreen.VICTORY_REWARD || end == GameScreen.DEFEAT)
    }

    @Test
    fun `varios combates seguidos sem travar turnos`() {
        val vm = GameViewModel(app)
        repeat(5) { round ->
            vm.navigateTo(GameScreen.COMBAT)
            val end = playCombatToEnd(vm)
            assertTrue("combate $round travou em $end", end == GameScreen.VICTORY_REWARD || end == GameScreen.DEFEAT)
            if (end == GameScreen.VICTORY_REWARD) {
                assertTrue("herói caído deve levantar após vitória", vm.uiState.value.party.all { it.isAlive })
                vm.continueAfterVictory()
            } else {
                vm.retryAfterDefeat()
            }
            idle()
            assertFalse(vm.uiState.value.currentScreen == GameScreen.COMBAT)
        }
    }

    @Test
    fun `nao da para agir com heroi fora da ordem de iniciativa`() {
        val vm = GameViewModel(app)
        vm.navigateTo(GameScreen.COMBAT)
        // espera até ser turno de um herói
        repeat(30) { if (!vm.uiState.value.isPlayerTurn) idle() }
        val s = vm.uiState.value
        if (!s.isPlayerTurn || s.currentScreen != GameScreen.COMBAT) return
        val acting = s.activeHeroIndex
        val other = s.party.indices.first { it != acting }
        vm.selectHeroTab(other)
        assertEquals(acting, vm.uiState.value.activeHeroIndex)
    }

    @Test
    fun `passar turno rapidamente nao pula turnos`() {
        val vm = GameViewModel(app)
        vm.navigateTo(GameScreen.COMBAT)
        repeat(30) { if (!vm.uiState.value.isPlayerTurn) idle() }
        val before = vm.uiState.value
        if (!before.isPlayerTurn) return
        vm.endPlayerTurn()
        vm.endPlayerTurn() // segundo toque deve ser ignorado ou afetar apenas o próximo herói
        val after = vm.uiState.value
        val queueSize = before.initiativeQueue.size
        val advanced = (after.currentTurnIndex - before.currentTurnIndex + queueSize) % queueSize
        assertTrue("avançou $advanced turnos com toque duplo", advanced in 1..2)
        assertTrue(after.combatLogs.size <= 80)
    }

    @Test
    fun `progresso e preferencias sao salvos e restaurados`() {
        val vm = GameViewModel(app)
        vm.toggleSound()
        vm.navigateTo(GameScreen.COMBAT)
        playCombatToEnd(vm)
        vm.saveProgress()
        val saved = vm.uiState.value

        val restored = GameViewModel(app).uiState.value
        assertEquals(saved.resources.gold, restored.resources.gold)
        assertEquals(saved.party.map { it.level }, restored.party.map { it.level })
        assertEquals(saved.party.map { it.xp }, restored.party.map { it.xp })
        assertEquals(saved.inventory.map { it.id }, restored.inventory.map { it.id })
        assertFalse(restored.soundEnabled)
        assertTrue(restored.party.all { it.skills.isNotEmpty() })
    }

    @Test
    fun `objetos do mapa so podem ser usados uma vez`() {
        val vm = GameViewModel(app)
        vm.selectScenario(EnvironmentTheme.FORGOTTEN_CATACOMBS)
        val s = vm.uiState.value
        val torchBefore = s.resources.torches
        // coloca o jogador sobre um suporte de tocha, se o mapa tiver um
        val stand = s.dungeonMap.tiles.firstOrNull { it.type == TileType.TORCH_STAND } ?: return
        val field = GameViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<com.example.viewmodel.GameUiState>
        flow.value = flow.value.copy(playerDungeonX = stand.x, playerDungeonY = stand.y)
        vm.interactWithTile()
        vm.interactWithTile()
        vm.interactWithTile()
        assertEquals(torchBefore + 1, vm.uiState.value.resources.torches)
    }

    @Test
    fun `botao voltar navega dentro do jogo`() {
        val vm = GameViewModel(app)
        assertFalse(vm.handleBack()) // no menu: deixa o Android sair
        vm.navigateTo(GameScreen.CODEX)
        assertTrue(vm.handleBack())
        assertEquals(GameScreen.HOME, vm.uiState.value.currentScreen)
    }
}
