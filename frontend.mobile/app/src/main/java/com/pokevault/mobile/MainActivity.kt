package com.pokevault.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pokevault.mobile.ui.navigation.PokeMarketNavHost
import com.pokevault.mobile.ui.theme.PokeMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeMarketTheme {
                PokeMarketNavHost()
            }
        }
    }
}
