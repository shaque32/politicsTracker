package com.shayanhaque.politicstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.shayanhaque.politicstracker.ui.navigation.PoliticsTrackerNavHost
import com.shayanhaque.politicstracker.ui.theme.PoliticsTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val container = (application as PoliticsTrackerApp).container

        setContent {
            PoliticsTrackerTheme {
                PoliticsTrackerNavHost(container = container)
            }
        }
    }
}
