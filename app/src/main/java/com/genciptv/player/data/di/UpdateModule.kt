package com.genciptv.player.data.di

import com.genciptv.player.BuildConfig
import com.genciptv.player.data.source.github.GithubApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Marks the GitHub-only OkHttp/Retrofit pair. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GithubRetrofit

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    /**
     * GitHub gets a client of its own rather than reusing (or `newBuilder()`-ing)
     * the shared one, so that any request header or auth interceptor added to
     * the Xtream/TMDb client later cannot leak credentials to github.com.
     *
     * Redirects must stay enabled: release assets on api.github.com hand off to
     * objects.githubusercontent.com, and the APK download follows that hop.
     */
    @Provides @Singleton @GithubRetrofit
    fun provideGithubOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            // Per-read timeout, not whole-call — a large APK may stream well
            // past this without tripping it.
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        return builder.build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides @Singleton @GithubRetrofit
    fun provideGithubRetrofit(
        @GithubRetrofit okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(GithubApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideGithubApi(@GithubRetrofit retrofit: Retrofit): GithubApi =
        retrofit.create(GithubApi::class.java)
}
