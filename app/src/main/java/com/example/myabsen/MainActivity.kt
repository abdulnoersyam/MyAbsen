package com.example.myabsen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myabsen.ui.theme.MyAbsenTheme
import com.example.myabsen.ui.theme.background_color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAbsenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = background_color
                ) {
                    MyAbsenApp()
                }
            }
        }
    }
}