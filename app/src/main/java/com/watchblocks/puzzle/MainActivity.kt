package com.watchblocks.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.watchblocks.puzzle.game.GameState
import com.watchblocks.puzzle.ui.GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("watchblocks", MODE_PRIVATE)

        setContent {
            val gameState = remember {
                GameState().apply { best = prefs.getInt("best", 0) }
            }

            // Persist immediately whenever a new best is set, so it
            // survives the process being killed (not just clean exit).
            LaunchedEffect(gameState.best) {
                prefs.edit().putInt("best", gameState.best).apply()
            }

            GameScreen(gameState = gameState)
        }
    }
}
