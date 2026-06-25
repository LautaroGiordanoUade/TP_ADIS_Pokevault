package com.pokevault.mobile.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.pokevault.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reproduce efectos de sonido cortos usando [SoundPool], la API recomendada por Android
 * para sonidos de baja latencia (< 1 s), a diferencia de [android.media.MediaPlayer]
 * que está pensado para música o audio largo.
 *
 * Es un @Singleton para que el sonido quede precargado en memoria y la reproducción
 * sea inmediata. El ciclo de vida está atado al proceso de la aplicación: Android libera
 * los recursos nativos del SoundPool automáticamente cuando el proceso muere.
 *
 * ### ¿Por qué AtomicBoolean para [isLoaded]?
 * [SoundPool.load] es **asíncrono**: retorna un soundId > 0 de inmediato, pero el audio
 * todavía no está decodificado en memoria. Si [play] se llama antes de que el listener
 * dispare, SoundPool falla silenciosamente. El [AtomicBoolean] es thread-safe y evita
 * reproducción prematura sin necesidad de synchronized ni locks.
 */
@Singleton
class PurchaseSoundPlayer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(audioAttributes)
        .build()

    /**
     * true únicamente cuando SoundPool termina de decodificar el archivo en memoria.
     * AtomicBoolean porque el callback de OnLoadCompleteListener puede llegar en un
     * hilo distinto al Main.
     */
    private val isLoaded = AtomicBoolean(false)
    private val soundId: Int

    init {
        // Registrar el listener ANTES de llamar a load() para no perder la callback
        // si la carga fuera instantánea (poco probable pero posible en algunos devices).
        soundPool.setOnLoadCompleteListener { _, loadedId, status ->
            if (loadedId == soundId && status == 0) {
                isLoaded.set(true)
            }
        }
        soundId = soundPool.load(context, R.raw.purchase_success, 1)
    }

    /**
     * Reproduce el sonido de compra exitosa.
     * Si el audio todavía no terminó de cargar, no hace nada — no bloquea ni lanza
     * excepción. En la práctica, el usuario tarda varios segundos en completar una
     * compra desde que abre la app, por lo que el audio siempre estará listo.
     */
    fun play() {
        if (isLoaded.get()) {
            soundPool.play(
                soundId,
                /* leftVolume  = */ 1f,
                /* rightVolume = */ 1f,
                /* priority    = */ 1,
                /* loop        = */ 0,  // sin loop
                /* rate        = */ 1f, // velocidad normal
            )
        }
    }
}
