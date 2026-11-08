package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudio
import com.example.model.GameLevel
import com.example.model.PlayerProfile
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.GameTopBar
import com.example.ui.theme.ArcanePurple
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

enum class GroupingPhase {
    PAIR_TERMS,       // Grouping terms in pairs
    EXTRACT_COMMON,   // Factor out common monomials
    COMBINE_BINOMIAL, // Factor out common binomial
    FIND_ROOTS,       // Final roots collection
    COMPLETED
}

@Composable
fun GroupingScreen(
    level: GameLevel,
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onLevelCompleted: (levelId: String, stars: Int, xp: Int) -> Unit
) {
    var phase by remember { mutableStateOf(GroupingPhase.PAIR_TERMS) }

    // For level 1: x^3 + 2x^2 - x - 2 = 0
    // Group 1: x^3 + 2x^2 -> x^2(x + 2)
    // Group 2: -x - 2 -> -1(x + 2)
    // Common: (x + 2)(x^2 - 1) = 0
    val targetRoots = level.targetRoots
    val solvedRoots = remember { mutableStateOf(setOf<Int>()) }

    var showConfetti by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }

    val c0 = level.polynomial.coefficients.getOrNull(0) ?: 1
    val c1 = level.polynomial.coefficients.getOrNull(1) ?: 2
    val c2 = level.polynomial.coefficients.getOrNull(2) ?: -1
    val c3 = level.polynomial.coefficients.getOrNull(3) ?: -2

    val rootFromBinomial = -c1 / c0
    val sqVal = abs(c2 / c0)
    val rootFromSquares = kotlin.math.sqrt(sqVal.toDouble()).toInt()

    LaunchedEffect(solvedRoots.value.size) {
        if (solvedRoots.value.size >= targetRoots.size && !showVictoryDialog) {
            audio.playVictory()
            showConfetti = true
            showVictoryDialog = true
            onLevelCompleted(level.id, 3, level.xpReward)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        GameTopBar(
            profile = profile,
            isAudioMuted = isAudioMuted,
            onToggleAudio = onToggleAudio,
            title = "Factor Común & Agrupación",
            showBackButton = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Equation Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MÉTODO DE FACTOR COMÚN POR AGRUPACIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = level.polynomial.toFormattedString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Pair terms
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PASO 1: AGRUPAR EN PAREJAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Group 1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "(${c0}x³ + ${c1}x²)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Text(text = " + ", fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 6.dp))

                        // Group 2
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, ArcanePurple, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "(${c2}x + ${c3})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcanePurple
                            )
                        }

                        Text(text = " = 0", fontSize = 15.sp, color = TextPrimary)
                    }

                    if (phase == GroupingPhase.PAIR_TERMS) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                audio.playHit()
                                phase = GroupingPhase.EXTRACT_COMMON
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_group_confirm"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text(text = "EXTRAER FACTOR COMÚN", fontWeight = FontWeight.Black, color = DarkBackground)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 2: Extract Common monomial
            AnimatedVisibility(visible = phase.ordinal >= GroupingPhase.EXTRACT_COMMON.ordinal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PASO 2: EXTRAER FACTORES COMUNES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "De la 1ra pareja extraemos x²; de la 2da extraemos $c2",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "x²(x + $c1) $c2(x + $c1) = 0",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                        }

                        if (phase == GroupingPhase.EXTRACT_COMMON) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    audio.playSuccess()
                                    phase = GroupingPhase.COMBINE_BINOMIAL
                                },
                                modifier = Modifier.fillMaxWidth().testTag("btn_combine_binomial"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text(text = "AGRUPAR BINOMIO COMÚN (x + $c1)", fontWeight = FontWeight.Black, color = DarkBackground)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 3: Combine Common Binomial
            AnimatedVisibility(visible = phase.ordinal >= GroupingPhase.COMBINE_BINOMIAL.ordinal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PASO 3: FACTORIZACIÓN FINAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "(x + $c1) · (x² - $sqVal) = 0",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Toca para despejar cada raíz:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Root from binomial
                            Button(
                                onClick = {
                                    audio.playHit()
                                    solvedRoots.value = solvedRoots.value + rootFromBinomial
                                },
                                enabled = !solvedRoots.value.contains(rootFromBinomial),
                                modifier = Modifier.weight(1f).testTag("btn_solve_root_1"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text(
                                    text = "x + $c1 = 0 ⟹ x = $rootFromBinomial",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Root + from square
                            Button(
                                onClick = {
                                    audio.playHit()
                                    solvedRoots.value = solvedRoots.value + rootFromSquares
                                },
                                enabled = !solvedRoots.value.contains(rootFromSquares),
                                modifier = Modifier.weight(1f).testTag("btn_solve_root_2"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text(
                                    text = "x = +√$sqVal ⟹ x = +$rootFromSquares",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBackground
                                )
                            }

                            // Root - from square
                            Button(
                                onClick = {
                                    audio.playHit()
                                    solvedRoots.value = solvedRoots.value + (-rootFromSquares)
                                },
                                enabled = !solvedRoots.value.contains(-rootFromSquares),
                                modifier = Modifier.weight(1f).testTag("btn_solve_root_3"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text(
                                    text = "x = -√$sqVal ⟹ x = -$rootFromSquares",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confetti
    ConfettiCelebration(
        isActive = showConfetti,
        onFinished = { showConfetti = false }
    )

    // Victory Dialog
    if (showVictoryDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, NeonGold),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡AGRUPACIÓN PERFECTA!",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Has factorizado la ecuación cúbica por agrupación",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "+${level.xpReward} XP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showVictoryDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_finish_grouping"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold)
                    ) {
                        Text(text = "VOLVER AL MAPA", fontWeight = FontWeight.Black, color = DarkBackground)
                    }
                }
            }
        }
    }
}
