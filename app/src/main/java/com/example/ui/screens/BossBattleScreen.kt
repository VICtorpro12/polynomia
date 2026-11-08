package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlin.math.abs
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudio
import com.example.model.BossMonster
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BossBattleScreen(
    boss: BossMonster,
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit,
    onBossDefeated: (bossId: String, damage: Int) -> Unit
) {
    var bossHp by remember { mutableIntStateOf(boss.currentHp) }
    var heroHp by remember { mutableIntStateOf(100) }
    var heroMaxHp by remember { mutableIntStateOf(100) }

    val roots = remember { boss.polynomial.findIntegerRoots() }
    val capturedRoots = remember { mutableStateListOf<Int>() }

    var battleLog by remember { mutableStateOf("¡El ${boss.name} ruge con su escudo polinómico!") }
    var floatingCombatText by remember { mutableStateOf<String?>(null) }
    var isBossHit by remember { mutableStateOf(false) }
    var isHeroHit by remember { mutableStateOf(false) }

    var showVictoryDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Shake animation offset
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.snapTo(12f)
            shakeOffset.animateTo(0f, animationSpec = tween(durationMillis = 200))
        }
    }

    LaunchedEffect(bossHp) {
        if (bossHp <= 0 && !showVictoryDialog) {
            audio.playVictory()
            showConfetti = true
            showVictoryDialog = true
            onBossDefeated(boss.id, boss.maxHp)
        }
    }

    LaunchedEffect(heroHp) {
        if (heroHp <= 0 && !showGameOverDialog) {
            audio.playError()
            showGameOverDialog = true
        }
    }

    fun heroAttackRoot(rootCandidate: Int) {
        val eval = boss.polynomial.evaluate(rootCandidate.toDouble())
        if (eval == 0.0) {
            // Hit!
            audio.playHit()
            isBossHit = true
            triggerShake()
            val damage = 50
            bossHp = (bossHp - damage).coerceAtLeast(0)
            capturedRoots.add(rootCandidate)
            floatingCombatText = "¡GOLPE CRÍTICO! -$damage HP"
            battleLog = "¡Acertaste la raíz x = $rootCandidate! El escudo de ${boss.name} se quiebra."
        } else {
            // Missed! Boss counter attack
            audio.playError()
            isHeroHit = true
            val bossDmg = 20
            heroHp = (heroHp - bossDmg).coerceAtLeast(0)
            floatingCombatText = "¡FALLO! P($rootCandidate) ≠ 0. Contraataque: -$bossDmg HP"
            battleLog = "x = $rootCandidate no anula el polinomio. ¡${boss.name} usó ${boss.attackName}!"
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
            title = "Coliseo: ${boss.name}",
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
            // Boss HP Bar & Avatar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.toInt(), 0) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, RubyRed.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = boss.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = RubyRed
                            )
                            Text(
                                text = boss.title,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Monster Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(RubyRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = RubyRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Boss HP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "BOSS HP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = RubyRed)
                        Text(text = "$bossHp / ${boss.maxHp}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (bossHp.toFloat() / boss.maxHp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = RubyRed,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Boss Polynomial Shield
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ESCUDO: ${boss.polynomial.toFormattedString()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Floating Combat Text
            if (floatingCombatText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (floatingCombatText!!.contains("CRÍTICO")) EmeraldGreen else RubyRed)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = floatingCombatText!!,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkBackground
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Hero Status Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "HÉROE HP: $heroHp / $heroMaxHp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }

                    LinearProgressIndicator(
                        progress = { (heroHp.toFloat() / heroMaxHp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldGreen,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Combat Log Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = battleLog,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Combat Actions Grid
            Text(
                text = "ELIGE TU ATAQUE (RAÍZ DE RUFFINI)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Candidate Root Attack Buttons
            val divisors = boss.polynomial.getPossibleDivisors().filter { abs(it) <= 4 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                divisors.take(4).forEach { cand ->
                    Button(
                        onClick = { heroAttackRoot(cand) },
                        modifier = Modifier.weight(1f).testTag("btn_attack_root_$cand"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(
                            text = if (cand > 0) "+$cand" else "$cand",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                divisors.drop(4).take(4).forEach { cand ->
                    Button(
                        onClick = { heroAttackRoot(cand) },
                        modifier = Modifier.weight(1f).testTag("btn_attack_root_$cand"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)
                    ) {
                        Text(
                            text = if (cand > 0) "+$cand" else "$cand",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Special Skill: Heal potion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        audio.playTransform()
                        heroHp = (heroHp + 30).coerceAtMost(heroMaxHp)
                        battleLog = "¡Poción de Euler usada! Recuperaste +30 HP"
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Icon(imageVector = Icons.Default.Healing, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "POCIÓN DE VIDA (+30 HP)", fontWeight = FontWeight.Black, color = DarkBackground)
                }
            }
        }
    }

    // Confetti on Boss Defeated
    ConfettiCelebration(
        isActive = showConfetti,
        onFinished = { showConfetti = false }
    )

    // Boss Defeated Dialog
    if (showVictoryDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, RubyRed),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonGold, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "¡JEFE DERROTADO!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = RubyRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Has destruido el ${boss.name} usando el poder del Álgebra",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "+300 XP  |  +50 Gemas 💎",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showVictoryDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_close_boss_victory"),
                        colors = ButtonDefaults.buttonColors(containerColor = RubyRed)
                    ) {
                        Text(text = "RECLAMAR RECOMPENSA", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    // Game Over Dialog
    if (showGameOverDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, RubyRed),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "💀", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "HAS CAÍDO EN COMBATE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = RubyRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Revisa los divisores del término independiente antes de atacar.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showGameOverDialog = false
                            heroHp = 100
                            bossHp = boss.maxHp
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_retry_boss"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(text = "REINTENTAR COMBATE", fontWeight = FontWeight.Black, color = DarkBackground)
                    }
                }
            }
        }
    }
}
