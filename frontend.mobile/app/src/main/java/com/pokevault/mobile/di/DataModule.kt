package com.pokevault.mobile.di

import android.content.Context
import androidx.room.Room
import com.pokevault.mobile.BuildConfig
import com.pokevault.mobile.data.local.PokeMarketDatabase
import com.pokevault.mobile.data.remote.PokemonApi
import com.pokevault.mobile.data.repository.CartRepository
import com.pokevault.mobile.data.repository.DefaultPokemonRepository
import com.pokevault.mobile.data.repository.DefaultProfileRepository
import com.pokevault.mobile.data.repository.InMemoryCartRepository
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
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokeMarketDatabase =
        Room.databaseBuilder(context, PokeMarketDatabase::class.java, "pokemarket.db").build()

    @Provides
    fun providePokemonDao(database: PokeMarketDatabase) = database.pokemonDao()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient =
        OkHttpClient.Builder()
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
}
