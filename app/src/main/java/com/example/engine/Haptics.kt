package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibração tátil otimizada para Android.
 * Usa VibrationEffect (API 26+) com pulsos curtos e cai para vibrate(ms) em aparelhos antigos.
 * Nunca lança exceção: falhas de hardware são ignoradas.
 */
object Haptics {
    @Volatile
    var enabled: Boolean = true

    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        if (vibrator != null) return
        vibrator = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.applicationContext
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Toque leve: passos e seleção. */
    fun tick() = vibrate(12L, 60)

    /** Impacto médio: acerto, armadilha. */
    fun hit() = vibrate(35L, 140)

    /** Impacto forte: crítico ou dano pesado recebido. */
    fun heavy() = vibrate(70L, 255)

    /** Padrão de vitória / level up. */
    fun success() {
        val v = vibrator ?: return
        if (!enabled) return
        try {
            if (!v.hasVibrator()) return
            val timings = longArrayOf(0, 40, 60, 40, 60, 90)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (v.hasAmplitudeControl()) {
                    v.vibrate(VibrationEffect.createWaveform(timings, intArrayOf(0, 180, 0, 180, 0, 255), -1))
                } else {
                    v.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!enabled) return
        try {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (v.hasAmplitudeControl()) amplitude.coerceIn(1, 255) else VibrationEffect.DEFAULT_AMPLITUDE
                v.vibrate(VibrationEffect.createOneShot(durationMs, amp))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }
}
