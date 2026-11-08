package com.example.model

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Represents a polynomial: a_n * x^n + ... + a_1 * x + a_0
 * coefficients are stored from highest degree to constant term:
 * e.g., x^3 - 4x^2 + x + 6 is stored as [1, -4, 1, 6]
 */
data class Polynomial(
    val coefficients: List<Int>
) {
    val degree: Int get() = (coefficients.size - 1).coerceAtLeast(0)
    val leadingCoeff: Int get() = coefficients.firstOrNull() ?: 1
    val constantTerm: Int get() = coefficients.lastOrNull() ?: 0

    fun evaluate(x: Double): Double {
        var result = 0.0
        for (coeff in coefficients) {
            result = result * x + coeff
        }
        return result
    }

    fun toFormattedString(): String {
        if (coefficients.isEmpty()) return "0"
        val sb = StringBuilder()
        val n = degree
        for (i in 0..n) {
            val power = n - i
            val coeff = coefficients[i]
            if (coeff == 0 && n > 0) continue

            val absVal = abs(coeff)
            val sign = if (coeff < 0) " - " else if (sb.isNotEmpty()) " + " else ""

            if (coeff < 0 && sb.isEmpty()) {
                sb.append("-")
            } else {
                sb.append(sign)
            }

            when (power) {
                0 -> sb.append(absVal)
                1 -> {
                    if (absVal != 1) sb.append(absVal)
                    sb.append("x")
                }
                else -> {
                    if (absVal != 1) sb.append(absVal)
                    sb.append("x")
                    sb.append(toSuperscript(power))
                }
            }
        }
        if (sb.isEmpty()) sb.append("0")
        sb.append(" = 0")
        return sb.toString()
    }

    fun getPossibleDivisors(): List<Int> {
        val term = abs(constantTerm)
        if (term == 0) return listOf(0, 1, -1)
        val divisors = mutableSetOf<Int>()
        for (i in 1..term) {
            if (term % i == 0) {
                divisors.add(i)
                divisors.add(-i)
            }
        }
        return divisors.sorted()
    }

    /**
     * Finds integer rational roots
     */
    fun findIntegerRoots(): List<Int> {
        val candidates = getPossibleDivisors()
        return candidates.filter { root ->
            evaluate(root.toDouble()) == 0.0
        }
    }

    companion object {
        fun toSuperscript(num: Int): String {
            return num.toString().map { char ->
                when (char) {
                    '0' -> '⁰'
                    '1' -> '¹'
                    '2' -> '²'
                    '3' -> '³'
                    '4' -> '⁴'
                    '5' -> '⁵'
                    '6' -> '⁶'
                    '7' -> '⁷'
                    '8' -> '⁸'
                    '9' -> '⁹'
                    else -> char
                }
            }.joinToString("")
        }

        fun fromString(str: String): Polynomial {
            // Basic fallback or helper
            return Polynomial(listOf(1, -3, -1, 3))
        }
    }
}

/**
 * Synthetic division step representation for Ruffini method
 */
data class RuffiniStep(
    val root: Int,
    val topRow: List<Int>,
    val middleRow: List<Int?>, // multiplications placed under topRow
    val bottomRow: List<Int?>, // additions (coefficients of quotient + remainder)
    val currentColumn: Int,    // which column is currently being worked on
    val subStep: RuffiniSubStep,
    val remainder: Int? = null,
    val isSuccess: Boolean? = null
)

enum class RuffiniSubStep {
    SELECT_ROOT,     // Pick root candidate
    DROP_FIRST,      // Drop the leading coefficient
    MULTIPLY,        // Multiply bottomRow[col-1] * root
    ADD_COLUMN,      // Add topRow[col] + middleRow[col]
    COMPLETED        // Finished checking remainder
}

enum class WorldType {
    RUFFINI,
    RATIONAL_ROOTS,
    BIQUADRATIC,
    GROUPING,
    BOSS_ARENA,
    SANDBOX
}

data class GameLevel(
    val id: String,
    val worldType: WorldType,
    val levelNumber: Int,
    val title: String,
    val polynomial: Polynomial,
    val targetRoots: List<Int>,
    val hints: List<String>,
    val xpReward: Int = 100,
    val stars: Int = 0,
    val isUnlocked: Boolean = false
)

data class PlayerProfile(
    val level: Int = 1,
    val xp: Int = 0,
    val gems: Int = 20,
    val starsTotal: Int = 0,
    val streak: Int = 1,
    val solvedEquations: Int = 0,
    val ruffiniMastery: Int = 0,
    val biquadraticMastery: Int = 0,
    val bossDefeated: Int = 0
) {
    val xpForNextLevel: Int get() = level * 250
    val currentLevelProgress: Float get() = (xp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
}

data class BossMonster(
    val id: String,
    val name: String,
    val title: String,
    val maxHp: Int,
    val currentHp: Int,
    val polynomial: Polynomial,
    val currentRootsFound: List<Int> = emptyList(),
    val attackName: String,
    val weakness: String,
    val phase: Int = 1
)
