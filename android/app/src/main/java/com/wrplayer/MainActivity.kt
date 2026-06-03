package com.wrplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.wrplayer.ui.debug.DesignGalleryScreen
import com.wrplayer.ui.theme.WrPlayerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WrPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // TEMPORARY Phase 6 design-system gallery for sign-off; replaced by the nav shell in Phase 8.
                    DesignGalleryScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
