package com.shayanhaque.politicstracker.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Single-responsibility builder for the Retrofit stack. Kept as a plain
 * object because the app uses manual DI via [com.shayanhaque.politicstracker.di.AppContainer];
 * if the project graduates to Hilt, these can be swapped for @Provides
 * methods without touching callers.
 */
object NetworkModule {

    private const val BASE_URL = "https://gamma-api.polymarket.com/"

    fun buildOkHttpClient(debug: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun buildMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun buildRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun buildApi(retrofit: Retrofit): PolymarketApi = retrofit.create(PolymarketApi::class.java)
}
