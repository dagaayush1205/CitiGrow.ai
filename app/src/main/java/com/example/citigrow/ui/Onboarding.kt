package com.example.citigrow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import org.json.JSONObject
import java.util.UUID

@Composable fun LanguageScreen(s: AppSession) {
    Spacer(Modifier.height(24.dp))
    Heading("Discover your next possibility.")
    Note(s.t("tagline"))
    GrowCard("A small step today. A world of possibilities tomorrow.") { JourneyVisual() }
    Heading(s.t("chooseLang"))
    listOf("en" to "English", "hi" to "हिन्दी", "ta" to "தமிழ்").forEach { (code, name) ->
        Secondary("${if (s.store.state.text("lang") == code) "●  " else ""}$name") { s.store.change { s.store.state.put("lang", code) } }
    }
    Note(s.t("bhashiniNote"))
    Primary(s.t("continue")) { s.go("roles") }
}
@Composable fun RolesScreen(s: AppSession) {
    Heading(s.t("chooseStart")); Note(s.t("startSub"))
    Primary(s.t("begin")) { s.go("profiles") }
    Secondary(s.t("imParent")) { s.go("pin/parent") }
    Secondary(s.t("imTeacher")) { s.go("pin/teacher") }
    Secondary("Partner · alumni, NGO or employer") { s.go("partner") }
    Note("Hackathon demo · fictional profiles, simulated AI, local data only.")
    Expand("Demo access") { Note("Ram: 1111 · Lakshman: 2222\nParent: 3333 · Teacher: 4444\nRole PINs demonstrate navigation, not production identity verification.") }
}
@Composable fun ProfilesScreen(s: AppSession) {
    Heading("Who is learning today?"); Note("One family phone. Your own learning journey.")
    s.store.profiles.forEach { p -> GrowCard(p.text("name"), "Class ${p.text("edu")} · ${p.text("district")}", { s.go("pin/${p.text("id")}") }) }
    Secondary("Add another student") { s.go("register") }
}
@Composable fun PinScreen(s: AppSession, target: String, unlocked: (String, String) -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val p = s.store.profiles.firstOrNull { it.text("id") == target }
    Heading("Unlock ${p?.text("name") ?: target}")
    Note("Use your private four-digit PIN. Voice cannot unlock a profile.")
    Entry("PIN", pin, secret = true) { pin = it.filter(Char::isDigit).take(4); error = "" }
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
    Heading(if (assisted) "Create a classroom profile" else "A journey of your own")
    Note("Step ${step + 1} of 3 · stored on this device")
    LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
    when (step) {
        0 -> {
            Entry("Student name", name) { name = it.take(60) }; Entry("Age (13–25)", age, numeric = true) { age = it.filter(Char::isDigit).take(2) }
            Note("Education level"); Choices(listOf("8", "9", "10", "11", "12", "iti", "college"), edu) { edu = it }
            Note("State"); Choices(s.store.data.arr("STATES").strings(), state) { state = it }
            Entry("District", district) { district = it.take(60) }
        }
        1 -> {
            Note("App language"); Choices(listOf("en", "hi", "ta"), s.store.state.text("lang")) { s.store.change { s.store.state.put("lang", it) } }
            Note("Phone access"); Choices(listOf("shared", "own", "teacher"), phone) { phone = it }
            Note("Internet access"); Choices(listOf("reliable", "sometimes", "limited"), internet) { internet = it }
            Note("Learning time"); Choices(listOf("morning", "afternoon", "evening"), time) { time = it }
            Note("Minutes each day"); Choices(listOf("15", "30", "45", "60"), minutes) { minutes = it }
            Entry("Create a four-digit PIN", pin, secret = true) { pin = it.filter(Char::isDigit).take(4) }
        }
        2 -> {
            Note("Demo ID is generated automatically. We do not collect Aadhaar, identity numbers or phone numbers.")
            if ((age.toIntOrNull() ?: 0) < 18) {
                Entry("Guardian name / relationship", guardian) { guardian = it.take(80) }
                Toggle("Guardian agrees to this demo profile", consent) { consent = it }
                Note("This checkbox records demo consent only. Verified guardian consent is required before a real pilot.")
            }
            Toggle("I understand the local demo privacy notice", privacy) { privacy = it }
            Secondary("Read privacy notice") { s.go("privacy") }
        }
    }
    val valid = when (step) { 0 -> name.isNotBlank() && age.toIntOrNull() in 13..25 && district.isNotBlank(); 1 -> pin.length == 4; else -> privacy && ((age.toIntOrNull() ?: 0) >= 18 || (consent && guardian.isNotBlank())) }
    Primary(if (step < 2) s.t("continue") else "Create profile", valid) {
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
    if (step > 0) Secondary(s.t("back")) { step-- }
}
