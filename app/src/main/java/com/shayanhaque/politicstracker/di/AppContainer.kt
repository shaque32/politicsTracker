package com.shayanhaque.politicstracker.di

import android.content.Context
import com.shayanhaque.politicstracker.BuildConfig
import com.shayanhaque.politicstracker.data.local.MarketDao
import com.shayanhaque.politicstracker.data.local.MarketDatabase
import com.shayanhaque.politicstracker.data.remote.FakeMarketRemoteDataSource
import com.shayanhaque.politicstracker.data.remote.MarketRemoteDataSource
import com.shayanhaque.politicstracker.data.remote.NetworkModule
import com.shayanhaque.politicstracker.data.remote.PolymarketRemoteDataSource
import com.shayanhaque.politicstracker.data.repository.MarketRepository
import com.shayanhaque.politicstracker.data.repository.MarketRepositoryImpl

/**
 * Manual DI container. Chosen over Hilt to keep the project dependency-light
 * and easy to read end-to-end — every wire is explicit in this one file.
 * If the project grows, migrating to Hilt is mechanical: these fields become
 * @Provides methods.
 *
 * [useFakeData] short-circuits Retrofit for a hardcoded sample catalog. Set
 * this to true when demoing offline or when the upstream API is flaky.
 */
class AppContainer(
    context: Context,
    private val useFakeData: Boolean = false,
) {
    private val appContext = context.applicationContext

    private val database: MarketDatabase by lazy { MarketDatabase.create(appContext) }
    private val dao: MarketDao by lazy { database.marketDao() }

    private val remote: MarketRemoteDataSource by lazy {
        if (useFakeData) {
            FakeMarketRemoteDataSource()
        } else {
            val client = NetworkModule.buildOkHttpClient(debug = BuildConfig.DEBUG)
            val moshi = NetworkModule.buildMoshi()
            val retrofit = NetworkModule.buildRetrofit(client, moshi)
            PolymarketRemoteDataSource(NetworkModule.buildApi(retrofit))
        }
    }

    val repository: MarketRepository by lazy { MarketRepositoryImpl(remote, dao) }
}
