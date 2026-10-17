package com.example.citigrow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citigrow.data.*
import com.example.citigrow.ui.theme.*
import org.json.JSONObject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun RegistrationDropdown(label: String, value: String, options: List<Pair<String, String>>, select: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val guide = LocalRegistrationGuide.current
    val displayed = options.firstOrNull { it.first == value }?.second.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (guide == null) Text(translated(label), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (it) guide?.stop(); expanded = it }) {
            OutlinedTextField(
                value = translated(displayed), onValueChange = {}, readOnly = true, singleLine = true,
                modifier = Modifier.menuAnchor().fillMaxWidth().semantics { contentDescription = label },
                shape = RoundedCornerShape(14.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = InputBorder, focusedBorderColor = Blue)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                options.forEach { (key, title) ->
                    DropdownMenuItem(text = { Text(translated(title)) }, onClick = { expanded = false; select(key) },
                        trailingIcon = { if (key == value) GrowIcon("check", tint = Blue) })
                }
            }
        }
    }
}

@Composable private fun RegistrationInput(id: String, value: String, secret: Boolean = false, numeric: Boolean = false, change: (String) -> Unit) {
    val guide = requireNotNull(LocalRegistrationGuide.current)
    NarratedField(id) { Entry(guide.label(id), value, secret, numeric, showLabel = false, onFocused = guide::stop, change = change) }
}
@Composable private fun RegistrationSelect(id: String, value: String, options: List<Pair<String, String>>, change: (String) -> Unit) {
    val guide = requireNotNull(LocalRegistrationGuide.current)
    NarratedField(id) { RegistrationDropdown(guide.label(id), value, options, change) }
}

@Composable fun LanguageScreen(s: AppSession) {
    Column(Modifier.fillMaxWidth().background(Navy).padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(64.dp).background(Teal, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) { GrowIcon("sprout", Modifier.size(40.dp), Navy) }
        Row(Modifier.padding(top = 4.dp)) {
            Text("CitiGrow", style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text(".ai", style = MaterialTheme.typography.displayLarge, color = TealLight)
        }
        Text(s.t("tagline"), color = Color(0xFFD9E2EC), fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 310.dp).padding(horizontal = 4.dp))
        JourneyVisual()
    }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { GrowIcon("languages", tint = Blue); SectionTitle(s.t("chooseLang")) }
        Column(Modifier.fillMaxWidth().semantics { selectableGroup() }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(Triple("en", "English", "English"), Triple("hi", "हिन्दी", "Hindi"), Triple("ta", "தமிழ்", "Tamil"), Triple("ml", "മലയാളം", "Malayalam")).chunked(2).forEach { languages ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    languages.forEach { (code, name, subtitle) ->
                        val selected = s.store.state.text("lang") == code
                        Surface(modifier = Modifier.weight(1f).heightIn(min = 76.dp).selectable(selected, role = Role.RadioButton) { s.store.change { s.store.state.put("lang", code) } },
                            color = if (selected) BlueTint else Color.White, border = BorderStroke(2.dp, if (selected) Blue else Border), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(horizontal = 4.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(name, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Navy); Note(subtitle)
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { GrowIcon("languages", Modifier.size(16.dp), Blue); Box(Modifier.weight(1f)) { Note(s.t("bhashiniNote")) } }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { s.go("roles") }, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), shape = RoundedCornerShape(16.dp)) {
            Text(s.t("continue"), fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); GrowIcon("chevR")
        }
    }
}
@Composable fun RolesScreen(s: AppSession) {
    Heading(s.t("chooseStart")); Note(s.t("startSub"))
    ActionRow(s.t("begin"), s.t("role_student_d"), "sprout", selected = true) { s.go("profiles") }
    ActionRow(s.t("imParent"), s.t("role_parent_d"), "users", Mint, TealInk) { s.go("pin/parent") }
    ActionRow(s.t("imTeacher"), s.t("role_teacher_d"), "pres", Gold, GoldInk) { s.go("pin/teacher") }
    ActionRow(s.t("role_partner"), s.t("role_partner_d"), "building", PurpleTint, PurpleInk) { s.go("partner") }
    Expand("Demo access") { Note("Ram: 1111 · Lakshman: 2222\nParent: 3333 · Teacher: 4444\nRole PINs demonstrate navigation, not production identity verification.") }
}
@Composable fun ProfilesScreen(s: AppSession) {
    Heading(s.t("whoLearning"))
    Surface(color = BlueTint, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { GrowIcon("phone", tint = BlueInk); Text(s.t("sharedNote"), style = MaterialTheme.typography.bodyMedium, color = BlueInk, modifier = Modifier.weight(1f)) }
    }
    s.store.profiles.forEach { p ->
        GrowPanel(padding = 14.dp, action = { s.go("pin/${p.text("id")}") }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(p.text("name"), if (p.text("id") == "lakshman") Blue else Teal, if (p.text("id") == "lakshman") Color.White else Navy, 60.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(p.text("name"), style = MaterialTheme.typography.titleLarge)
                    Note("Class ${p.text("edu")} · Age ${p.optInt("age")}")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { GrowIcon("pin", Modifier.size(14.dp), Muted); Box(Modifier.weight(1f)) { Note("${p.text("district")}, ${p.text("state")}") } }
                }
                IconTile("lock", GreyTint, Muted, 40.dp)
            }
        }
    }
    ActionRow(s.t("addStudent"), icon = "plus") { s.go("register") }
}
@Composable fun PinScreen(s: AppSession, target: String, unlocked: (String, String) -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val p = s.store.profiles.firstOrNull { it.text("id") == target }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Avatar(p?.text("name") ?: target, if (target == "teacher") GoldFill else Teal, size = 76.dp)
        Heading(p?.text("name") ?: target.replaceFirstChar { it.uppercase() })
        Note(s.t("enterPin"))
        BasicTextField(value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(4); error = "" }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), cursorBrush = SolidColor(Color.Transparent), textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier.fillMaxWidth().height(44.dp).semantics { contentDescription = "PIN" }, decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(1.dp).alpha(0f)) { inner() }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { repeat(4) { index -> Surface(Modifier.size(16.dp), shape = CircleShape, color = if (index < pin.length) Blue else Color.White, border = BorderStroke(2.dp, if (index < pin.length) Blue else Color(0xFFC9D6E3))) {} } }
                }
            })
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "Delete")).forEach { keys ->
            Row(Modifier.widthIn(max = 290.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { digit ->
                    if (digit.isEmpty()) Spacer(Modifier.weight(1f)) else Surface(onClick = { if (digit == "Delete") pin = pin.dropLast(1) else if (pin.length < 4) pin += digit; error = "" }, modifier = Modifier.weight(1f).height(62.dp), shape = RoundedCornerShape(20.dp), color = if (digit == "Delete") Color.Transparent else Color.White, shadowElevation = if (digit == "Delete") 0.dp else 1.dp) {
                        Box(contentAlignment = Alignment.Center) { if (digit == "Delete") GrowIcon("del", description = "Delete digit") else Text(digit, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy) }
                    }
                }
            }
        }
        Note("Private PIN · voice cannot unlock a profile")
    }
    if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
    val lockedUntil = s.store.state.optLong("lockUntil:$target")
    Primary("Unlock", pin.length == 4) {
        if (System.currentTimeMillis() < lockedUntil) error = "Please wait a minute before trying again."
        else {
            val valid = when (target) { "parent" -> pin == "3333"; "teacher" -> pin == "4444"; else -> p != null && s.store.unlock(p, pin) }
            if (valid) {
                s.store.change { s.store.state.remove("attempts:$target"); s.store.state.remove("lockUntil:$target") }
                unlocked(p?.text("id") ?: "", if (p != null) "home" else target)
            } else {
                s.store.change {
                    val count = s.store.state.optInt("attempts:$target") + 1
                    s.store.state.put("attempts:$target", count)
                    if (count >= 5) { s.store.state.put("lockUntil:$target", System.currentTimeMillis() + 60_000); s.store.state.put("attempts:$target", 0) }
                }
                error = "Incorrect PIN. After five attempts, wait one minute."
            }
        }
    }
}
@Composable fun RegisterScreen(s: AppSession, assisted: Boolean, created: (String) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }; var age by rememberSaveable { mutableStateOf("") }
    var edu by rememberSaveable { mutableStateOf("9") }; var state by rememberSaveable { mutableStateOf("Tamil Nadu") }
    var district by rememberSaveable { mutableStateOf("") }; var pin by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("30") }; var phone by rememberSaveable { mutableStateOf(if (assisted) "teacher" else "shared") }
    var internet by rememberSaveable { mutableStateOf("sometimes") }; var time by rememberSaveable { mutableStateOf("evening") }
    var guardian by rememberSaveable { mutableStateOf("") }; var consent by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(false) }
    val language = s.store.state.text("lang", "en")
    val context = LocalContext.current
    val strings = remember { JSONObject(context.assets.open("tts/registration_prompts.json").bufferedReader().use { it.readText() }) }
    val minor = (age.toIntOrNull() ?: 0) < 18
    val guide = remember(step, language, minor && step == 2, s.narrator) {
        val ids = when (step) {
            0 -> listOf("intro0", "name", "age", "education", "state", "district", "continue")
            1 -> listOf("intro1", "language", "phone", "internet", "time", "minutes", "pin", "continue")
            else -> listOf("intro2", "privacyInfo") + (if (minor) listOf("guardian", "guardianConsent") else emptyList()) + listOf("privacy", "privacyLink", "create")
        }
        RegistrationGuide(language, s.narrator, strings.getJSONObject(language), ids)
    }
    LaunchedEffect(step, language) { withFrameNanos { }; guide.play() }
    DisposableEffect(step, language) { onDispose { guide.stop() } }
    CompositionLocalProvider(LocalRegistrationGuide provides guide) {
    NarratedField("intro$step", showLabel = false) { Heading(guide.label("intro$step")); Note(guide.hint("intro$step")) }
    if (assisted) Note("Teacher-assisted profile · the student keeps their own PIN")
    Note("Step ${step + 1} of 3 · stored on this device")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { guide.play() }) { GrowIcon("volume", tint = Blue); Spacer(Modifier.width(6.dp)); Text(s.t("readThisStep")) }
        TextButton(onClick = guide::stop) { Text(s.t("stopReading")) }
    }
    LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
    when (step) {
        0 -> {
            RegistrationInput("name", name) { name = it.take(60) }; RegistrationInput("age", age, numeric = true) { age = it.filter(Char::isDigit).take(2) }
            RegistrationSelect("education", edu, (8..12).map { it.toString() to "Class $it" } + listOf("iti" to "ITI", "college" to "College / graduation")) { edu = it }
            RegistrationSelect("state", state, s.store.regions.map { it to it }) { state = it }
            RegistrationInput("district", district) { district = it.take(60) }
        }
        1 -> {
            RegistrationSelect("language", language, listOf("en" to "English", "hi" to "हिन्दी", "ta" to "தமிழ்", "ml" to "മലയാളം")) { value -> s.store.change { s.store.state.put("lang", value) } }
            RegistrationSelect("phone", phone, listOf("shared" to "Shared family phone", "own" to "Own phone", "teacher" to "Teacher's device")) { phone = it }
            RegistrationSelect("internet", internet, listOf("reliable" to "Reliable", "sometimes" to "Sometimes available", "limited" to "Limited connectivity")) { internet = it }
            RegistrationSelect("time", time, listOf("morning" to "Morning", "afternoon" to "Afternoon", "evening" to "Evening")) { time = it }
            RegistrationSelect("minutes", minutes, listOf("15", "30", "45", "60").map { it to "$it minutes" }) { minutes = it }
            RegistrationInput("pin", pin, secret = true) { pin = it.filter(Char::isDigit).take(4) }
        }
        2 -> {
            NarratedField("privacyInfo") { Note(guide.hint("privacyInfo")) }
            if (minor) {
                RegistrationInput("guardian", guardian) { guardian = it.take(80) }
                NarratedField("guardianConsent", showLabel = false) {
                    Toggle(guide.label("guardianConsent"), consent) { guide.stop(); consent = it }
                    Note(guide.hint("guardianConsent"))
                }
            }
            NarratedField("privacy", showLabel = false) { Toggle(guide.label("privacy"), privacy) { guide.stop(); privacy = it } }
            NarratedField("privacyLink", showLabel = false) { Secondary(guide.label("privacyLink")) { guide.stop(); s.go("privacy") } }
        }
    }
    val valid = when (step) { 0 -> name.isNotBlank() && age.toIntOrNull() in 13..25 && district.isNotBlank() && state in s.store.regions; 1 -> pin.length == 4; else -> privacy && ((age.toIntOrNull() ?: 0) >= 18 || (consent && guardian.isNotBlank())) }
    NarratedField(if (step < 2) "continue" else "create", showLabel = false) {
    Primary(guide.label(if (step < 2) "continue" else "create"), valid) {
        guide.stop()
        if (step < 2) step++ else {
            val id = UUID.randomUUID().toString()
            val p = JSONObject().put("id", id).put("name", name.trim()).put("age", age.toInt()).put("edu", edu).put("state", state)
                .put("district", district.trim()).put("study", minutes.toInt()).put("lang", s.store.state.text("lang")).put("phone", phone)
                .put("internet", internet).put("learnTime", time).put("guardian", guardian).put("guardianDemoConsent", consent)
                .put("studentId", "CG-DEMO-${id.take(6).uppercase()}").put("readAloud", true).put("income", "unknown").put("chatStep", 0)
                .put("consent", JSONObject().put("teacher", false).put("parent", false))
            s.store.setPin(p, pin)
            s.store.change("Profile created") { s.store.state.arr("profiles").put(p) }; created(id)
        }
    }
    }
    if (step > 0) Secondary(s.t("back")) { guide.stop(); step-- }
    }
}
