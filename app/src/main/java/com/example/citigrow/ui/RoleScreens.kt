package com.example.citigrow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import com.example.citigrow.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable fun ParentScreen(s: AppSession) {
    var selected by rememberSaveable { mutableStateOf(s.store.profiles.first().text("id")) }
    val child = s.store.profiles.firstOrNull { it.text("id") == selected } ?: s.store.profiles.first()
    Heading("A future we can support together")
    Choices(s.store.profiles.map { it.text("name") }, child.text("name")) { name -> selected = s.store.profiles.first { it.text("name") == name }.text("id") }
    if (!child.obj("consent").optBoolean("parent")) {
        GrowCard("${child.text("name")} keeps progress private", "Your child can enable sharing with family in Profile. Conversations always stay private.")
    } else {
        val career = s.store.find("CAREERS", child.text("career"))
        val transcript = if (career.length() > 0) "${child.text("name")} is exploring ${career.text("title")}. ${career.text("short")} Typical entry: ${career.text("edu")}. This is sample guidance; confirm current requirements with a teacher."
            else "${child.text("name")} is beginning to explore interests and possibilities. Give them time and encouragement."
        GrowCard("Your child's possibilities", transcript) { Secondary("Listen to career explanation") { s.speak("parent_explanation", transcript) } }
        GrowCard("How you can help this week", "Make room for ${child.optInt("study", 30)} quiet minutes, three times this week. Ask what they enjoyed learning.")
        val usage = child.arr("usage"); val total = (0 until usage.length()).sumOf { usage.optInt(it) }
        val goal = child.optInt("study", 30)
        Expand("Usage this week · $total minutes") {
            Note("${(0 until usage.length()).count { usage.optInt(it) > 0 }} active days · ${(0 until usage.length()).count { usage.optInt(it) >= goal }} days met the ${goal}m goal\nLast active: ${child.text("lastActive", "Not yet")}")
            val maximum = maxOf(goal, (0 until usage.length()).maxOfOrNull { usage.optInt(it) } ?: goal).coerceAtLeast(1)
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, day ->
                Text("$day · ${usage.optInt(index)}m · goal ${goal}m")
                LinearProgressIndicator(progress = { usage.optInt(index).toFloat() / maximum }, modifier = Modifier.fillMaxWidth())
            }
            Note("Ram and Lakshman start with sample usage. New lesson time counts completed minutes on this device.")
        }
        Expand("Pathways and expected milestones") {
            Note(if (career.length() > 0) "${career.text("edu")}\n${career.text("dur")}\n${career.text("prog")}" else "Explore interests → Skills Compass → Choose a career → Build skills → Apply with support")
        }
        Expand("Costs") { Note(career.text("fin", "Free foundations are available. Teacher course fees are illustrative.")) }
        Expand("Financial help") { Note("Check current scheme rules with the school. Sample matching does not guarantee eligibility."); Secondary("National Scholarship Portal") { s.openUrl("https://scholarships.gov.in/") } }
        Expand("Nearby learning") { Note("Sample: Government ITI, Villupuram. Confirm admissions and availability."); Secondary("AISHE college directory") { s.openUrl("https://aishe.gov.in/") } }
    }
    Expand("Family learning schedule · shared phone") {
        var cursor = 18 * 60
        val visible = s.store.profiles.filter { it.obj("consent").optBoolean("parent") && it.text("phone") == "shared" }
        visible.forEach { p ->
            val from = cursor; cursor += p.optInt("study", 30)
            fun clock(min: Int) = "%02d:%02d".format(min / 60, min % 60)
            Note("${p.text("name")}: ${clock(from)}–${clock(cursor)} · preferred ${p.text("learnTime", "evening")}")
        }
        if (visible.isEmpty()) Note("No shared schedules are currently shared with family.")
        Note("Suggested evening schedule, Monday–Saturday. Sunday is a family day. Adjust together to suit your household.")
    }
    Secondary("Switch role") { s.go("roles") }
}
@Composable fun TeacherScreen(s: AppSession) {
    var tab by rememberSaveable { mutableStateOf("Overview") }
    var selected by rememberSaveable { mutableStateOf("") }
    Heading("Every student has a possibility")
    Note("Facilitator demo · recommendations and requests stay on this device")
    Choices(listOf("Overview", "Students", "Courses", "Profile"), tab) { tab = it; selected = "" }
    val shared = s.store.profiles.filter { it.obj("consent").optBoolean("teacher") }
    val sample = s.store.list("COHORT").filter { it.optBoolean("consent") }
    when (tab) {
        "Overview" -> {
            GrowCard("Your classroom", "${shared.size} shared device profiles · ${sample.size} fictional cohort students")
            Expand("Help requests and interventions") {
                sample.filter { it.optBoolean("help") }.forEach { c -> Note("${c.text("name")}: ${c.text("helpReason")}\nSuggested support: a short guided practice session.") }
                shared.filter { it.optJSONObject("assessment")?.obj("levels")?.optInt("digital", 3) == 1 }.forEach { Note("${it.text("name")}: offer guided shared-phone digital practice.") }
            }
            Expand("Talent recommendations · simulated rules") {
                (shared.map { it to (it.optJSONObject("assessment")?.obj("levels") ?: JSONObject()) } + sample.map { it to it.obj("levels") }).forEach { (p, levels) ->
                    val strong = levels.keys().asSequence().filter { levels.optInt(it) == 3 }.toList()
                    if (strong.isNotEmpty()) Note("${p.text("name")}: strong in ${strong.joinToString(", ")}. Explore relevant training together.")
                }
            }
            Expand("Upcoming dates · sample only") { s.store.list("OPPS").filter { it.optInt("sort") > 0 }.take(3).forEach { Note("${it.text("name")}: ${it.text("deadline")}") } }
        }
        "Students" -> {
            if (selected.isNotEmpty()) {
                val student = shared.firstOrNull { it.text("id") == selected }
                if (student == null) { Note("This student no longer shares progress."); return }
                GrowCard(student.text("name"), "Class ${student.text("edu")} · ${student.arr("lessonsDone").length()} lessons completed")
                Note("Career: ${s.store.find("CAREERS", student.text("career")).text("title", "Exploring")}")
                Secondary("Start assisted learning session") { s.go("pin/${student.text("id")}") }
                Note("The student unlocks with their own PIN. Conversations are not visible in this dashboard.")
                Expand("Recommend for an opportunity") {
                    s.store.list("OPPS").forEach { o ->
                        Secondary(o.text("name")) {
                            s.store.change("Teacher recommendation saved") { s.store.state.arr("recommendations").put(JSONObject().put("student", selected).put("opportunity", o.text("id")).put("note", "Explore ${o.text("name")} together. Check official eligibility first.")) }
                            s.tell("Recommendation saved on this device for ${student.text("name")}.")
                        }
                    }
                }
                Secondary("Back to students") { selected = "" }
            } else {
                shared.forEach { p -> GrowCard(p.text("name"), "Class ${p.text("edu")} · shared with teacher", { selected = p.text("id") }) }
                Expand("Fictional classroom cohort") { sample.forEach { GrowCard(it.text("name"), "Class ${it.text("edu")} · ${it.optInt("lessons")} sample lessons\n${it.text("helpReason", "Making steady progress")}") } }
                Primary("Create profile for a student without a phone") { s.go("register/assisted") }
            }
        }
        "Courses" -> TeacherCourses(s)
        else -> {
            Heading("Connect & Earn")
            val revenue = s.store.list("TX").sumOf { it.optInt("amount") }
            GrowCard("₹$revenue · simulated earnings", "Includes fictional student payments and CSR-funded seats. No payments or subscriptions are connected.")
            Expand("Offer teacher-moderated group sessions") {
                val offered = s.store.state.optBoolean("offered")
                Secondary(if (offered) "Session offer saved" else "Offer a weekly group session", !offered) { s.store.change("Group session offer saved") { s.store.state.put("offered", true) } }
                Note("Local demo offer only. No public listing or invitation is sent.")
            }
            Expand("Partner organisations · samples") { s.store.list("PSUBS").forEach { Note(it.text("org")) } }
            Expand("Student interest requests") {
                val requests = shared.filter { it.arr("interests").length() > 0 || it.arr("bookings").length() > 0 }
                if (requests.isEmpty()) Note("No shared interest requests yet.")
                requests.forEach { p ->
                    GrowCard(p.text("name"), "${p.arr("interests").length()} partner interests · ${p.arr("bookings").length()} group-seat requests") {
                        Secondary("Prepare teacher follow-up") { s.tell("Discuss the request with ${p.text("name")} and their guardian, then contact the organisation through your school. External forwarding is not connected.") }
                    }
                }
            }
            Secondary("Switch role") { s.go("roles") }
        }
    }
}
@Composable private fun TeacherCourses(s: AppSession) {
    var title by rememberSaveable { mutableStateOf("") }; var description by rememberSaveable { mutableStateOf("") }
    var pricing by rememberSaveable { mutableStateOf("Paid") }; var price by rememberSaveable { mutableStateOf("49") }
    var csr by rememberSaveable { mutableStateOf(true) }
    Expand("My sample modules") { s.store.list("TMODS").forEach { GrowCard(it.text("title"), "${it.text("meta")}\n${it.optInt("enrol")} simulated enrolments") } }
    s.store.state.arr("modules").objects().forEach { GrowCard(it.text("title"), "${it.text("pricing")} · ${it.text("price")} · local demo publication") }
    Heading("Create your own module")
    Entry("Course title", title) { title = it.take(120) }; Entry("What will students learn?", description) { description = it.take(500) }
    Choices(listOf("Paid", "Free"), pricing) { pricing = it }
    if (pricing == "Paid") Entry("Illustrative price in rupees", price, numeric = true) { price = it.filter(Char::isDigit).take(5) }
    Toggle("Allow CSR-sponsored seats", csr) { csr = it }
    Note("Teachers are paid for sponsored seats. One-time versus subscription pricing will be decided after the pilot.")
    Primary("Publish demo module", title.isNotBlank() && description.isNotBlank() && (pricing == "Free" || (price.toIntOrNull() ?: 0) > 0)) {
        s.store.change("Teacher module published locally") { s.store.state.arr("modules").put(JSONObject().put("id", UUID.randomUUID()).put("title", title.trim()).put("desc", description).put("pricing", pricing).put("price", if (pricing == "Free") "0" else price).put("csr", csr)) }
        title = ""; description = ""; s.tell("Module saved locally. Public marketplace publishing and payments are not connected.")
    }
}
@Composable fun PartnerScreen(s: AppSession) {
    var type by rememberSaveable { mutableStateOf("Story") }; var org by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }; var district by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }; var safe by rememberSaveable { mutableStateOf(false) }
    Heading("Open a door for someone")
    Note("Alumni, mentors, NGOs and employers · local demo portal")
    Choices(listOf("Story", "Group mentorship", "Training programme", "Apprenticeship", "Job / internship"), type) { type = it }
    Entry("Organisation / contributor", org) { org = it.take(120) }; Entry("Listing title", title) { title = it.take(120) }
    Entry("District", district) { district = it.take(60) }; Entry("Describe your contribution", description) { description = it.take(1000) }
    Toggle("I agree to teacher moderation, age-appropriate work and no private contact with minors", safe) { safe = it }
    Expand("Verification steps") { Note("1. Organisation review\n2. Teacher safety review\n3. Verify opportunity terms\n4. Publish only after approval\n\nThese are planned steps. No verification service is connected.") }
    Primary("Save submission for review", safe && org.isNotBlank() && title.isNotBlank() && district.isNotBlank() && description.isNotBlank()) {
        s.store.change("Partner submission saved locally") { s.store.state.arr("partnerSubs").put(JSONObject().put("type", type).put("org", org.trim()).put("title", title.trim()).put("district", district).put("desc", description).put("status", "Pending review · local demo")) }
        title = ""; description = ""; safe = false
        s.tell("Saved on this device. Nothing has been submitted to a reviewer or published.")
    }
    Expand("Submissions on this device") {
        if (s.store.state.arr("partnerSubs").length() == 0) Note("No submissions yet.")
        s.store.state.arr("partnerSubs").objects().forEach { GrowCard(it.text("title"), "${it.text("org")} · ${it.text("status")}") }
    }
    Secondary("Switch role") { s.go("roles") }
}
