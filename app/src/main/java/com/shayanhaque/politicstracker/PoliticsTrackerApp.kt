package com.shayanhaque.politicstracker

import android.app.Application
import com.shayanhaque.politicstracker.di.AppContainer
import com.shayanhaque.politicstracker.work.MarketRefreshWorker

class PoliticsTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            context = this,
            // Flip this to true (or drive from a debug toggle) to demo the
            // app fully offline using the bundled sample markets.
            useFakeData = false,
        )
        MarketRefreshWorker.schedule(this)
    }
}
