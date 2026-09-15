package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Efeitos sonoros leves via ToneGenerator (zero assets, zero memória extra).
 * - Criado sob demanda (não trava a abertura do app)
 * - Pode ser silenciado pelo jogador
 * - Liberado quando o app vai para segundo plano
 */
object SoundEngine {
    @Volatile
    var enabled: Boolean = true

    private var toneGenerator: ToneGenerator? = null
    private var creationFailed = false

    private fun generator(): ToneGenerator? {
        if (toneGenerator == null && !creationFailed) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {
                creationFailed = true
                null
            }
        }
        return toneGenerator
    }

    private fun tone(type: Int, durationMs: Int) {
        if (!enabled) return
        try {
            generator()?.startTone(type, durationMs)
        } catch (_: Exception) {
        }
    }

    /** Libera o recurso de áudio nativo (chamado em onStop). */
    fun release() {
        try {
            toneGenerator?.release()
        } catch (_: Exception) {
        }
        toneGenerator = null
        creationFailed = false
    }

    fun playAttack() = tone(ToneGenerator.TONE_PROP_BEEP, 80)
    fun playCrit() = tone(ToneGenerator.TONE_CDMA_HIGH_L, 160)
    fun playSpell() = tone(ToneGenerator.TONE_PROP_ACK, 120)
    fun playHeal() = tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
    fun playLoot() = tone(ToneGenerator.TONE_PROP_PROMPT, 200)
    fun playLevelUp() = tone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 300)
    fun playRest() = tone(ToneGenerator.TONE_PROP_BEEP2, 220)
    fun playDiceRoll() = tone(ToneGenerator.TONE_DTMF_D, 120)
    fun playShieldBlock() = tone(ToneGenerator.TONE_SUP_ERROR, 150)
    fun playShove() = tone(ToneGenerator.TONE_PROP_NACK, 140)
    fun playTrapTrigger() = tone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
    fun playStep() = tone(ToneGenerator.TONE_PROP_ACK, 40)
}
