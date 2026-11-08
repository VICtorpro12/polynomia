package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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

data class OrbItem(
    val value: Int,
    val isValidDivisor: Boolean,
    var isTapped: Boolean = false
)

@Composable
fun RationalRootsScreen(
    level: GameLevel,
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onLevelCompleted: (levelId: String, stars: Int, xp: Int) -> Unit
) {
    val a0 = level.polynomial.constantTerm
    val an = level.polynomial.leadingCoeff
    val trueDivisors = remember { level.polynomial.getPossibleDivisors().toSet() }

    // Generate pool of orbs (true divisors + fake numbers)
    val orbs = remember {
        val list = mutableStateListOf<OrbItem>()
        val initialItems = mutableListOf<OrbItem>()
        trueDivisors.forEach { div ->
            initialItems.add(OrbItem(div, true))
        }
        val fakes = listOf(5, -5, 7, -7, 8, -8, 10, -10, 11, -11)
            .filter { !trueDivisors.contains(it) }
            .take(6)
        fakes.forEach { fake ->
            initialItems.add(OrbItem(fake, false))
        }
        list.addAll(initialItems.shuffled())
        list
    }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(1) }
    var discoveredRoots by remember { mutableStateOf(setOf<Int>()) }
    var showLevelClearedDialog by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var testedRootResult by remember { mutableStateOf<String?>(null) }

    val capturedValidCount = orbs.count { it.isValidDivisor && it.isTapped }
    val totalValidCount = orbs.count { it.isValidDivisor }
    val progressFraction = if (totalValidCount > 0) capturedValidCount.toFloat() / totalValidCount.toFloat() else 0f

    LaunchedEffect(capturedValidCount, totalValidCount) {
        if (totalValidCount > 0 && capturedValidCount >= totalValidCount && !showLevelClearedDialog) {
            audio.playVictory()
            showConfetti = true
            showLevelClearedDialog = true
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
            title = "Caza de Divisores (p/q)",
            showBackButton = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Target Equation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TEOREMA DE LAS RAÍCES RACIONALES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = level.polynomial.toFormattedString(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Coeff principal an
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "q (an) = $an",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        // Termino independiente a0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "p (a₀) = $a0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Progress HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Divisores Capturados: ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$capturedValidCount / $totalValidCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonGold.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "COMBO ×$combo 🔥",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldGreen,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "TOCA LAS ORBES QUE SEAN DIVISORES DE $a0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Orbs Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(orbs.size) { index ->
                    val orb = orbs[index]
                    val isTapped = orb.isTapped

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(70.dp)
                            .testTag("orb_${orb.value}")
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isTapped -> Brush.radialGradient(
                                        listOf(DarkSurfaceElevated, DarkSurface)
                                    )
                                    orb.isValidDivisor -> Brush.radialGradient(
                                        listOf(EmeraldGreen, Color(0xFF00994D))
                                    )
                                    else -> Brush.radialGradient(
                                        listOf(RubyRed, Color(0xFF880E4F))
                                    )
                                }
                            )
                            .border(
                                2.dp,
                                when {
                                    !isTapped -> CardBorder
                                    orb.isValidDivisor -> EmeraldGreen
                                    else -> RubyRed
                                },
                                CircleShape
                            )
                            .clickable(enabled = !isTapped) {
                                orbs[index] = orb.copy(isTapped = true)
                                if (orb.isValidDivisor) {
                                    audio.playHit()
                                    score += 20 * combo
                                    combo++
                                    // Check if this divisor is an exact root!
                                    val eval = level.polynomial.evaluate(orb.value.toDouble())
                                    if (eval == 0.0) {
                                        audio.playSuccess()
                                        discoveredRoots = discoveredRoots + orb.value
                                        testedRootResult = "¡RAÍZ ENCONTRADA! P(${orb.value}) = 0"
                                    } else {
                                        testedRootResult = "Divisor válido: P(${orb.value}) = ${eval.toInt()}"
                                    }
                                } else {
                                    audio.playError()
                                    combo = 1
                                    testedRootResult = "${orb.value} NO divide a $a0"
                                }
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (orb.value > 0) "+${orb.value}" else "${orb.value}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    !isTapped -> NeonCyan
                                    orb.isValidDivisor -> DarkBackground
                                    else -> TextPrimary
                                }
                            )
                            if (isTapped) {
                                Icon(
                                    imageVector = if (orb.isValidDivisor) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (orb.isValidDivisor) DarkBackground else TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real-time evaluation message
            if (testedRootResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = testedRootResult!!,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (testedRootResult!!.contains("RAÍZ")) EmeraldGreen else NeonGold
                    )
                }
            }
        }
    }

    // Victory Confetti
    ConfettiCelebration(
        isActive = showConfetti,
        onFinished = { showConfetti = false }
    )

    // Victory Dialog
    if (showLevelClearedDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldGreen),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡DIVISORES CAZADOS!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Has dominado el Teorema de Raíces Racionales",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "+${level.xpReward} XP  |  Puntos: $score",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showLevelClearedDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_return_world_map"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text(text = "CONTINUAR", fontWeight = FontWeight.Black, color = DarkBackground)
                    }
                }
            }
        }
    }
}
