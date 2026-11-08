package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudio
import com.example.data.GameRepository
import com.example.model.BossMonster
import com.example.model.GameLevel
import com.example.model.PlayerProfile
import com.example.model.Polynomial
import com.example.model.WorldType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object WorldMap : Screen()
    data class RuffiniGame(val level: GameLevel) : Screen()
    data class RationalRootsGame(val level: GameLevel) : Screen()
    data class BiquadraticGame(val level: GameLevel) : Screen()
    data class GroupingGame(val level: GameLevel) : Screen()
    data class BossBattle(val boss: BossMonster) : Screen()
    object Sandbox : Screen()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application)
    val audio = GameAudio(application)

    val playerProfile: StateFlow<PlayerProfile> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerProfile())

    val levels: StateFlow<List<GameLevel>> = repository.levels
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bosses: StateFlow<List<BossMonster>> = repository.bosses
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.WorldMap)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isAudioMuted = MutableStateFlow(audio.isMuted())
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    private val _selectedWorldTab = MutableStateFlow(WorldType.RUFFINI)
    val selectedWorldTab: StateFlow<WorldType> = _selectedWorldTab.asStateFlow()

    // Confetti celebration state
    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    fun navigateTo(screen: Screen) {
        audio.playTap()
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value !is Screen.WorldMap) {
            audio.playTap()
            _currentScreen.value = Screen.WorldMap
            return true
        }
        return false
    }

    fun selectWorldTab(worldType: WorldType) {
        audio.playTap()
        _selectedWorldTab.value = worldType
    }

    fun toggleAudio() {
        _isAudioMuted.value = audio.toggleMute()
    }

    fun onLevelComplete(levelId: String, stars: Int, xpEarned: Int) {
        audio.playVictory()
        triggerCelebration()
        repository.completeLevel(levelId, stars, xpEarned)
    }

    fun triggerCelebration() {
        viewModelScope.launch {
            _showConfetti.value = true
        }
    }

    fun dismissCelebration() {
        _showConfetti.value = false
    }

    fun damageBoss(bossId: String, damage: Int): BossMonster? {
        val updated = repository.damageBoss(bossId, damage)
        if (updated?.currentHp == 0) {
            audio.playVictory()
            triggerCelebration()
        } else {
            audio.playHit()
        }
        return updated
    }

    fun resetBoss(bossId: String) {
        repository.resetBoss(bossId)
    }
}
