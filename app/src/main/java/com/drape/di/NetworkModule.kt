package com.drape.di

import com.drape.BuildConfig
import com.drape.network.TryOnApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies.
 *
 * Provides Retrofit instance and API services for external API communication.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for the Virtual Try-On API.
     * TODO: Replace with actual API base URL
     */
    private const val TRYON_BASE_URL = "https://drape-vto-z5cruhbr4q-ew.a.run.app/api/"

    /**
     * Provides the API token from BuildConfig (injected by Secret Gradle Plugin).
     */
    @Provides
    @Singleton
    @TryOnApiToken
    fun provideTryOnApiToken(): String {
        return BuildConfig.TRYON_API_TOKEN
    }

    /**
     * Provides a configured OkHttpClient with logging interceptor.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.HEADERS
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    /**
     * Provides a Retrofit instance configured for the Try-On API.
     */
    @Provides
    @Singleton
    @TryOnRetrofit
    fun provideTryOnRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TRYON_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the Try-On API service.
     */
    @Provides
    @Singleton
    fun provideTryOnApiService(@TryOnRetrofit retrofit: Retrofit): TryOnApiService {
        return retrofit.create(TryOnApiService::class.java)
    }
}

/**
 * Qualifier annotation for the Try-On API token.
 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TryOnApiToken

/**
 * Qualifier annotation for the Try-On Retrofit instance.
 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TryOnRetrofit
