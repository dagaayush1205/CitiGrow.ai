package com.example.citigrow.ui
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.citigrow.data.*
import org.json.JSONArray

@Composable fun OpportunityScreen(s: AppSession, id: String) {
    val o = s.store.find("OPPS", id); val result = GrowRules.eligibility(s.p, o)
    Heading(o.text("name")); Note("Sample opportunity · ${o.text("region")}")
    Text(o.text("desc")); Note("${o.text("deadline")} · unverified sample date")
    GrowCard("Eligibility · ${result.status}", "Simulated rules, not an official eligibility decision.") { result.reasons.forEach { Note(it) } }
    o.arr("rules").objects().filter { it.text("t") == "income" }.forEach { rule ->
        GrowCard("Is family income below ${rule.text("v")}?", "Choose a category only. No income documents are collected.") {
            Choices(listOf("unknown", "below", "above"), s.p.text("income", "unknown")) { value -> s.update("Income category updated") { it.put("income", value) } }
        }
    }
    o.arr("rules").objects().filter { it.text("t") == "confirm" }.forEach { rule ->
        val key = "$id:${rule.text("id")}"
        GrowCard(rule.text("q")) {
            Choices(listOf("Not sure", "Yes", "No"), if (!s.p.obj("confirms").has(key)) "Not sure" else if (s.p.obj("confirms").optBoolean(key)) "Yes" else "No") { value ->
                s.update("Eligibility confirmation changed") { if (value == "Not sure") it.obj("confirms").remove(key) else it.obj("confirms").put(key, value == "Yes") }
            }
        }
    }
    Heading("Your application checklist")
    Note("Track preparation here. Enter documents only on the official portal; no application is submitted by CitiGrow.")
    val steps = listOf("Read the latest official notice") + o.arr("docs").strings().map { "Prepare: $it" } + listOf("Ask teacher to check eligibility", "Complete application on official portal")
    steps.forEachIndexed { index, label ->
        val checks = s.p.obj("checklists").optJSONArray(id) ?: JSONArray()
        Toggle(label, checks.optBoolean(index)) { value -> s.update("Application checklist changed") { p -> val values = p.obj("checklists").optJSONArray(id) ?: JSONArray(); values.put(index, value); p.obj("checklists").put(id, values) } }
    }
    Note("Source: ${o.text("source")}\nRules and availability still need official verification.")
    // Only known portal roots are enabled; unverified deep links from the prototype are not promoted.
    val portal = when (o.text("type")) {
        "scholarship" -> "https://scholarships.gov.in/"
        "apprentice" -> "https://www.apprenticeshipindia.gov.in/"
        "scheme", "centre" -> "https://www.skillindiadigital.gov.in/"
        else -> ""
    }
    if (portal.isNotBlank()) Primary("Open official portal") { s.openUrl(portal) } else Note("Official application link pending verification. Ask your teacher for the current notice.")
}
@Composable fun MentorsScreen(s: AppSession) {
    Heading("Learn from people who took this path")
    Note("Fictional mentors · teacher-moderated group sessions only. No private messages or phone numbers.")
    val sharing = s.p.obj("consent").optBoolean("teacher")
    s.store.list("SESSIONS").forEach { session ->
        val booked = session.text("id") in s.p.arr("bookings").strings()
        GrowCard(session.text("title"), "${session.text("host")}\n${session.text("when")}") {
            Secondary(if (booked) "Request saved for teacher" else "Request a group seat", !booked && sharing) {
                s.update("Group seat requested") { it.arr("bookings").addUnique(session.text("id")) }
                s.tell("Request saved locally for your teacher. No booking or message was sent.")
            }
        }
    }
    if (!sharing) Note("Enable sharing with teacher in Profile to request a group session.")
}
