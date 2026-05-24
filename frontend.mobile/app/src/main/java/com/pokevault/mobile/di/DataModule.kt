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
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.data.repository.DefaultOrderRepository
import com.pokevault.mobile.data.repository.DefaultPokemonRepository
import com.pokevault.mobile.data.repository.DefaultProfileRepository
import com.pokevault.mobile.data.repository.InMemoryCartRepository
import com.pokevault.mobile.data.repository.OrderRepository
import com.pokevault.mobile.data.repository.PokemonRepository
import com.pokevault.mobile.data.repository.ProfileRepository
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

    @Provides
    @Singleton
    fun providePokemonApi(moshi: Moshi, okHttpClient: OkHttpClient): PokemonApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PokemonApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(moshi: Moshi, okHttpClient: OkHttpClient): AuthApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideVaultApi(moshi: Moshi, okHttpClient: OkHttpClient): VaultApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VaultApi::class.java)

    @Provides
    @Singleton
    fun provideOrderApi(moshi: Moshi, okHttpClient: OkHttpClient): OrderApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OrderApi::class.java)
}
