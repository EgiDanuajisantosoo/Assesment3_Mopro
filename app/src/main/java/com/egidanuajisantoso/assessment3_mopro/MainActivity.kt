package com.egidanuajisantoso.assessment3_mopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.egidanuajisantoso.assessment3_mopro.ui.screen.MainScreen
import com.egidanuajisantoso.assessment3_mopro.ui.theme.Assessment3_moproTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assessment3_moproTheme {
                MainScreen()
            }
        }
    }
}