package com.example.booktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.example.booktracker.ui.navigation.BookNavHost
import com.example.booktracker.ui.theme.BookTrackerTheme
import com.example.booktracker.ui.theme.Libri
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Libri is a light-only identity, so the system bars are pinned to Paper White
        // with dark icons rather than following the system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Libri.Surface.toArgb(),
                Libri.Surface.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Libri.Surface.toArgb(),
                Libri.Surface.toArgb()
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            BookTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Libri.Background
                ) {
                    BookNavHost()
                }
            }
        }
    }
}
