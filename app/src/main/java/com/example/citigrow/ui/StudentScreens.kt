package com.example.citigrow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import org.json.JSONArray
import org.json.JSONObject

@Composable fun HomeScreen(s: AppSession) {
    Heading("Hello, ${s.p.text("name")} ☀")
    Note("A little learning today brings your future closer.")
    val c = s.career()
    if (c.length() > 0) GrowCard("Your dream · ${c.text("title")}", "${s.p.optInt("study", 30)} minutes a day · at your own pace", { s.go("lifemap") })
    val next = when { s.p.optJSONObject("discovery") == null -> "Talk about what you enjoy" to "chat"; s.p.optJSONObject("assessment") == null -> "Find your strengths with Skills Compass" to "assessment"; c.length() == 0 -> "Explore your career matches" to "discover"; else -> "Continue your Life Map" to "lifemap" }
    GrowCard("Your next step", next.first) { Primary("Let's go") { s.go(next.second) } }
    Choices(listOf("Life Map", "My Future", "Mentors"), "") { s.go(when (it) { "Life Map" -> if (c.length() > 0) "lifemap" else "discover"; "My Future" -> "future"; else -> "mentors" }) }
    val recs = s.store.state.arr("recommendations").objects().filter { it.text("student") == s.p.text("id") }
    if (s.p.obj("consent").optBoolean("teacher") && recs.isNotEmpty()) GrowCard("From your teacher", recs.last().text("note"))
    Note(if (s.store.state.optBoolean("offline")) "Offline demo · saved lessons are ready. Listings are cached samples." else "Saved on this phone · ${s.store.state.arr("queue").length()} local changes awaiting a future sync service.")
}
@Composable fun DiscoverScreen(s: AppSession) {
    var segment by rememberSaveable { mutableStateOf("Career paths") }
    var filter by rememberSaveable { mutableStateOf("All") }
    Heading("Discover possibilities")
    Choices(listOf("Career paths", "Opportunities", "Connect"), segment) { segment = it; filter = "All" }
    when (segment) {
        "Career paths" -> {
            GrowCard("Talk to CitiGrow", "Start with things you do every day.", { s.go("chat") })
            Note("Simulated AI · rule-based matches · sample career guidance")
            Expand(s.t("storiesTitle")) { s.store.list("ALUMNI").forEach { a -> GrowCard(a.text("name"), "${a.text("quote")}\nFictional alumni story", { s.go("career/${a.text("career")}") }) } }
            Choices(listOf("All", "Vocational", "Academic"), filter) { filter = it }
            Secondary("Compare pathways") { s.go("compare") }
            val careers = s.store.list("CAREERS").filter { filter == "All" || it.text("route") == if (filter == "Vocational") "voc" else "aca" }
                .sortedByDescending { GrowRules.match(s.p, it)?.percent ?: 0 }
            careers.forEach { c ->
                val match = GrowRules.match(s.p, c)
                GrowCard(c.text("title"), "${match?.let { "${it.percent}% match · " } ?: "Explore · "}${c.text("short")}", { s.go("career/${c.text("id")}") })
            }
            EducationAd(s, "discover")
            Secondary("AISHE · find colleges") { s.openUrl("https://aishe.gov.in/") }
        }
        "Opportunities" -> {
            Note("Sample listings and simplified eligibility. Dates and rules are unverified. Check current official notices with your teacher.")
            if (s.store.state.optBoolean("offline")) Note("Offline · cached sample listings, not live availability.")
            val types = listOf("All", "scholarship", "scheme", "exam", "apprentice", "internship", "job", "centre")
            Choices(types, filter) { filter = it }
            val portal = when (filter) { "scholarship" -> "NSP" to "https://scholarships.gov.in/"; "apprentice" -> "NAPS" to "https://www.apprenticeshipindia.gov.in/"; else -> "Skill India" to "https://www.skillindiadigital.gov.in/" }
            Secondary("Official portal · ${portal.first}") { s.openUrl(portal.second) }
            s.store.list("OPPS").filter { filter == "All" || it.text("type") == filter }.forEach { o ->
                GrowCard(o.text("name"), "${GrowRules.eligibility(s.p, o).status}\n${o.text("deadline")}", { s.go("opp/${o.text("id")}") })
            }
        }
        else -> {
            GrowCard("Mentors and group sessions", "Teacher-moderated group sessions only.", { s.go("mentors") })
            Note("Partner listings · fictional demo organisations. No real verification or submission.")
            s.store.list("PSUBS").forEachIndexed { index, listing ->
                val id = "partner:$index"
                val interested = id in s.p.arr("interests").strings()
                GrowCard(listing.text("title"), "${listing.text("org")}\n${listing.text("desc")}") {
                    Secondary(if (interested) "Interest saved for teacher" else "Express interest", !interested && s.p.obj("consent").optBoolean("teacher")) {
                        s.update("Partner interest saved") { it.arr("interests").addUnique(id) }
                        s.tell("Saved for your teacher on this device. No contact details were shared; no message was sent.")
                    }
                    if (!s.p.obj("consent").optBoolean("teacher")) Note("Enable sharing with teacher in Profile to express interest.")
                }
            }
            Secondary("Scheme finder") { segment = "Opportunities"; filter = "scheme" }
        }
    }
}
@Composable fun CareerScreen(s: AppSession, id: String) {
    val c = s.store.find("CAREERS", id); val match = GrowRules.match(s.p, c)
    Heading(c.text("title")); Note("${if (c.text("route") == "voc") "Vocational" else "Academic"} pathway · sample guidance")
    GrowCard("A day in this career", c.text("desc"))
    if (match != null) GrowCard("${match.percent}% match · Simulated AI", match.why)
    Expand("Education, time and cost") { Note("${c.text("edu")}\n\n${c.text("dur")}\n\n${c.text("fin")}\n\n${c.text("loc")}") }
    Expand("Where this path can lead") { Note(c.text("prog")); Note(c.arr("skills").strings().joinToString(" · ")) }
    Note("Source: supplied hackathon prototype. Requirements are illustrative and need official verification.")
    if (c.text("sim").isNotEmpty()) Secondary("Try this career · scenario") { s.go("sim/${c.text("sim")}") }
    Primary(if (s.p.text("career") == id) "Open my Life Map" else "Choose this career") {
        s.update("Career chosen") { p -> p.put("career", id); if (!p.obj("mapProgress").has(id)) p.obj("mapProgress").put(id, JSONObject().put("stage", 1).put("lessons", 0)) }
        s.go("lifemap")
    }
}
@Composable fun LifeMapScreen(s: AppSession) {
    val c = s.career()
    if (c.length() == 0) { Heading("Choose your path first"); Primary("Explore careers") { s.go("discover") }; return }
    val id = c.text("id"); val map = s.p.obj("mapProgress").obj(id)
    val stage = map.optInt("stage"); val minutes = s.p.optInt("study", 30)
    Heading("Your Life Map"); Note(c.text("title"))
    GrowCard("A plan that fits your day", "${GrowRules.weeks(c.optInt("prepHours"), minutes)} weeks of illustrative preparation · 6 days a week") {
        Choices(listOf("15", "30", "45", "60"), minutes.toString()) { value -> s.update("Study plan adjusted") { it.put("study", value.toInt()) } }
        Note("Minutes per day. This estimates preparation effort, not the duration of a qualification.")
    }
    val titles = listOf("Explore the career", "Check eligibility", "Assess foundational skills", "Complete prerequisite preparation", "Follow a study and training plan", "Track official application dates", "Prepare for selection or your first job")
    titles.forEachIndexed { index, title ->
        GrowCard("${if (index < stage) "✓" else if (index == stage) "●" else "○"} ${index + 1}. $title", when { index < stage -> "Completed"; index > stage -> "Coming next"; else -> "Your current step" }) {
            if (index == stage) {
                when (index) {
                    0 -> Secondary("Explore") { s.go("career/$id") }
                    1 -> Note("Typical entry: ${c.text("edu")}. Confirm current requirements with your teacher.")
                    2 -> Secondary("Open Skills Compass") { s.go("assessment") }
                    3, 4 -> c.arr("courses").strings().take(if (index == 3) 2 else 3).forEach { course -> Secondary(s.store.find("COURSES", course).text("title")) { s.go("course/$course") } }
                    5 -> c.arr("opps").strings().forEach { opp -> Secondary(s.store.find("OPPS", opp).text("name")) { s.go("opp/$opp") } }
                    6 -> { Note(c.text("prog")); Secondary("Find group support") { s.go("mentors") } }
                }
                val required = if (index == 3) 2 else if (index == 4) 3 else 0
                val done = map.optInt("lessons")
                val ready = when (index) { 2 -> s.p.optJSONObject("assessment") != null; 3, 4 -> done >= required; else -> true }
                if (required > 0) Note("$done of $required pathway lessons completed")
                Primary("Complete this step", ready) { s.update("Life Map step completed") { map.put("stage", minOf(7, index + if (index == 1 && it.optJSONObject("assessment") != null) 2 else 1)).put("lessons", 0) } }
            }
        }
    }
    if (stage >= 7) GrowCard("Your preparation steps are complete", "Keep learning and check official application windows with your teacher.")
    Expand(c.obj("alt").text("title", "Alternate route")) { Note(c.obj("alt").text("text")) }
}
@Composable fun LearnScreen(s: AppSession) {
    var filter by rememberSaveable { mutableStateOf("All") }
    Heading("Build skills, one step at a time")
    val complete = s.p.optJSONObject("assessment") != null
    GrowCard("Skills Compass", if (complete) "Completed · tap to see results" else "Two short ASER-style checks · 9 questions", { s.go("assessment") }) {
        Note("Reading and maths basics · ${if (complete) "Completed" else "Not started"}\nBeyond Basics · ${if (complete) "Completed" else "Not started"}")
    }
    if (s.career().length() > 0) Expand("Exams for your career · illustrative") {
        Note("Exam names and timings from the prototype have not been verified. Check the current official notice.")
        s.store.data.obj("EXAMS").optJSONArray(s.p.text("career"))?.let { exams ->
            (0 until exams.length()).forEach { i -> val e = exams.getJSONArray(i); GrowCard(e.optString(0), e.optString(1)) }
        }
    }
    Heading("All courses"); Choices(listOf("All", "Free", "My path", "Saved"), filter) { filter = it }
    s.store.list("COURSES").filter { c -> when (filter) { "Free" -> c.obj("price").text("mode") == "free"; "My path" -> s.p.text("career") in c.arr("careers").strings() || "all" in c.arr("careers").strings(); "Saved" -> c.text("id") in s.p.arr("downloads").strings(); else -> true } }.forEach { c ->
        val price = if (c.obj("price").text("mode") == "free") "Free" else "₹${c.obj("price").optInt("amount")} · illustrative price"
        GrowCard(c.text("title"), "$price · ${c.optInt("min")} min / lesson\n${c.text("by")}", { s.go("course/${c.text("id")}") })
    }
    if (filter == "All") s.store.state.arr("modules").objects().forEach { module ->
        Expand("Teacher module · ${module.text("title")}") {
            Note(module.text("desc")); Note("${module.text("pricing")} · ₹${module.text("price")} illustrative · local demo module")
            Note("Full lesson materials and payments are not connected.")
            val enrolled = module.text("id") in s.p.arr("enrolled").strings()
            Secondary(if (enrolled) "Demo seat saved" else "Save interest in a demo seat", !enrolled) { s.update("Teacher module interest saved") { it.arr("enrolled").addUnique(module.text("id")) } }
        }
    }
    EducationAd(s, "learn")
    Expand(s.t("govPlatforms")) {
        listOf("DIKSHA" to "https://diksha.gov.in/", "SWAYAM" to "https://swayam.gov.in/", "Skill India Digital Hub" to "https://www.skillindiadigital.gov.in/").forEach { (name, url) -> Secondary(name) { s.openUrl(url) } }
        Note("Opens an official external website. Internet required; individual certification fees may apply.")
    }
}
@Composable fun ProfileScreen(s: AppSession) {
    var reset by remember { mutableStateOf(false) }
    Heading(s.p.text("name")); Note("${s.p.text("studentId")} · Class ${s.p.text("edu")}\n${s.p.text("district")}, ${s.p.text("state")}")
    Expand("Your interest profile") {
        val traits = s.p.optJSONObject("discovery")?.obj("traits")
        if (traits == null) Note("Talk to CitiGrow to discover what you enjoy.") else traits.keys().forEach { Note("$it: ${traits.optInt(it)} / 3") }
        Secondary("Continue discovery") { s.go("chat") }
    }
    GrowCard("You control what you share") {
        Toggle("Share progress with teacher", s.p.obj("consent").optBoolean("teacher")) { value -> s.update("Teacher sharing changed") { it.obj("consent").put("teacher", value) } }
        Toggle("Share progress with family", s.p.obj("consent").optBoolean("parent")) { value -> s.update("Family sharing changed") { it.obj("consent").put("parent", value) } }
        Note("Conversations stay private. Teacher and family dashboards respect these switches.")
    }
    GrowCard(s.t("voiceTitle")) {
        Toggle("Read each screen aloud", s.p.optBoolean("readAloud", true)) { value -> s.update("Read aloud changed") { it.put("readAloud", value) } }
        Note("Audio placeholder. Pre-recorded clips are supported; an on-device Hindi/Tamil TTS engine has not been selected.")
        Secondary("Listen · test voice") { s.speak("tagline", s.t("tagline")) }
    }
    GrowCard("App language") { Choices(listOf("en", "hi", "ta"), s.store.state.text("lang")) { value -> s.store.change { s.store.state.put("lang", value); s.p.put("lang", value) } }; Note("Career and lesson content is currently English. Some interface text falls back to English.") }
    Toggle("Data saver · text only", s.store.state.optBoolean("lite")) { value -> s.store.change { s.store.state.put("lite", value) } }
    Note("All sample content is bundled. There are no automatic media downloads.")
    Expand("Offline storage and sync") { Note("${s.store.state.arr("queue").length()} changes saved locally. Server sync is not connected; coming online does not upload or erase this queue.") }
    Secondary("Privacy") { s.go("privacy") }; Secondary("Switch student") { s.go("profiles") }; Secondary("Switch role") { s.go("roles") }
    TextButton(onClick = { reset = true }) { Text("Reset demo") }
    if (reset) AlertDialog(onDismissRequest = { reset = false }, title = { Text("Reset this demo?") }, text = { Text("This deletes profiles and progress created on this device and restores Ram and Lakshman. This cannot be undone.") },
        confirmButton = { TextButton(onClick = { reset = false; s.go("reset") }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { reset = false }) { Text("Cancel") } })
}
@Composable fun EducationAd(s: AppSession, placement: String) {
    val ad = s.store.data.obj("ADS").obj(placement)
    Expand("Education ad · sample") { Note(ad.text("title")); Note("${ad.text("sponsor")} · No personal data used. No ad network connected.") }
}
