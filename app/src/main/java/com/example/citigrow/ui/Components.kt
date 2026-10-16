package com.example.citigrow.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.citigrow.ui.theme.*
import com.example.citigrow.data.GrowStore
val LocalGrowStore = staticCompositionLocalOf<GrowStore?> { null }
@Composable private fun translated(text: String) = LocalGrowStore.current?.t(text) ?: text
@Composable fun Heading(text: String) { Text(translated(text), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy) }
@Composable fun Note(text: String) { Text(translated(text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable fun GrowCard(title: String, detail: String = "", action: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().then(if (action != null) Modifier.clickable(onClick = action) else Modifier),
        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(translated(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (detail.isNotBlank()) Note(detail)
            content()
        }
    }
}
@Composable fun Primary(text: String, enabled: Boolean = true, click: () -> Unit) {
    Button(onClick = click, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Text(translated(text)) }
}
@Composable fun Secondary(text: String, enabled: Boolean = true, click: () -> Unit) {
    OutlinedButton(onClick = click, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp)) { Text(translated(text)) }
}
@Composable fun Entry(label: String, value: String, secret: Boolean = false, numeric: Boolean = false, change: (String) -> Unit) {
    OutlinedTextField(value, onValueChange = change, label = { Text(translated(label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric || secret) KeyboardType.NumberPassword else KeyboardType.Text))
}
@Composable fun Choices(options: List<String>, selected: String, choose: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option -> FilterChip(selected == option, onClick = { choose(option) }, label = { Text(translated(option)) }, modifier = Modifier.heightIn(min = 48.dp)) }
    }
}
@Composable fun Toggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val description = translated(label)
        Text(description, modifier = Modifier.weight(1f).padding(top = 12.dp, end = 12.dp))
        Switch(checked, onCheckedChange = change, modifier = Modifier.semantics { contentDescription = description })
    }
}
@Composable fun Expand(title: String, content: @Composable ColumnScope.() -> Unit) {
    var open by remember { mutableStateOf(false) }
    GrowCard("${if (open) "−" else "+"} $title", action = { open = !open }) { if (open) content() }
}
