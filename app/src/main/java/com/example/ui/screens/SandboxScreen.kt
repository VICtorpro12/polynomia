package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudio
import com.example.model.PlayerProfile
import com.example.model.Polynomial
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

@Composable
fun SandboxScreen(
    profile: PlayerProfile,
    audio: GameAudio,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onBackClick: () -> Unit
) {
    // Degree 3 polynomial coefficients: [a3, a2, a1, a0]
    val coeffs = remember { mutableStateListOf(1, -4, 1, 6) }
    var selectedTestRoot by remember { mutableIntStateOf(2) }

    val currentPoly = remember(coeffs.toList()) { Polynomial(coeffs.toList()) }
    val realRoots = remember(currentPoly) { currentPoly.findIntegerRoots() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        GameTopBar(
            profile = profile,
            isAudioMuted = isAudioMuted,
            onToggleAudio = onToggleAudio,
            title = "Laboratorio Libre",
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
            // Formula display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POLINOMIO EXPERIMENTAL f(x)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPoly.toFormattedString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Raíces reales:", fontSize = 11.sp, color = TextSecondary)
                        if (realRoots.isEmpty()) {
                            Text(text = "Sin raíces enteras", fontSize = 11.sp, color = TextMuted)
                        } else {
                            realRoots.forEach { r ->
                                Text(
                                    text = "x = $r",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("x³ - 4x² + x + 6") {
                    audio.playTap()
                    coeffs.clear()
                    coeffs.addAll(listOf(1, -4, 1, 6))
                }
                PresetChip("x³ + 2x² - 5x - 6") {
                    audio.playTap()
                    coeffs.clear()
                    coeffs.addAll(listOf(1, 2, -5, -6))
                }
                PresetChip("x³ - 7x + 6") {
                    audio.playTap()
                    coeffs.clear()
                    coeffs.addAll(listOf(1, 0, -7, 6))
                }
                PresetChip("x³ - 6x² + 11x - 6") {
                    audio.playTap()
                    coeffs.clear()
                    coeffs.addAll(listOf(1, -6, 11, -6))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Coefficient Steppers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AJUSTAR COEFICIENTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        coeffs.forEachIndexed { idx, value ->
                            val power = coeffs.size - 1 - idx
                            val label = if (power > 0) "x${Polynomial.toSuperscript(power)}" else "cte"

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = label, fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                IconButton(
                                    onClick = {
                                        audio.playTap()
                                        coeffs[idx] = coeffs[idx] + 1
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceElevated)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$value",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (value == 0) TextMuted else TextPrimary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        audio.playTap()
                                        coeffs[idx] = coeffs[idx] - 1
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Canvas Graph of the Polynomial
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRÁFICA CARTESIANA: y = f(x)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                        Text(
                            text = "Eje X ∈ [-4, +4]",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF07050E))
                    ) {
                        val w = size.width
                        val h = size.height
                        val midX = w / 2f
                        val midY = h / 2f

                        val scaleX = w / 8f   // 8 units across (-4 to 4)
                        val scaleY = h / 30f  // 30 units vertically

                        // Draw Grid Lines & Axes
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(0f, midY),
                            end = Offset(w, midY),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(midX, 0f),
                            end = Offset(midX, h),
                            strokeWidth = 1.5f
                        )

                        // Draw polynomial curve
                        val path = Path()
                        var firstPoint = true
                        val stepCount = 100
                        for (i in 0..stepCount) {
                            val xVal = -4.0 + (8.0 * i / stepCount)
                            val yVal = currentPoly.evaluate(xVal)

                            val canvasX = (midX + (xVal * scaleX)).toFloat()
                            val canvasY = (midY - (yVal * scaleY)).toFloat().coerceIn(-50f, h + 50f)

                            if (firstPoint) {
                                path.moveTo(canvasX, canvasY)
                                firstPoint = false
                            } else {
                                path.lineTo(canvasX, canvasY)
                            }
                        }

                        drawPath(
                            path = path,
                            color = NeonCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Highlight Roots where curve crosses Y=0
                        realRoots.forEach { root ->
                            if (root in -4..4) {
                                val rootCanvasX = midX + (root * scaleX).toFloat()
                                val rootCanvasY = midY

                                drawCircle(
                                    color = EmeraldGreen,
                                    radius = 6.dp.toPx(),
                                    center = Offset(rootCanvasX, rootCanvasY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(rootCanvasX, rootCanvasY)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Instant Ruffini Breakdown Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIVISIÓN SINTÉTICA EN VIVO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )

                        // Root selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Probar r = ", fontSize = 11.sp, color = TextSecondary)
                            IconButton(
                                onClick = { selectedTestRoot-- },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = NeonCyan)
                            }
                            Text(text = "$selectedTestRoot", fontWeight = FontWeight.Bold, color = NeonGold, fontSize = 13.sp)
                            IconButton(
                                onClick = { selectedTestRoot++ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compute Ruffini table
                    val top = coeffs.toList()
                    val mult = mutableListOf<Int>()
                    val bottom = mutableListOf<Int>()

                    var curr = top[0]
                    bottom.add(curr)
                    mult.add(0)

                    for (i in 1 until top.size) {
                        val m = curr * selectedTestRoot
                        mult.add(m)
                        curr = top[i] + m
                        bottom.add(curr)
                    }

                    val remainder = bottom.last()

                    // Render table
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Root
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "r", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArcanePurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$selectedTestRoot", fontWeight = FontWeight.Bold, color = ArcanePurple)
                            }
                        }

                        top.forEachIndexed { i, coeff ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$coeff", fontSize = 13.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (i == 0) "-" else "${mult[i]}",
                                    fontSize = 11.sp,
                                    color = ArcanePurple
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (i == top.size - 1) {
                                                if (remainder == 0) EmeraldGreen.copy(alpha = 0.3f) else RubyRed.copy(alpha = 0.3f)
                                            } else DarkSurfaceElevated
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${bottom[i]}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (i == top.size - 1 && remainder == 0) EmeraldGreen else NeonCyan
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (remainder == 0) "✅ Resto = 0: ¡$selectedTestRoot es raíz exacta de la ecuación!"
                        else "❌ Resto = $remainder ≠ 0: $selectedTestRoot no es raíz.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainder == 0) EmeraldGreen else RubyRed
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
    }
}
