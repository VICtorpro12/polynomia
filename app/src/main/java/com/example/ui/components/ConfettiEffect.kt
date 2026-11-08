package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiCelebration(
    isActive: Boolean,
    onFinished: () -> Unit = {}
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD129), // Gold
            Color(0xFF00F0FF), // Cyan
            Color(0xFFB353FF), // Purple
            Color(0xFF00E676), // Green
            Color(0xFFFF2A6D)  // Ruby
        )
        List(65) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat() * 0.3f,
                velocityX = (Random.nextFloat() - 0.5f) * 600f,
                velocityY = Random.nextFloat() * 900f + 300f,
                color = colors.random(),
                size = Random.nextFloat() * 14f + 8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        for (p in particles) {
            val currentX = (p.initialX * w + p.velocityX * t).coerceIn(0f, w)
            val currentY = p.initialY * h + p.velocityY * t + 0.5f * 980f * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)
            val currentRot = p.rotationSpeed * t

            rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(currentX - p.size / 2, currentY - p.size / 2),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
