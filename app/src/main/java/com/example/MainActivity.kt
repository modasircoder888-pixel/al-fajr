package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.alfajr.ui.AlFajrWatchApp
import com.example.alfajr.ui.watchface.WatchfaceViewModel
import com.example.ui.theme.AlFajrTheme

class MainActivity : ComponentActivity() {

    private val watchfaceViewModel: WatchfaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure true full-screen immersive mode for smartwatch
        configureSmartwatchImmersiveMode()

        setContent {
            AlFajrTheme {
                AlFajrWatchApp(
                    viewModel = watchfaceViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        configureSmartwatchImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureSmartwatchImmersiveMode()
        }
    }

    private fun configureSmartwatchImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
