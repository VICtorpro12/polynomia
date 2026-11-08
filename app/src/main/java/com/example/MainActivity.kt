package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BiquadraticScreen
import com.example.ui.screens.BossBattleScreen
import com.example.ui.screens.GroupingScreen
import com.example.ui.screens.RationalRootsScreen
import com.example.ui.screens.RuffiniGameScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.screens.WorldMapScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    PolynomiaApp()
                }
            }
        }
    }
}

@Composable
fun PolynomiaApp(
    viewModel: GameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val bosses by viewModel.bosses.collectAsState()
    val selectedWorldTab by viewModel.selectedWorldTab.collectAsState()
    val isAudioMuted by viewModel.isAudioMuted.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen !is Screen.WorldMap) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        when (val screen = currentScreen) {
            is Screen.WorldMap -> {
                WorldMapScreen(
                    profile = playerProfile,
                    levels = levels,
                    bosses = bosses,
                    selectedTab = selectedWorldTab,
                    onSelectTab = { viewModel.selectWorldTab(it) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            is Screen.RuffiniGame -> {
                RuffiniGameScreen(
                    level = screen.level,
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() },
                    onLevelCompleted = { levelId, stars, xp ->
                        viewModel.onLevelComplete(levelId, stars, xp)
                    }
                )
            }
            is Screen.RationalRootsGame -> {
                RationalRootsScreen(
                    level = screen.level,
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() },
                    onLevelCompleted = { levelId, stars, xp ->
                        viewModel.onLevelComplete(levelId, stars, xp)
                    }
                )
            }
            is Screen.BiquadraticGame -> {
                BiquadraticScreen(
                    level = screen.level,
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() },
                    onLevelCompleted = { levelId, stars, xp ->
                        viewModel.onLevelComplete(levelId, stars, xp)
                    }
                )
            }
            is Screen.GroupingGame -> {
                GroupingScreen(
                    level = screen.level,
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() },
                    onLevelCompleted = { levelId, stars, xp ->
                        viewModel.onLevelComplete(levelId, stars, xp)
                    }
                )
            }
            is Screen.BossBattle -> {
                BossBattleScreen(
                    boss = screen.boss,
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() },
                    onBossDefeated = { bossId, damage ->
                        viewModel.damageBoss(bossId, damage)
                    }
                )
            }
            is Screen.Sandbox -> {
                SandboxScreen(
                    profile = playerProfile,
                    audio = viewModel.audio,
                    isAudioMuted = isAudioMuted,
                    onToggleAudio = { viewModel.toggleAudio() },
                    onBackClick = { viewModel.navigateBack() }
                )
            }
        }
    }
}
