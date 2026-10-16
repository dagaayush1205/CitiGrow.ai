package com.example.citigrow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.citigrow.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable fun JourneyVisual() {
    Canvas(Modifier.fillMaxWidth().height(110.dp).semantics { contentDescription = "A winding path connects discovery, learning and livelihood." }) {
        val y = size.height * 0.5f
        val path = Path().apply { moveTo(20f, y); cubicTo(size.width * .3f, -y * .3f, size.width * .65f, size.height * 1.4f, size.width - 20f, y) }
        drawPath(path, Color(0xFFE6F0FA), style = Stroke(32f))
        drawPath(path, Blue.copy(alpha = .4f), style = Stroke(3f))
        listOf(.1f to .32f, .5f to .55f, .9f to .72f).forEachIndexed { i, (x, cy) ->
            val center = Offset(size.width * x, size.height * cy)
            drawCircle(listOf(Blue, TealInk, Color(0xFFF0B429))[i], 24f, center)
            drawCircle(Color.White, 8f, center)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Discover", "Learn", "Livelihood").forEach { Text(it, style = MaterialTheme.typography.labelLarge, color = TealInk) }
    }
}

@Composable fun SkillRadar(levels: List<Int>) {
    Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = "Skills Compass chart. Exact skill levels follow below." }) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * .43f
        fun point(i: Int, fraction: Float): Offset {
            val angle = -Math.PI / 2 + i * Math.PI / 3
            return Offset(center.x + (cos(angle) * radius * fraction).toFloat(), center.y + (sin(angle) * radius * fraction).toFloat())
        }
        fun polygon(fractions: List<Float>) = Path().apply {
            fractions.forEachIndexed { i, fraction -> val p = point(i, fraction); if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }; close()
        }
        (1..3).forEach { ring -> drawPath(polygon(List(6) { ring / 3f }), Blue.copy(alpha = .15f), style = Stroke(2f)) }
        (0..5).forEach { drawLine(Blue.copy(alpha = .15f), center, point(it, 1f), 2f) }
        val result = polygon((0..5).map { (levels.getOrNull(it) ?: 1).coerceIn(1, 3) / 3f })
        drawPath(result, TealInk.copy(alpha = .22f)); drawPath(result, TealInk, style = Stroke(4f))
        (0..5).forEach { drawCircle(TealInk, 5f, point(it, (levels.getOrNull(it) ?: 1) / 3f)) }
    }
}
