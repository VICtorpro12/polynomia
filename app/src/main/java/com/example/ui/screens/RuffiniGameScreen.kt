package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.model.Polynomial
import com.example.model.RuffiniSubStep
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.GameTopBar
import com.example.ui.theme.ArcanePurple
import com.example.ui.theme.NeonCyanDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun RuffiniGameScreen(
    level: GameLevel,
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onLevelCompleted: (levelId: String, stars: Int, xp: Int) -> Unit
) {
    // Current working coefficients (initially from polynomial, then quotient if continuing)
    var currentCoeffs by remember { mutableStateOf(level.polynomial.coefficients) }
    val discoveredRoots = remember { mutableStateListOf<Int>() }

    // Ruffini active step state
    var selectedRoot by remember { mutableStateOf<Int?>(null) }
    var currentStep by remember { mutableStateOf(RuffiniSubStep.SELECT_ROOT) }
    var currentWorkingColumn by remember { mutableIntStateOf(0) }

    // Table rows
    val middleRow = remember { mutableStateListOf<Int?>() }
    val bottomRow = remember { mutableStateListOf<Int?>() }

    var feedbackMessage by remember { mutableStateOf("Elige una raíz candidata") }
    var isSuccessState by remember { mutableStateOf<Boolean?>(null) }
    var showLevelClearedDialog by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Initialize rows when currentCoeffs changes
    LaunchedEffect(currentCoeffs) {
        middleRow.clear()
        bottomRow.clear()
        repeat(currentCoeffs.size) {
            middleRow.add(null)
            bottomRow.add(null)
        }
        selectedRoot = null
        currentWorkingColumn = 0
        currentStep = RuffiniSubStep.SELECT_ROOT
        isSuccessState = null
        feedbackMessage = "Elige una raíz candidata (divisores de ${currentCoeffs.last()})"
    }

    fun resetTrial() {
        middleRow.clear()
        bottomRow.clear()
        repeat(currentCoeffs.size) {
            middleRow.add(null)
            bottomRow.add(null)
        }
        selectedRoot = null
        currentWorkingColumn = 0
        currentStep = RuffiniSubStep.SELECT_ROOT
        isSuccessState = null
        feedbackMessage = "Prueba con otro divisor de ${currentCoeffs.last()}"
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
            title = level.title,
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
            // Equation Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ECUACIÓN OBJETIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = level.polynomial.toFormattedString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold,
                        textAlign = TextAlign.Center
                    )

                    // Discovered roots badges
                    if (discoveredRoots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Raíces halladas:",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                            discoveredRoots.forEach { r ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.2f))
                                        .border(1.dp, EmeraldGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "x = $r",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ruffini Interactive Table Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ruffini_table_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TABLA DE RUFFINI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal scrollable grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Root Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Raíz (r)",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Test root display box
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedRoot != null) ArcanePurple.copy(alpha = 0.25f)
                                        else DarkSurfaceElevated
                                    )
                                    .border(
                                        1.5.dp,
                                        if (selectedRoot != null) ArcanePurple else CardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = selectedRoot?.toString() ?: "?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selectedRoot != null) NeonCyan else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Divider line
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(2.dp)
                                    .background(NeonCyan.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                        }

                        // Vertical bar separator of Ruffini
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(160.dp)
                                .background(NeonCyan.copy(alpha = 0.6f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Coefficient columns
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            currentCoeffs.forEachIndexed { colIndex, coeff ->
                                val isCurrentCol = colIndex == currentWorkingColumn
                                val isLastCol = colIndex == currentCoeffs.size - 1

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(52.dp)
                                ) {
                                    // Power header (x^3, x^2, x, cte)
                                    val power = currentCoeffs.size - 1 - colIndex
                                    Text(
                                        text = if (power > 0) "x${Polynomial.toSuperscript(power)}" else "cte",
                                        fontSize = 11.sp,
                                        color = if (isCurrentCol) NeonCyan else TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Top Row: Coefficient
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isCurrentCol) NeonCyan else CardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text(
                                            text = "$coeff",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Middle Row: Multiplied value
                                    val middleVal = middleRow.getOrNull(colIndex)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (middleVal != null) ArcanePurple.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                    ) {
                                        Text(
                                            text = middleVal?.let { if (it > 0) "+$it" else "$it" } ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcanePurple
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Horizontal line
                                    Box(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .height(2.dp)
                                            .background(
                                                if (isLastCol) NeonGold else NeonCyan.copy(alpha = 0.4f)
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Bottom Row: Result
                                    val bottomVal = bottomRow.getOrNull(colIndex)
                                    val isRemainder = isLastCol
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    bottomVal == null -> DarkSurfaceVariant
                                                    isRemainder && bottomVal == 0 -> EmeraldGreen.copy(alpha = 0.3f)
                                                    isRemainder && bottomVal != 0 -> RubyRed.copy(alpha = 0.3f)
                                                    else -> NeonCyan.copy(alpha = 0.2f)
                                                }
                                            )
                                            .border(
                                                1.5.dp,
                                                when {
                                                    isRemainder && bottomVal == 0 -> EmeraldGreen
                                                    isRemainder && bottomVal != null -> RubyRed
                                                    isCurrentCol -> NeonCyan
                                                    else -> CardBorder
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Text(
                                            text = bottomVal?.toString() ?: "",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isRemainder && bottomVal == 0 -> EmeraldGreen
                                                isRemainder && bottomVal != null -> RubyRed
                                                else -> NeonCyan
                                            }
                                        )
                                    }

                                    if (isRemainder) {
                                        Text(
                                            text = "Resto",
                                            fontSize = 9.sp,
                                            color = if (bottomVal == 0) EmeraldGreen else NeonGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Guidance Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = feedbackMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (isSuccessState) {
                            true -> EmeraldGreen
                            false -> RubyRed
                            null -> TextPrimary
                        },
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1: Select Root Candidates
                    if (currentStep == RuffiniSubStep.SELECT_ROOT) {
                        val constantTerm = currentCoeffs.last()
                        val divisors = Polynomial(currentCoeffs).getPossibleDivisors()

                        Text(
                            text = "Divisores de $constantTerm:",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            divisors.forEach { candidate ->
                                OutlinedButton(
                                    onClick = {
                                        audio.playTap()
                                        selectedRoot = candidate
                                        currentStep = RuffiniSubStep.DROP_FIRST
                                        currentWorkingColumn = 0
                                        feedbackMessage = "Baja el primer coeficiente ($candidate como raíz)"
                                    },
                                    modifier = Modifier.testTag("root_candidate_$candidate"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = NeonCyan
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "$candidate",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Step 2: Drop Leading Coeff
                    if (currentStep == RuffiniSubStep.DROP_FIRST) {
                        val firstCoeff = currentCoeffs.first()
                        Button(
                            onClick = {
                                audio.playHit()
                                bottomRow[0] = firstCoeff
                                currentWorkingColumn = 1
                                currentStep = RuffiniSubStep.MULTIPLY
                                val nextM = firstCoeff * (selectedRoot ?: 1)
                                feedbackMessage = "Multiplica: $firstCoeff × ${selectedRoot} = $nextM"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_drop_first"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = DarkBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BAJAR PRIMER TÉRMINO ($firstCoeff)",
                                color = DarkBackground,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Step 3: Multiply
                    if (currentStep == RuffiniSubStep.MULTIPLY) {
                        val prevBottom = bottomRow[currentWorkingColumn - 1] ?: 0
                        val root = selectedRoot ?: 1
                        val expectedMultiplication = prevBottom * root

                        // Interactive choices for the multiplication result
                        val choices = listOf(
                            expectedMultiplication,
                            expectedMultiplication + 2,
                            -expectedMultiplication + 1
                        ).distinct().shuffled()

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "¿Cuánto es $prevBottom × ($root)?",
                                fontSize = 13.sp,
                                color = NeonGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                choices.forEach { choice ->
                                    Button(
                                        onClick = {
                                            if (choice == expectedMultiplication) {
                                                audio.playTap()
                                                middleRow[currentWorkingColumn] = expectedMultiplication
                                                currentStep = RuffiniSubStep.ADD_COLUMN
                                                val topVal = currentCoeffs[currentWorkingColumn]
                                                val sum = topVal + expectedMultiplication
                                                feedbackMessage = "Suma la columna: $topVal + ($expectedMultiplication) = $sum"
                                            } else {
                                                audio.playError()
                                                feedbackMessage = "¡Cálculo incorrecto! Intenta de nuevo"
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)
                                    ) {
                                        Text(text = "$choice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Step 4: Add Column
                    if (currentStep == RuffiniSubStep.ADD_COLUMN) {
                        val topVal = currentCoeffs[currentWorkingColumn]
                        val midVal = middleRow[currentWorkingColumn] ?: 0
                        val expectedSum = topVal + midVal

                        val choices = listOf(
                            expectedSum,
                            expectedSum - 3,
                            -expectedSum + 2
                        ).distinct().shuffled()

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "¿Cuánto es $topVal + ($midVal)?",
                                fontSize = 13.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                choices.forEach { choice ->
                                    Button(
                                        onClick = {
                                            if (choice == expectedSum) {
                                                audio.playTap()
                                                bottomRow[currentWorkingColumn] = expectedSum

                                                if (currentWorkingColumn < currentCoeffs.size - 1) {
                                                    // Move to next column
                                                    currentWorkingColumn++
                                                    currentStep = RuffiniSubStep.MULTIPLY
                                                    val nextM = expectedSum * (selectedRoot ?: 1)
                                                    feedbackMessage = "Multiplica: $expectedSum × ${selectedRoot} = $nextM"
                                                } else {
                                                    // Finished! Check remainder
                                                    currentStep = RuffiniSubStep.COMPLETED
                                                    if (expectedSum == 0) {
                                                        // Valid root!
                                                        audio.playSuccess()
                                                        isSuccessState = true
                                                        feedbackMessage = "¡IMPACTO CRÍTICO! x = $selectedRoot es raíz exacta (Resto = 0)"
                                                        val root = selectedRoot!!
                                                        if (!discoveredRoots.contains(root)) {
                                                            discoveredRoots.add(root)
                                                        }

                                                        // If we have found a root, quotient polynomial is bottomRow except the last column
                                                        val quotient = bottomRow.dropLast(1).filterNotNull()
                                                        if (quotient.size > 2) {
                                                            // Can continue Ruffini with the quotient
                                                            currentCoeffs = quotient
                                                        } else {
                                                            // We solved the cubic/quartic completely!
                                                            showConfetti = true
                                                            showLevelClearedDialog = true
                                                            onLevelCompleted(level.id, 3, level.xpReward)
                                                        }
                                                    } else {
                                                        // Failed root
                                                        audio.playError()
                                                        isSuccessState = false
                                                        feedbackMessage = "Resto = $expectedSum ≠ 0. ¡No es raíz! Intenta con otro divisor"
                                                    }
                                                }
                                            } else {
                                                audio.playError()
                                                feedbackMessage = "¡Suma incorrecta! Intenta de nuevo"
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyanDark)
                                    ) {
                                        Text(text = "$choice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Completed Step (Retry if failed, or Continue if success)
                    if (currentStep == RuffiniSubStep.COMPLETED) {
                        if (isSuccessState == false) {
                            Button(
                                onClick = { resetTrial() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_retry_trial"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RubyRed)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "PROBAR OTRO DIVISOR", fontWeight = FontWeight.Black)
                            }
                        } else {
                            // If more roots needed
                            Button(
                                onClick = {
                                    if (discoveredRoots.size < level.targetRoots.size && currentCoeffs.size > 2) {
                                        // continue with quotient
                                        resetTrial()
                                    } else {
                                        showLevelClearedDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_continue_ruffini"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (discoveredRoots.size >= level.targetRoots.size) "¡NIVEL COMPLETADO!" else "BUSCAR SIGUIENTE RAÍZ",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hint Oracle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = {
                        audio.playTap()
                        val hint = level.hints.firstOrNull() ?: "Prueba con divisores enteros de ${currentCoeffs.last()}"
                        feedbackMessage = "🔮 Oráculo: $hint"
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = NeonGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pista del Oráculo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Confetti on Victory
    ConfettiCelebration(
        isActive = showConfetti,
        onFinished = { showConfetti = false }
    )

    // Level Cleared Dialog
    if (showLevelClearedDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { /* forced action */ }) {
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
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(NeonGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡VICTORIA MATEMÁTICA!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Has dominado el Método de Ruffini",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Stars animation
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NeonGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "+${level.xpReward} XP  |  +15 Gemas 💎",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showLevelClearedDialog = false
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_finish_level"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(
                            text = "VOLVER AL MAPA",
                            fontWeight = FontWeight.Black,
                            color = DarkBackground
                        )
                    }
                }
            }
        }
    }
}
