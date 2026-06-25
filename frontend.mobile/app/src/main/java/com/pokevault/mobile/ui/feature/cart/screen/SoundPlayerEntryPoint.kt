package com.pokevault.mobile.ui.feature.cart.screen

import com.pokevault.mobile.util.PurchaseSoundPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint de Hilt que permite acceder a [PurchaseSoundPlayer] desde un Composable
 * sin necesidad de pasarlo como parámetro ni inyectarlo en el ViewModel.
 *
 * Uso:
 * ```
 * val player = EntryPointAccessors
 *     .fromApplication(context.applicationContext, SoundPlayerEntryPoint::class.java)
 *     .purchaseSoundPlayer()
 * ```
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundPlayerEntryPoint {
    fun purchaseSoundPlayer(): PurchaseSoundPlayer
}
