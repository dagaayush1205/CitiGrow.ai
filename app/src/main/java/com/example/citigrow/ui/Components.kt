package com.example.citigrow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import com.example.citigrow.ui.theme.*
import com.example.citigrow.data.GrowStore
import org.json.JSONObject
import android.content.res.AssetManager

val LocalGrowStore = staticCompositionLocalOf<GrowStore?> { null }
@Composable fun translated(text: String) = LocalGrowStore.current?.t(text) ?: text

internal object ReferenceIcons {
    @Volatile private var cached: JSONObject? = null
    fun load(assets: AssetManager): JSONObject = cached ?: synchronized(this) {
        cached ?: JSONObject(assets.open("prototype-icons.json").bufferedReader().use { it.readText() }).also { cached = it }
    }
}

/** Original Lucide-style SVG paths, imported verbatim from static I in the prototype. */
@Composable fun GrowIcon(name: String, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, description: String? = null) {
    val context = LocalContext.current
    val icons = remember { ReferenceIcons.load(context.assets) }
    val vector = remember(name) {
        val nodes = PathParser().parsePathString(icons.optString(name, icons.getString("info"))).toNodes()
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(nodes, fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    Icon(vector, description, modifier.size(22.dp), tint)
}
@Composable fun IconTile(icon: String, background: Color = BlueTint, ink: Color = Blue, size: Dp = 44.dp) {
    Box(Modifier.size(size).background(background, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { GrowIcon(icon, Modifier.size(if (size > 48.dp) 28.dp else 22.dp), ink) }
}
@Composable fun Avatar(name: String, color: Color = Teal, ink: Color = Navy, size: Dp = 48.dp) {
    Box(Modifier.size(size).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(1).uppercase(), color = ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    }
}
@Composable fun Heading(text: String) { Text(translated(text), style = MaterialTheme.typography.headlineLarge, color = Navy) }
@Composable fun SectionTitle(text: String) { Text(translated(text), style = MaterialTheme.typography.titleMedium, color = Navy) }
@Composable fun Note(text: String) { Text(translated(text), style = MaterialTheme.typography.bodyMedium, color = LocalContentColor.current.let { if (it == Color.White) Color(0xFFD9E2EC) else Muted }) }
@Composable fun Badge(text: String, background: Color = BlueTint, ink: Color = BlueInk, icon: String? = null) {
    Row(Modifier.background(background, RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (icon != null) GrowIcon(icon, Modifier.size(14.dp), ink)
        Text(translated(text), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ink)
    }
}
@Composable fun GrowPanel(modifier: Modifier = Modifier, background: Color = Color.White, border: Color = Border, borderWidth: Dp = 1.dp,
    padding: Dp = 16.dp, action: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth().then(if (action != null) Modifier.clickable(onClick = action) else Modifier),
        shape = RoundedCornerShape(20.dp), color = background, contentColor = Navy,
        border = BorderStroke(borderWidth, border), shadowElevation = 2.dp) {
        Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable fun GrowCard(title: String, detail: String = "", action: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    GrowPanel(action = action) {
        Text(translated(title), style = MaterialTheme.typography.titleMedium)
        if (detail.isNotBlank()) Note(detail)
        content()
    }
}
@Composable fun Hero(title: String, detail: String = "", icon: String? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    Surface(shape = RoundedCornerShape(24.dp), color = Navy, contentColor = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (icon != null) IconTile(icon, Teal, Navy, 52.dp)
                Text(translated(title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            }
            if (detail.isNotBlank()) Text(translated(detail), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE4ECF4))
            content()
        }
    }
}
@Composable fun ActionRow(title: String, detail: String = "", icon: String = "book", tint: Color = BlueTint, ink: Color = Blue,
    selected: Boolean = false, action: () -> Unit) {
    GrowPanel(background = if (selected) BlueTint else Color.White, border = if (selected) Blue else Border, borderWidth = if (selected) 2.dp else 1.dp, action = action) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile(icon, if (selected) Blue else tint, if (selected) Color.White else ink, 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(translated(title), style = MaterialTheme.typography.titleMedium)
                if (detail.isNotBlank()) Note(detail)
            }
            GrowIcon("chevR", tint = Color(0xFF829AB1))
        }
    }
}
@Composable fun Primary(text: String, enabled: Boolean = true, click: () -> Unit) {
    Button(onClick = click, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White, disabledContainerColor = Border, disabledContentColor = Muted)) {
        Text(translated(text), style = MaterialTheme.typography.labelLarge)
    }
}
@Composable fun Secondary(text: String, enabled: Boolean = true, click: () -> Unit) {
    OutlinedButton(onClick = click, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (enabled) Color(0xFFC9D6E3) else Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Blue), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
        Text(translated(text), style = MaterialTheme.typography.labelLarge)
    }
}
@Composable fun Entry(label: String, value: String, secret: Boolean = false, numeric: Boolean = false, change: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(translated(label), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value, onValueChange = change, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label }, singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = InputBorder, focusedBorderColor = Blue),
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.NumberPassword else if (numeric) KeyboardType.Number else KeyboardType.Text))
    }
}
@Composable fun ChoiceChip(text: String, selected: Boolean, modifier: Modifier = Modifier, horizontalPadding: Dp = 14.dp, pick: () -> Unit) {
    Surface(modifier.heightIn(min = 44.dp).selectable(selected, role = Role.Tab, onClick = pick), shape = RoundedCornerShape(14.dp),
        color = if (selected) Blue else Color.White, contentColor = if (selected) Color.White else Navy,
        border = BorderStroke(2.dp, if (selected) Blue else InputBorder)) {
        Box(Modifier.padding(horizontal = horizontalPadding, vertical = 10.dp), contentAlignment = Alignment.Center) { Text(translated(text), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) }
    }
}
@Composable fun Choices(options: List<String>, selected: String, choose: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option -> ChoiceChip(option, selected == option) { choose(option) } }
    }
}
@Composable fun Toggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        val description = translated(label)
        Text(description, modifier = Modifier.weight(1f).padding(end = 12.dp), style = MaterialTheme.typography.bodyLarge)
        Switch(checked, onCheckedChange = change, modifier = Modifier.semantics { contentDescription = description },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Teal, checkedBorderColor = Teal, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFF9FB3C8), uncheckedBorderColor = Color(0xFF9FB3C8)))
    }
}
@Composable fun Expand(title: String, content: @Composable ColumnScope.() -> Unit) {
    var open by remember { mutableStateOf(false) }
    GrowPanel {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable { open = !open }.semantics { stateDescription = if (open) "Expanded" else "Collapsed" }, verticalAlignment = Alignment.CenterVertically) {
            Text(translated(title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            GrowIcon("chevR", modifier = Modifier.then(if (open) Modifier.rotate(90f) else Modifier), tint = Muted)
        }
        if (open) content()
    }
}
