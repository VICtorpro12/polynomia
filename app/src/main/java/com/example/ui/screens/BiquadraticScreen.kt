package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.sqrt

enum class BiquadStage {
    STAGE_TRANSMUTE, // Change variable u = x^2
    STAGE_SOLVE_U,   // Solve quadratic in u
    STAGE_UNPACK_X,  // x = ±√u
    STAGE_COMPLETED
}

@Composable
fun BiquadraticScreen(
    level: GameLevel,
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onLevelCompleted: (levelId: String, stars: Int, xp: Int) -> Unit
) {
    var stage by remember { mutableStateOf(BiquadStage.STAGE_TRANSMUTE) }

    // Coefficients: ax^4 + bx^2 + c = 0
    val a = level.polynomial.coefficients.getOrNull(0) ?: 1
    val b = level.polynomial.coefficients.getOrNull(2) ?: -5
    val c = level.polynomial.coefficients.getOrNull(4) ?: 4

    // u roots: (u - u1)(u - u2) = 0
    val discriminant = (b * b - 4 * a * c).toDouble()
    val u1 = ((-b + sqrt(discriminant)) / (2 * a)).toInt()
    val u2 = ((-b - sqrt(discriminant)) / (2 * a)).toInt()

    var selectedU1 by remember { mutableStateOf<Int?>(null) }
    var selectedU2 by remember { mutableStateOf<Int?>(null) }
    var unpackedRoots by remember { mutableStateOf(setOf<Int>()) }

    var showConfetti by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(unpackedRoots.size) {
        if (unpackedRoots.size >= 4 && !showVictoryDialog) {
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
            title = "Alquimia Bicuadrada",
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
            // Stage Indicator Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StagePill(1, "u = x²", stage.ordinal >= 0, stage == BiquadStage.STAGE_TRANSMUTE)
                StagePill(2, "Hallar u", stage.ordinal >= 1, stage == BiquadStage.STAGE_SOLVE_U)
                StagePill(3, "x = ±√u", stage.ordinal >= 2, stage == BiquadStage.STAGE_UNPACK_X)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Cauldron / Equation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ArcanePurple)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ArcanePurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ArcanePurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (stage == BiquadStage.STAGE_TRANSMUTE) {
                            "ECUACIÓN BICUADRADA (GRADO 4)"
                        } else {
                            "ECUACIÓN TRANSMUTADA EN u"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcanePurple,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Equation display
                    Text(
                        text = if (stage == BiquadStage.STAGE_TRANSMUTE) {
                            level.polynomial.toFormattedString()
                        } else {
                            val signB = if (b >= 0) "+ $b" else "- ${kotlin.math.abs(b)}"
                            val signC = if (c >= 0) "+ $c" else "- ${kotlin.math.abs(c)}"
                            val coeffA = if (a == 1) "" else "$a"
                            "${coeffA}u² $signB u $signC = 0"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (stage) {
                            BiquadStage.STAGE_TRANSMUTE -> "Solo contiene potencias pares: x⁴ y x². ¡Ideal para cambio de variable!"
                            BiquadStage.STAGE_SOLVE_U -> "¡Ahora es una ecuación cuadrática simple! Resuelve para u."
                            BiquadStage.STAGE_UNPACK_X -> "Deshaz el cambio: como u = x², entonces x = ±√u"
                            BiquadStage.STAGE_COMPLETED -> "¡Las 4 raíces han sido purificadas!"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stage 1: Transmute Button
            if (stage == BiquadStage.STAGE_TRANSMUTE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CAMBIO DE VARIABLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "u = x²", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "⟹", fontSize = 18.sp, color = TextMuted)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "u² = x⁴", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ArcanePurple)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                audio.playTransform()
                                stage = BiquadStage.STAGE_SOLVE_U
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_transmute_u"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "TRANSMUTAR A u²", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Stage 2: Solve u
            if (stage == BiquadStage.STAGE_SOLVE_U) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SELECCIONA LAS SOLUCIONES DE u",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dos valores que multiplicados den $c y sumados den ${-b}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val candidatesU = listOf(u1, u2, u1 + 2, u2 + 3, -u1, -u2).distinct().shuffled()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            candidatesU.forEach { valU ->
                                val isSelected = selectedU1 == valU || selectedU2 == valU
                                OutlinedButton(
                                    onClick = {
                                        audio.playTap()
                                        if (valU == u1 && selectedU1 == null) {
                                            selectedU1 = valU
                                        } else if (valU == u2 && selectedU2 == null) {
                                            selectedU2 = valU
                                        } else if (valU == u1 && selectedU2 == null && selectedU1 != null) {
                                            // already selected
                                        } else {
                                            audio.playError()
                                        }

                                        if (selectedU1 != null && selectedU2 != null) {
                                            audio.playSuccess()
                                            stage = BiquadStage.STAGE_UNPACK_X
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_u_candidate_$valU"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent,
                                        contentColor = if (isSelected) EmeraldGreen else NeonCyan
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) EmeraldGreen else CardBorder
                                    )
                                ) {
                                    Text(
                                        text = "u = $valU",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = "u₁ = ${selectedU1 ?: '?'}", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            Text(text = "u₂ = ${selectedU2 ?: '?'}", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Stage 3: Unpack x = ±√u
            if (stage == BiquadStage.STAGE_UNPACK_X) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DESHACER TRANSMUTACIÓN: x = ±√u",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Root Extraction 1: u1
                        val rootVal1 = sqrt(u1.toDouble()).toInt()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "De u = $u1 ⟹ x = ±√$u1",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            audio.playHit()
                                            unpackedRoots = unpackedRoots + rootVal1
                                        },
                                        enabled = !unpackedRoots.contains(rootVal1),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                    ) {
                                        Text(text = "+$rootVal1", color = DarkBackground, fontWeight = FontWeight.Black)
                                    }
                                    Button(
                                        onClick = {
                                            audio.playHit()
                                            unpackedRoots = unpackedRoots + (-rootVal1)
                                        },
                                        enabled = !unpackedRoots.contains(-rootVal1),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                    ) {
                                        Text(text = "-$rootVal1", color = DarkBackground, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Root Extraction 2: u2
                        val rootVal2 = sqrt(u2.toDouble()).toInt()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "De u = $u2 ⟹ x = ±√$u2",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            audio.playHit()
                                            unpackedRoots = unpackedRoots + rootVal2
                                        },
                                        enabled = !unpackedRoots.contains(rootVal2),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Text(text = "+$rootVal2", color = DarkBackground, fontWeight = FontWeight.Black)
                                    }
                                    Button(
                                        onClick = {
                                            audio.playHit()
                                            unpackedRoots = unpackedRoots + (-rootVal2)
                                        },
                                        enabled = !unpackedRoots.contains(-rootVal2),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Text(text = "-$rootVal2", color = DarkBackground, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Collected 4 roots
                        Text(
                            text = "Raíces colectadas (${unpackedRoots.size}/4):",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            unpackedRoots.sorted().forEach { root ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.2f))
                                        .border(1.dp, EmeraldGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "x = $root",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = EmeraldGreen
                                    )
                                }
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
                border = androidx.compose.foundation.BorderStroke(2.dp, ArcanePurple),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = ArcanePurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡TRANSMUTACIÓN EXITOSA!",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = ArcanePurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Has resuelto la ecuación bicuadrada de grado 4",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "+${level.xpReward} XP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showVictoryDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_close_biquad_victory"),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)
                    ) {
                        Text(text = "VOLVER AL MAPA", fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun StagePill(step: Int, label: String, isPassed: Boolean, isCurrent: Boolean) {
    val bg = when {
        isCurrent -> NeonCyan
        isPassed -> EmeraldGreen.copy(alpha = 0.3f)
        else -> DarkSurfaceElevated
    }
    val textColor = when {
        isCurrent -> DarkBackground
        isPassed -> EmeraldGreen
        else -> TextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$step. $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
