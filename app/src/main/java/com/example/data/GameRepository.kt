package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BossMonster
import com.example.model.GameLevel
import com.example.model.PlayerProfile
import com.example.model.Polynomial
import com.example.model.WorldType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("polynomia_game_prefs", Context.MODE_PRIVATE)

    private val _playerProfile = MutableStateFlow(loadPlayerProfile())
    val playerProfile: StateFlow<PlayerProfile> = _playerProfile.asStateFlow()

    private val _levels = MutableStateFlow(loadLevels())
    val levels: StateFlow<List<GameLevel>> = _levels.asStateFlow()

    private val _bosses = MutableStateFlow(initBosses())
    val bosses: StateFlow<List<BossMonster>> = _bosses.asStateFlow()

    private fun loadPlayerProfile(): PlayerProfile {
        val level = prefs.getInt("player_level", 1)
        val xp = prefs.getInt("player_xp", 0)
        val gems = prefs.getInt("player_gems", 50)
        val stars = prefs.getInt("player_stars", 0)
        val streak = prefs.getInt("player_streak", 1)
        val solved = prefs.getInt("player_solved", 0)
        val ruffini = prefs.getInt("player_ruffini_mastery", 0)
        val biquad = prefs.getInt("player_biquad_mastery", 0)
        val bossDef = prefs.getInt("player_boss_defeated", 0)

        return PlayerProfile(
            level = level,
            xp = xp,
            gems = gems,
            starsTotal = stars,
            streak = streak,
            solvedEquations = solved,
            ruffiniMastery = ruffini,
            biquadraticMastery = biquad,
            bossDefeated = bossDef
        )
    }

    private fun savePlayerProfile(profile: PlayerProfile) {
        prefs.edit()
            .putInt("player_level", profile.level)
            .putInt("player_xp", profile.xp)
            .putInt("player_gems", profile.gems)
            .putInt("player_stars", profile.starsTotal)
            .putInt("player_streak", profile.streak)
            .putInt("player_solved", profile.solvedEquations)
            .putInt("player_ruffini_mastery", profile.ruffiniMastery)
            .putInt("player_biquad_mastery", profile.biquadraticMastery)
            .putInt("player_boss_defeated", profile.bossDefeated)
            .apply()
        _playerProfile.value = profile
    }

    private fun loadLevels(): List<GameLevel> {
        val baseLevels = listOf(
            // World 1: Ruffini Tower
            GameLevel(
                id = "ruffini_1",
                worldType = WorldType.RUFFINI,
                levelNumber = 1,
                title = "Iniciación de Ruffini",
                polynomial = Polynomial(listOf(1, -4, 1, 6)),
                targetRoots = listOf(-1, 2, 3),
                hints = listOf("Prueba con divisores del término independiente 6: ±1, ±2, ±3", "Baja el primer término y multiplica por la raíz"),
                xpReward = 120,
                isUnlocked = true
            ),
            GameLevel(
                id = "ruffini_2",
                worldType = WorldType.RUFFINI,
                levelNumber = 2,
                title = "Valle de Negativos",
                polynomial = Polynomial(listOf(1, 2, -5, -6)),
                targetRoots = listOf(-1, 2, -3),
                hints = listOf("Los divisores de -6 son ±1, ±2, ±3, ±6", "Recuerda: (-1) * (-1) = +1"),
                xpReward = 140,
                isUnlocked = true
            ),
            GameLevel(
                id = "ruffini_3",
                worldType = WorldType.RUFFINI,
                levelNumber = 3,
                title = "El Término Invisible (0x²)",
                polynomial = Polynomial(listOf(1, 0, -7, 6)),
                targetRoots = listOf(1, 2, -3),
                hints = listOf("¡Cuidado! Falta el término x², por eso colocamos un 0 en su columna", "Divisores de 6: prueba con 1 o 2"),
                xpReward = 160,
                isUnlocked = false
            ),
            GameLevel(
                id = "ruffini_4",
                worldType = WorldType.RUFFINI,
                levelNumber = 4,
                title = "Cuártica de las Sombras",
                polynomial = Polynomial(listOf(1, -2, -7, 8, 12)),
                targetRoots = listOf(-1, 2, -2, 3),
                hints = listOf("Grado 4: al hallar una raíz pasamos a grado 3 y repetimos Ruffini", "Divisores de 12: ±1, ±2, ±3"),
                xpReward = 200,
                isUnlocked = false
            ),
            GameLevel(
                id = "ruffini_5",
                worldType = WorldType.RUFFINI,
                levelNumber = 5,
                title = "Maestría Cúbica",
                polynomial = Polynomial(listOf(1, -6, 11, -6)),
                targetRoots = listOf(1, 2, 3),
                hints = listOf("Las raíces son consecutivas", "Divisores positivos de -6"),
                xpReward = 220,
                isUnlocked = false
            ),

            // World 2: Rational Roots Hunter
            GameLevel(
                id = "rational_1",
                worldType = WorldType.RATIONAL_ROOTS,
                levelNumber = 1,
                title = "Caza de Divisores Simples",
                polynomial = Polynomial(listOf(1, -2, -5, 6)),
                targetRoots = listOf(1, -2, 3),
                hints = listOf("Toca todos los divisores enteros del término independiente 6: ±1, ±2, ±3, ±6"),
                xpReward = 100,
                isUnlocked = true
            ),
            GameLevel(
                id = "rational_2",
                worldType = WorldType.RATIONAL_ROOTS,
                levelNumber = 2,
                title = "La Fracción de Gauss",
                polynomial = Polynomial(listOf(2, 3, -8, 3)),
                targetRoots = listOf(1, -3),
                hints = listOf("El término principal es 2 y el independiente es 3. Candidatos p/q: ±1, ±3, ±1/2, ±3/2"),
                xpReward = 150,
                isUnlocked = true
            ),
            GameLevel(
                id = "rational_3",
                worldType = WorldType.RATIONAL_ROOTS,
                levelNumber = 3,
                title = "Orbes del Número 12",
                polynomial = Polynomial(listOf(1, -1, -8, 12)),
                targetRoots = listOf(2, -3),
                hints = listOf("Divisores de 12: ±1, ±2, ±3, ±4, ±6, ±12"),
                xpReward = 160,
                isUnlocked = false
            ),

            // World 3: Biquadratic Alchemist
            GameLevel(
                id = "biquad_1",
                worldType = WorldType.BIQUADRATIC,
                levelNumber = 1,
                title = "Transmutación Cuártica",
                polynomial = Polynomial(listOf(1, 0, -5, 0, 4)),
                targetRoots = listOf(-2, -1, 1, 2),
                hints = listOf("Transforma con u = x²: u² - 5u + 4 = 0", "Halla u = 1 y u = 4, luego x = ±√u"),
                xpReward = 150,
                isUnlocked = true
            ),
            GameLevel(
                id = "biquad_2",
                worldType = WorldType.BIQUADRATIC,
                levelNumber = 2,
                title = "El Caldero del 36",
                polynomial = Polynomial(listOf(1, 0, -13, 0, 36)),
                targetRoots = listOf(-3, -2, 2, 3),
                hints = listOf("Cambio u = x² produce u² - 13u + 36 = 0", "Dos números que multiplicados den 36 y sumados 13: 4 y 9"),
                xpReward = 180,
                isUnlocked = false
            ),
            GameLevel(
                id = "biquad_3",
                worldType = WorldType.BIQUADRATIC,
                levelNumber = 3,
                title = "Doble Raíz de Jade",
                polynomial = Polynomial(listOf(1, 0, -10, 0, 9)),
                targetRoots = listOf(-3, -1, 1, 3),
                hints = listOf("u² - 10u + 9 = 0 -> u = 1, u = 9", "x = ±√1 = ±1, x = ±√9 = ±3"),
                xpReward = 200,
                isUnlocked = false
            ),

            // World 4: Grouping & Common Factor
            GameLevel(
                id = "grouping_1",
                worldType = WorldType.GROUPING,
                levelNumber = 1,
                title = "Agrupación Elemental",
                polynomial = Polynomial(listOf(1, 2, -1, -2)),
                targetRoots = listOf(-2, -1, 1),
                hints = listOf("Agrupa: [x³ + 2x²] y [-x - 2]", "x²(x + 2) - 1(x + 2) = (x + 2)(x² - 1)"),
                xpReward = 130,
                isUnlocked = true
            ),
            GameLevel(
                id = "grouping_2",
                worldType = WorldType.GROUPING,
                levelNumber = 2,
                title = "Doble Binomio Gemelo",
                polynomial = Polynomial(listOf(1, -3, -4, 12)),
                targetRoots = listOf(-2, 2, 3),
                hints = listOf("Agrupa: x²(x - 3) - 4(x - 3) = 0", "(x - 3)(x² - 4) = 0 -> x = 3, x = ±2"),
                xpReward = 160,
                isUnlocked = false
            ),
            GameLevel(
                id = "grouping_3",
                worldType = WorldType.GROUPING,
                levelNumber = 3,
                title = "Santuario del 9",
                polynomial = Polynomial(listOf(1, 5, -9, -45)),
                targetRoots = listOf(-5, -3, 3),
                hints = listOf("x²(x + 5) - 9(x + 5) = 0", "(x + 5)(x² - 9) = 0"),
                xpReward = 180,
                isUnlocked = false
            )
        )

        // Load saved stars & unlock states
        return baseLevels.map { level ->
            val savedStars = prefs.getInt("level_stars_${level.id}", 0)
            val savedUnlocked = prefs.getBoolean("level_unlocked_${level.id}", level.isUnlocked)
            level.copy(stars = savedStars, isUnlocked = savedUnlocked)
        }
    }

    private fun initBosses(): List<BossMonster> {
        return listOf(
            BossMonster(
                id = "boss_1",
                name = "Gólem Cúbico",
                title = "Guardián de la Torre de Piedra",
                maxHp = 100,
                currentHp = 100,
                polynomial = Polynomial(listOf(1, -3, -4, 12)),
                attackName = "Avalancha Polinómica",
                weakness = "División de Ruffini con x = 2 o x = 3"
            ),
            BossMonster(
                id = "boss_2",
                name = "Dragón Bicuadrado",
                title = "Señor de la Cuarta Dimensión",
                maxHp = 160,
                currentHp = 160,
                polynomial = Polynomial(listOf(1, 0, -20, 0, 64)),
                attackName = "Fuego Cuártico u²",
                weakness = "Cambio de Variable u = x²"
            ),
            BossMonster(
                id = "boss_3",
                name = "Leviatán Cuártico",
                title = "Monstruo de Cuatro Raíces",
                maxHp = 220,
                currentHp = 220,
                polynomial = Polynomial(listOf(1, -2, -7, 8, 12)),
                attackName = "Vórtice Sintético",
                weakness = "Ruffini en cadena (x = -1, x = 2)"
            )
        )
    }

    fun completeLevel(levelId: String, stars: Int, xpEarned: Int) {
        val currentProfile = _playerProfile.value
        val newXp = currentProfile.xp + xpEarned
        var newLevel = currentProfile.level
        var xpRemainder = newXp
        while (xpRemainder >= newLevel * 250) {
            xpRemainder -= newLevel * 250
            newLevel++
        }

        val updatedProfile = currentProfile.copy(
            level = newLevel,
            xp = xpRemainder,
            starsTotal = currentProfile.starsTotal + stars,
            gems = currentProfile.gems + (stars * 5),
            solvedEquations = currentProfile.solvedEquations + 1
        )
        savePlayerProfile(updatedProfile)

        // Update level stars and unlock next level in the world
        val updatedLevels = _levels.value.mapIndexed { index, level ->
            if (level.id == levelId) {
                val bestStars = maxOf(level.stars, stars)
                prefs.edit().putInt("level_stars_${level.id}", bestStars).apply()
                level.copy(stars = bestStars)
            } else {
                level
            }
        }.toMutableList()

        // Unlock next level in list if applicable
        val currentIndex = updatedLevels.indexOfFirst { it.id == levelId }
        if (currentIndex != -1 && currentIndex + 1 < updatedLevels.size) {
            val nextLevel = updatedLevels[currentIndex + 1]
            prefs.edit().putBoolean("level_unlocked_${nextLevel.id}", true).apply()
            updatedLevels[currentIndex + 1] = nextLevel.copy(isUnlocked = true)
        }

        _levels.value = updatedLevels
    }

    fun damageBoss(bossId: String, damage: Int): BossMonster? {
        val currentBosses = _bosses.value.toMutableList()
        val index = currentBosses.indexOfFirst { it.id == bossId }
        if (index == -1) return null

        val boss = currentBosses[index]
        val newHp = (boss.currentHp - damage).coerceAtLeast(0)
        val updatedBoss = boss.copy(currentHp = newHp)
        currentBosses[index] = updatedBoss
        _bosses.value = currentBosses

        if (newHp == 0) {
            val prof = _playerProfile.value
            savePlayerProfile(prof.copy(bossDefeated = prof.bossDefeated + 1, gems = prof.gems + 50))
        }

        return updatedBoss
    }

    fun resetBoss(bossId: String) {
        _bosses.value = _bosses.value.map {
            if (it.id == bossId) it.copy(currentHp = it.maxHp, currentRootsFound = emptyList()) else it
        }
    }
}
