package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    var position: Offset,
    var velocity: Offset,
    val color: Color,
    val initialSize: Float,
    val maxLife: Float,
    var currentLife: Float = 0f
) {
    val isDead: Boolean get() = currentLife >= maxLife
    val progress: Float get() = (currentLife / maxLife).coerceIn(0f, 1f)
    val alpha: Float get() = (1f - progress).coerceAtLeast(0f)
    val size: Float get() = initialSize * (1f - progress * 0.5f)

    fun update(deltaSeconds: Float) {
        currentLife += deltaSeconds
        position = Offset(
            x = position.x + velocity.x * deltaSeconds,
            y = position.y + velocity.y * deltaSeconds + 200f * deltaSeconds * deltaSeconds // gravity
        )
        velocity = Offset(velocity.x * 0.95f, velocity.y * 0.95f)
    }
}

class ParticleManager {
    val particles = mutableListOf<Particle>()

    fun spawnLineClearBurst(center: Offset, colors: List<Color>, count: Int = 25) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 450f + 150f
            val vx = kotlin.math.cos(angle) * speed
            val vy = kotlin.math.sin(angle) * speed
            val color = colors.random()
            val size = Random.nextFloat() * 12f + 6f
            val maxLife = Random.nextFloat() * 0.5f + 0.4f

            particles.add(
                Particle(
                    position = center,
                    velocity = Offset(vx, vy),
                    color = color,
                    initialSize = size,
                    maxLife = maxLife
                )
            )
        }
    }

    fun update(deltaSeconds: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(deltaSeconds)
            if (p.isDead) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        particles.clear()
    }
}
