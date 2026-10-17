@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.citigrow.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citigrow.ui.theme.*
import com.example.citigrow.voice.*
import org.json.JSONObject

class RegistrationGuide(val language: String, val narrator: FormNarrator?, private val strings: JSONObject, val ids: List<String>) {
    val requesters = ids.associateWith { BringIntoViewRequester() }
    fun label(id: String) = strings.getJSONObject(id).getString("label")
    fun hint(id: String) = strings.getJSONObject(id).getString("hint")
    fun play(only: String? = null) {
        val prompts = (only?.let { listOf(it) } ?: ids).map { VoicePrompt(it, "${label(it)}. ${hint(it)}") }
        narrator?.guide(prompts, language) { id -> requesters[id]?.bringIntoView() }
    }
    fun stop() { narrator?.stop() }
}
val LocalRegistrationGuide = staticCompositionLocalOf<RegistrationGuide?> { null }

@Composable fun NarratedField(id: String, showLabel: Boolean = true, content: @Composable () -> Unit) {
    val guide = LocalRegistrationGuide.current
    val active = guide?.narrator?.state?.let { it.phase == VoicePhase.PLAYING && it.field == id } == true
    val background by animateColorAsState(if (active) BlueTint.copy(alpha = .65f) else Color.Transparent, label = "narration highlight")
    Column(Modifier.fillMaxWidth().then(guide?.requesters?.get(id)?.let { Modifier.bringIntoViewRequester(it) } ?: Modifier)
        .testTag("registration-field-$id").semantics { stateDescription = if (active) "Reading aloud" else "Not reading" }
        .background(background, RoundedCornerShape(18.dp)).border(2.dp, if (active) Blue.copy(alpha = .6f) else Color.Transparent, RoundedCornerShape(18.dp))
        .padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (showLabel && guide != null) Row(verticalAlignment = Alignment.CenterVertically) {
            Text(guide.label(id), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = { guide.play(id) }, enabled = guide.narrator != null) { GrowIcon("volume", tint = Blue, description = "Listen: ${guide.label(id)}") }
        }
        content()
    }
}
