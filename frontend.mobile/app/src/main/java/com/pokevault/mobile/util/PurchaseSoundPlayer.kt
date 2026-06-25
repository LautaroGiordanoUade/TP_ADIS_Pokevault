package com.pokevault.mobile.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.pokevault.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reproduce efectos de sonido cortos usando [SoundPool], que es la API recomendada
 * por Android para sonidos de baja latencia (< 1 s), a diferencia de MediaPlayer
 * que está pensado para reproducción de música o audio largo.
 *
 * Es un @Singleton para que el sonido quede precargado en memoria y la reproducción
 * sea inmediata. Se libera automáticamente cuando el proceso termina.
 */
@Singleton
class PurchaseSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(audioAttributes)
        .build()

    /** ID del sonido una vez cargado en el pool; -1 = todavía cargando. */
    private var soundId: Int = soundPool.load(context, R.raw.purchase_success, 1)

    /**
     * Reproduce el sonido de compra exitosa.
     * Si el sonido todavía no terminó de cargar, simplemente no hace nada
     * (no bloquea ni lanza excepción).
     */
    fun play() {
        if (soundId > 0) {
            soundPool.play(
                soundId,
                /* leftVolume  = */ 1f,
                /* rightVolume = */ 1f,
                /* priority    = */ 1,
                /* loop        = */ 0,   // sin loop
                /* rate        = */ 1f,  // velocidad normal
            )
        }
    }

    /** Libera los recursos del SoundPool. Llamar solo al destruir el proceso. */
    fun release() {
        soundPool.release()
    }
}
