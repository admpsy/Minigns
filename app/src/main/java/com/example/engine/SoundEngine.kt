package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator

object SoundEngine {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playAttack() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {}
    }

    fun playCrit() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 160)
        } catch (_: Exception) {}
    }

    fun playSpell() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (_: Exception) {}
    }

    fun playHeal() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
        } catch (_: Exception) {}
    }

    fun playLoot() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
        } catch (_: Exception) {}
    }

    fun playLevelUp() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 300)
        } catch (_: Exception) {}
    }

    fun playRest() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
        } catch (_: Exception) {}
    }

    fun playDiceRoll() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
        } catch (_: Exception) {}
    }

    fun playShieldBlock() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 150)
        } catch (_: Exception) {}
    }

    fun playShove() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 140)
        } catch (_: Exception) {}
    }

    fun playTrapTrigger() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
        } catch (_: Exception) {}
    }

    fun playStep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 40)
        } catch (_: Exception) {}
    }
}
