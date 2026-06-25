package com.pokevault.mobile.di

import android.content.Context
import androidx.room.Room
import com.pokevault.mobile.BuildConfig
import com.pokevault.mobile.data.local.PokeMarketDatabase
import com.pokevault.mobile.data.remote.AuthApi
import com.pokevault.mobile.data.remote.AuthInterceptor
import com.pokevault.mobile.data.remote.OrderApi
import com.pokevault.mobile.data.remote.PokemonApi
import com.pokevault.mobile.data.remote.VaultApi
import com.pokevault.mobile.data.repository.DefaultOrderRepository
import com.pokevault.mobile.data.repository.DefaultPokemonRepository
import com.pokevault.mobile.data.repository.DefaultProfileRepository
import com.pokevault.mobile.data.repository.InMemoryCartRepository
import com.pokevault.mobile.domain.repository.CartRepository
import com.pokevault.mobile.domain.repository.OrderRepository
import com.pokevault.mobile.domain.repository.PokemonRepository
import com.pokevault.mobile.domain.repository.ProfileRepository
import com.pokevault.mobile.ui.feature.pickup.location.AndroidPickupLocationClient
import com.pokevault.mobile.ui.feature.pickup.location.PickupLocationClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindPokemonRepository(repository: DefaultPokemonRepository): PokemonRepository
    @Binds abstract fun bindCartRepository(repository: InMemoryCartRepository): CartRepository
    @Binds abstract fun bindProfileRepository(repository: DefaultProfileRepository): ProfileRepository
    @Binds abstract fun bindOrderRepository(repository: DefaultOrderRepository): OrderRepository
    @Binds abstract fun bindPickupLocationClient(repository: AndroidPickupLocationClient): PickupLocationClient
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokeMarketDatabase =
        Room.databaseBuilder(context, PokeMarketDatabase::class.java, "pokemarket.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePokemonDao(database: PokeMarketDatabase) = database.pokemonDao()

    @Provides
    fun provideOrderDao(database: PokeMarketDatabase) = database.orderDao()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    /**
     * Instancia de Retrofit compartida por todas las APIs del proyecto.
     * La baseUrl, el cliente HTTP y el conversor se configuran una única vez aquí.
     */
    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun providePokemonApi(retrofit: Retrofit): PokemonApi =
        retrofit.create(PokemonApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideVaultApi(retrofit: Retrofit): VaultApi =
        retrofit.create(VaultApi::class.java)

    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi =
        retrofit.create(OrderApi::class.java)
}

