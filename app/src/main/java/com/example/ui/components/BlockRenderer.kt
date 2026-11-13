package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.BlockShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val bombTextPaint: Paint by lazy {
    Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
}

/**
 * Draws a modern, shiny 3D glossy jewel/acrylic block with soft shadow,
 * rich multi-stop bevel gradient, specular glass sheen, and crisp rim light.
 */
fun DrawScope.drawShinyBlock(
    topLeft: Offset,
    size: Size,
    color: Color,
    bombCountdown: Int? = null,
    isObstacle: Boolean = false,
    alpha: Float = 1f,
    scale: Float = 1f,
    glowIntensity: Float = 0f
) {
    if (size.width <= 1f || size.height <= 1f) return

    val scaledW = size.width * scale
    val scaledH = size.height * scale
    val cx = topLeft.x + size.width / 2f
    val cy = topLeft.y + size.height / 2f
    val tl = Offset(cx - scaledW / 2f, cy - scaledH / 2f)
    val sz = Size(scaledW, scaledH)

    val cornerPx = (scaledW * 0.22f).coerceIn(4f, 14f)
    val corner = CornerRadius(cornerPx, cornerPx)

    val baseColor = if (isObstacle) Color(0xFF607D8B) else color
    val highlightColor = lerp(baseColor, Color.White, if (isObstacle) 0.22f else 0.36f)
    val midColor = baseColor
    val shadowColor = lerp(baseColor, Color.Black, if (isObstacle) 0.36f else 0.30f)

    // Optional outer glow (e.g., for line-clear preview or active highlight)
    if (glowIntensity > 0.01f) {
        drawRoundRect(
            color = baseColor.copy(alpha = (0.45f * glowIntensity * alpha).coerceIn(0f, 1f)),
            topLeft = Offset(tl.x - 3f, tl.y - 3f),
            size = Size(sz.width + 6f, sz.height + 6f),
            cornerRadius = CornerRadius(cornerPx + 3f, cornerPx + 3f)
        )
    }

    // Soft tactile drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.28f * alpha),
        topLeft = Offset(tl.x + 1.5f, tl.y + 3f),
        size = sz,
        cornerRadius = corner
    )

    // Main 3D diagonal body gradient
    drawRoundRect(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to highlightColor.copy(alpha = alpha),
                0.48f to midColor.copy(alpha = alpha),
                1.0f to shadowColor.copy(alpha = alpha)
            ),
            start = tl,
            end = Offset(tl.x + sz.width, tl.y + sz.height)
        ),
        topLeft = tl,
        size = sz,
        cornerRadius = corner
    )

    // Inner beveled facet frame (subtle 3D raised crystal cushion)
    val inset = (sz.width * 0.11f).coerceIn(2f, 6f)
    if (sz.width > inset * 2.5f) {
        val innerTl = Offset(tl.x + inset, tl.y + inset)
        val innerSz = Size(sz.width - inset * 2f, sz.height - inset * 2f)
        val innerCorner = CornerRadius((cornerPx - inset * 0.5f).coerceAtLeast(2f))
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    highlightColor.copy(alpha = 0.45f * alpha),
                    midColor.copy(alpha = 0.2f * alpha),
                    shadowColor.copy(alpha = 0.38f * alpha)
                ),
                startY = innerTl.y,
                endY = innerTl.y + innerSz.height
            ),
            topLeft = innerTl,
            size = innerSz,
            cornerRadius = innerCorner
        )
    }

    // Glossy specular glass reflection across top half
    val clipRounded = Path().apply {
        addRoundRect(RoundRect(Rect(tl, sz), corner))
    }
    clipPath(clipRounded) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = (if (isObstacle) 0.22f else 0.52f) * alpha),
                    Color.White.copy(alpha = 0.08f * alpha),
                    Color.Transparent
                ),
                startY = tl.y,
                endY = tl.y + sz.height * 0.52f
            ),
            topLeft = Offset(tl.x + sz.width * 0.06f, tl.y + 1.5f),
            size = Size(sz.width * 0.88f, sz.height * 0.46f),
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )

        // Crisp top-left specular gleam dot
        val glintRadius = (sz.width * 0.11f).coerceIn(2f, 5.5f)
        drawCircle(
            color = Color.White.copy(alpha = (if (isObstacle) 0.30f else 0.70f) * alpha),
            radius = glintRadius,
            center = Offset(tl.x + sz.width * 0.22f, tl.y + sz.height * 0.22f)
        )

        // Subtle bottom bounce rim light
        drawRoundRect(
            color = Color.White.copy(alpha = 0.18f * alpha),
            topLeft = Offset(tl.x + sz.width * 0.14f, tl.y + sz.height - inset * 1.1f),
            size = Size(sz.width * 0.72f, (inset * 0.65f).coerceAtLeast(1.5f)),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // Crisp rim border (brighter on top-left, subtle overall)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f * alpha),
                Color.White.copy(alpha = 0.15f * alpha),
                Color.Black.copy(alpha = 0.25f * alpha)
            ),
            start = tl,
            end = Offset(tl.x + sz.width, tl.y + sz.height)
        ),
        topLeft = tl,
        size = sz,
        cornerRadius = corner,
        style = Stroke(width = (sz.width * 0.045f).coerceIn(1f, 2.2f))
    )

    // Obstacle diagonal metallic X accent
    if (isObstacle) {
        val pad = sz.width * 0.28f
        val strokeW = (sz.width * 0.08f).coerceIn(1.5f, 3.5f)
        drawLine(
            color = Color.White.copy(alpha = 0.32f * alpha),
            start = Offset(tl.x + pad, tl.y + pad),
            end = Offset(tl.x + sz.width - pad, tl.y + sz.height - pad),
            strokeWidth = strokeW
        )
        drawLine(
            color = Color.White.copy(alpha = 0.32f * alpha),
            start = Offset(tl.x + sz.width - pad, tl.y + pad),
            end = Offset(tl.x + pad, tl.y + sz.height - pad),
            strokeWidth = strokeW
        )
    }

    // Bomb countdown badge & number
    if (bombCountdown != null) {
        val badgeRadius = sz.width * 0.36f
        val badgeColor = if (bombCountdown <= 3) Color(0xFFFF1744) else Color(0xFFD50000)

        drawCircle(
            color = badgeColor.copy(alpha = alpha),
            radius = badgeRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f * alpha),
            radius = badgeRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        drawContext.canvas.nativeCanvas.apply {
            bombTextPaint.textSize = sz.height * 0.46f
            val yOffset = -(bombTextPaint.descent() + bombTextPaint.ascent()) / 2f
            drawText(bombCountdown.toString(), cx, cy + yOffset, bombTextPaint)
        }
    }
}

/**
 * Draws a modern, shiny 3D glossy hexagonal block.
 */
fun DrawScope.drawShinyHexBlock(
    center: Offset,
    radius: Float,
    color: Color,
    isFilled: Boolean,
    borderColor: Color,
    alpha: Float = 1f,
    glowIntensity: Float = 0f
) {
    val hexPath = buildHexPath(center, radius)

    if (!isFilled) {
        drawPath(path = hexPath, color = color.copy(alpha = alpha), style = Fill)
        drawPath(path = hexPath, color = borderColor.copy(alpha = alpha), style = Stroke(width = 1.5f))
        return
    }

    val highlightColor = lerp(color, Color.White, 0.35f)
    val shadowColor = lerp(color, Color.Black, 0.30f)

    if (glowIntensity > 0.01f) {
        val glowPath = buildHexPath(center, radius * 1.1f)
        drawPath(
            path = glowPath,
            color = color.copy(alpha = (0.45f * glowIntensity * alpha).coerceIn(0f, 1f)),
            style = Fill
        )
    }

    // Drop shadow
    val shadowPath = buildHexPath(Offset(center.x + 1.5f, center.y + 2.5f), radius)
    drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.25f * alpha), style = Fill)

    // 3D gradient body
    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            colors = listOf(highlightColor.copy(alpha = alpha), color.copy(alpha = alpha), shadowColor.copy(alpha = alpha)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        style = Fill
    )

    // Upper gloss reflection inside hex
    clipPath(hexPath) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f * alpha),
                    Color.Transparent
                ),
                startY = center.y - radius,
                endY = center.y
            ),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius)
        )
    }

    // Crisp rim border
    drawPath(
        path = hexPath,
        color = Color.White.copy(alpha = 0.45f * alpha),
        style = Stroke(width = 1.8f)
    )
}

fun buildHexPath(center: Offset, radius: Float): Path {
    return Path().apply {
        for (i in 0 until 6) {
            val angleRad = (60f * i - 30f) * PI.toFloat() / 180f
            val x = center.x + radius * cos(angleRad)
            val y = center.y + radius * sin(angleRad)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

// Pointy-top axial hex <-> pixel conversions
private val SQRT3 = sqrt(3f)

fun hexAxialToPixel(q: Int, r: Int, hexRadius: Float): Offset {
    val x = hexRadius * (SQRT3 * q + SQRT3 / 2f * r)
    val y = hexRadius * (1.5f * r)
    return Offset(x, y)
}

fun pixelToHexAxial(dx: Float, dy: Float, hexRadius: Float): Pair<Int, Int> {
    val qf = (SQRT3 / 3f * dx - 1f / 3f * dy) / hexRadius
    val rf = (2f / 3f * dy) / hexRadius
    return cubeRound(qf, rf)
}

private fun cubeRound(qf: Float, rf: Float): Pair<Int, Int> {
    val sf = -qf - rf
    var rq = qf.roundToInt()
    var rr = rf.roundToInt()
    val rs = sf.roundToInt()

    val qDiff = abs(rq - qf)
    val rDiff = abs(rr - rf)
    val sDiff = abs(rs - sf)

    if (qDiff > rDiff && qDiff > sDiff) {
        rq = -rr - rs
    } else if (rDiff > sDiff) {
        rr = -rq - rs
    }
    return Pair(rq, rr)
}

fun hexPiecePixelCentroid(piece: BlockShape, hexRadius: Float): Offset {
    val coords = piece.normalizedCoordinates
    if (coords.isEmpty()) return Offset.Zero
    var sumX = 0f
    var sumY = 0f
    for ((dq, dr) in coords) {
        val p = hexAxialToPixel(dq, dr, hexRadius)
        sumX += p.x
        sumY += p.y
    }
    return Offset(sumX / coords.size, sumY / coords.size)
}
