package com.example.citigrow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.selected
import org.json.JSONObject
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.citigrow.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable fun JourneyVisual() {
    val context = LocalContext.current
    val icons = remember { ReferenceIcons.load(context.assets) }
    Canvas(Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = "A road from books to skills to a job to a star." }) {
        scale(size.width / 390f, size.height / 150f, pivot = Offset.Zero) {
            drawCircle(GoldFill, 18f, Offset(338f, 34f))
            val hills = Path().apply { moveTo(0f,108f); cubicTo(80f,68f,160f,70f,230f,98f); cubicTo(300f,126f,340f,78f,390f,90f); lineTo(390f,150f); lineTo(0f,150f); close() }
            drawPath(hills, Blue)
            val foreground = Path().apply { moveTo(0f,126f); cubicTo(90f,98f,180f,104f,260f,124f); cubicTo(340f,144f,350f,118f,390f,120f); lineTo(390f,150f); lineTo(0f,150f); close() }
            drawPath(foreground, Teal)
            val road = Path().apply { moveTo(40f,146f); cubicTo(70f,126f,100f,104f,140f,100f); cubicTo(180f,96f,210f,104f,246f,84f); cubicTo(282f,64f,300f,62f,336f,66f) }
            drawPath(road, Cloud, style = Stroke(4f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f,9f))))
            listOf(Triple(64f,128f,"book"),Triple(146f,99f,"wrench"),Triple(246f,84f,"briefcase"),Triple(336f,66f,"star")).forEach { (x,y,icon) ->
                drawCircle(if (icon == "star") GoldFill else Color.White, if (icon == "star") 17f else 16f, Offset(x,y))
                translate(x-9f,y-9f) { scale(.75f,.75f,pivot=Offset.Zero) { drawPath(PathParser().parsePathString(icons.getString(icon)).toPath(), Navy, style=Stroke(2.4f,cap=StrokeCap.Round)) } }
            }
        }
    }
}

/** Coordinates and Bézier control points mirror vmStudent's Life Map in the reference. */
@Composable fun RoadMap(stage: Int, selected: Int, select: (Int) -> Unit) {
    val x = listOf(.22f, .72f, .30f, .74f, .26f, .70f, .40f)
    val labels = listOf("Explore", "Eligibility", "Skills check", "Foundations", "Study plan", "Dates", "First job")
    BoxWithConstraints(Modifier.fillMaxWidth().height(668.dp)) {
        val width = maxWidth
        Canvas(Modifier.matchParentSize()) {
            fun path(to: Int) = Path().apply {
                moveTo(x[0] * size.width, 44.dp.toPx())
                for (i in 1..to) cubicTo(x[i-1] * size.width, (44+(i-1)*92+46).dp.toPx(), x[i] * size.width, (44+i*92-46).dp.toPx(), x[i] * size.width, (44+i*92).dp.toPx())
            }
            drawPath(path(6), InputBorder, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(),10.dp.toPx()))))
            if (stage > 0) drawPath(path(stage.coerceAtMost(6)), Teal, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
            drawLine(GoldFill, Offset(x[3] * size.width,320.dp.toPx()), Offset(.19f*size.width,320.dp.toPx()), strokeWidth = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(),8.dp.toPx())))
        }
        labels.forEachIndexed { index, title ->
            Column(Modifier.offset(x = width * x[index] - 60.dp, y = (22 + index * 92).dp).width(120.dp).clickable { select(index) }.semantics { contentDescription = "${index+1}. $title"; this.selected = selected == index }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = CircleShape, color = if (index < stage) Teal else if (index == stage) Blue else Color.White,
                    border = BorderStroke(if (selected == index) 3.dp else 2.dp, if (selected == index) GoldFill else if (index <= stage) Color.Transparent else InputBorder)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        if (index < stage) GrowIcon("check", tint = Navy) else if (index > stage) GrowIcon("lock", Modifier.size(16.dp), Muted) else Text((index+1).toString(), color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text(title, modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = if (index > stage) Muted else Navy)
                if (index == stage) Badge("You are here", Blue, Color.White)
            }
        }
        Surface(onClick = { select(7) }, modifier = Modifier.offset(x = width * .19f - 56.dp, y = 302.dp).width(112.dp), shape = RoundedCornerShape(12.dp), color = GoldSurface, border = BorderStroke(2.dp, GoldFill)) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { GrowIcon("swap", Modifier.size(16.dp), GoldInk); Text("Alternate route", fontSize = 12.sp, color = GoldInk, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable fun WeekBars(minutes: List<Int>, goal: Int? = null) {
    val maximum = maxOf(goal ?: 0, minutes.maxOrNull() ?: 1, 1)
    Box(Modifier.fillMaxWidth().height(128.dp)) {
        if (goal != null) Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            val y = size.height * (1 - goal.toFloat()/maximum)
            drawLine(GoldFill, Offset(0f,y), Offset(size.width,y), 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(),5.dp.toPx())))
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEachIndexed { index, day ->
                val value = minutes.getOrNull(index) ?: 0
                val color = if (goal != null) { if (value >= goal) Teal else Blue } else listOf(Blue,Teal,Blue,GoldFill,Blue,Teal,Border)[index]
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (value == 0) "—" else "${value}m", style = MaterialTheme.typography.labelSmall, color = Navy)
                    Box(Modifier.widthIn(max = 28.dp).fillMaxWidth().height(maxOf(6f, value.toFloat()/maximum*72).dp).background(if (value == 0) Border else color, RoundedCornerShape(8.dp)))
                    Text(day, fontSize = 10.sp, color = Muted)
                }
            }
        }
    }
    if (goal != null) Note("Gold line · ${goal}m daily goal")
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
