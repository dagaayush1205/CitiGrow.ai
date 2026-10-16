package com.example.citigrow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.citigrow.data.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable fun ChatScreen(s: AppSession) {
    var text by rememberSaveable { mutableStateOf("") }
    val flow = s.store.data.arr("FLOW").strings(); val step = s.p.optInt("chatStep")
    TextButton(onClick = { s.go("home") }) { Text("Close discovery") }
    Heading("Let's discover your possibilities")
    Note("Simulated AI · your conversation stays private")
    LinearProgressIndicator(progress = { (step.toFloat() / flow.size).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
    if (step >= flow.size) {
        GrowCard("Your everyday experience matters", "Your interest profile is ready. Explore careers and take a short skills check to build your path.")
        Primary("See career matches") { s.go("discover") }
        Secondary("Take Skills Compass") { s.go("assessment") }
        return
    }
    val question = flow[step]
    val key = if (question == "exp") {
        val enjoy = s.p.obj("chatAns").optJSONObject("enjoy")
        if ((enjoy?.obj("tags")?.optInt("practical") ?: 0) >= 2 || (enjoy?.obj("tags")?.optInt("tech") ?: 0) >= 2) "q_exp_fix"
        else if ((enjoy?.obj("tags")?.optInt("people") ?: 0) >= 2) "q_exp_people" else "q_exp"
    } else "q_$question"
    s.p.arr("chat").objects().takeLast(2).forEach { answer -> GrowCard("You shared", answer.text("text")) }
    GrowCard("${step + 1} of ${flow.size}", s.t(key))
    LaunchedEffect(step) { if (s.p.optBoolean("readAloud", true)) s.speak(key, s.t(key)) }
    s.store.data.obj("QA").arr(question).objects().forEach { answer ->
        val labelKey = "a_${question}_${answer.text("id")}" // prototype uses a_* keys
        val label = s.t(labelKey).takeUnless { it == labelKey } ?: s.t("${question}_${answer.text("id")}").takeUnless { it == "${question}_${answer.text("id")}" } ?: answer.text("id")
        Secondary(label) {
            s.update("Discovery answer saved") { GrowRules.answer(s.store, it, question, answer.copy().put("text", label)) }; text = ""
        }
    }
    Entry("Or tell us in your own words", text) { text = it.take(500) }
    Primary("Send", text.isNotBlank()) { s.update("Discovery answer saved") { GrowRules.answer(s.store, it, question, GrowRules.parse(s.store, question, text.trim())) }; text = "" }
    Secondary("Speak · coming soon") { s.tell("Speech recognition is not connected. Choose an answer or type in English, Hindi or Tamil.") }
}
@Composable fun AssessmentScreen(s: AppSession) {
    var started by rememberSaveable { mutableStateOf(false) }
    var answers by rememberSaveable { mutableStateOf(listOf<Int>()) }
    val items = s.store.list("ASSESS")
    val result = s.p.optJSONObject("assessment")
    Heading("Skills Compass")
    Note("ASER-style demo · not an official ASER assessment")
    if (!started && result != null) {
        Heading("Your strengths can grow")
        val skills = s.store.data.arr("SKILLS").strings()
        SkillRadar(skills.map { result.obj("levels").optInt(it, 1) })
        Note("Clockwise from top: ${skills.joinToString(", ")}")
        result.obj("levels").keys().forEach { skill ->
            val level = result.obj("levels").optInt(skill, 1)
            GrowCard(skill.replaceFirstChar { it.uppercase() }, listOf("Basic", "Developing", "Strong")[(level - 1).coerceIn(0, 2)]) {
                LinearProgressIndicator(progress = { level / 3f }, modifier = Modifier.fillMaxWidth())
                val task = s.store.data.obj("TASKS").obj(skill)
                if (level < 3) Secondary(task.text("title")) { s.go(if (task.has("course")) "course/${task.text("course")}" else "quiz/${task.text("quiz")}") }
            }
        }
        Secondary("Try the check again") { started = true; answers = emptyList() }
        Primary("Continue learning") { s.go("learn") }
        return
    }
    if (!started) {
        GrowCard("Find a starting point", "9 short questions about reading, numbers and everyday situations. This helps us choose practice; it is not a pass or fail test.")
        Primary("Begin skills check") { started = true }
        return
    }
    val index = answers.size.coerceAtMost(items.lastIndex); val item = items[index]
    Note("Question ${index + 1} of ${items.size}")
    LinearProgressIndicator(progress = { index.toFloat() / items.size }, modifier = Modifier.fillMaxWidth())
    if (item.text("p").isNotBlank()) GrowCard("Read or listen", s.t(item.text("p")))
    Heading(s.t(item.text("q")))
    Secondary("Read question aloud") { s.speak(item.text("q"), s.t(item.text("p")) + " " + s.t(item.text("q"))) }
    val options = if (item.has("lit")) item.arr("lit").strings() else item.arr("o").strings().map { s.t(it) }
    options.forEachIndexed { choice, label -> Secondary(label) {
        answers = answers + choice
        if (answers.size == items.size) {
            s.update("Skills Compass completed") { p ->
                p.put("assessment", GrowRules.assess(items, answers))
                val map = p.obj("mapProgress").optJSONObject(p.text("career")); if (map?.optInt("stage") == 2) map.put("stage", 3)
            }; started = false
        }
    } }
}
@Composable fun CourseScreen(s: AppSession, id: String) {
    val c = s.store.find("COURSES", id); val saved = id in s.p.arr("downloads").strings()
    val free = c.obj("price").text("mode") == "free"
    Heading(c.text("title")); Note("${c.text("by")} · ${c.text("langs")}")
    Note(if (free) "Free foundations · sample text lessons" else "₹${c.obj("price").optInt("amount")} illustrative price · demo access, no payment collected")
    Note(c.text("desc").replace("Free module", if (free) "Free module" else "Sample module"))
    val bytes = c.toString().toByteArray().size
    Secondary(if (saved) "✓ Saved for offline · ${(bytes / 1024f).coerceAtLeast(0.1f).let { "%.1f".format(it) }} KB text" else "Save text lessons offline · ${maxOf(1, bytes / 1024)} KB", !saved && !s.store.state.optBoolean("offline")) {
        s.update("Course saved offline") { it.arr("downloads").addUnique(id) }
    }
    if (c.optBoolean("csr")) Note("CSR seats may fund teacher fees. Demo only; sponsorship is not connected.")
    if (!free) {
        val enrolled = id in s.p.arr("enrolled").strings()
        Secondary(if (enrolled) "Demo enrolment saved" else "Enrol · demo access", !enrolled) { s.update("Demo enrolment saved") { it.arr("enrolled").addUnique(id) } }
    }
    val unavailable = s.store.state.optBoolean("offline") && !saved
    if (unavailable) Note("Save this course when online to open lessons in offline demo mode.")
    c.arr("lessons").strings().forEachIndexed { index, title ->
        Secondary("${if ("$id|$index" in s.p.arr("lessonsDone").strings()) "✓ " else ""}${index + 1}. $title", !unavailable) { s.go("lesson/$id/$index") }
    }
    if (c.text("quiz").isNotBlank()) Secondary("Practice quiz", !unavailable) { s.go("quiz/${c.text("quiz")}") }
}
@Composable fun LessonScreen(s: AppSession, id: String, index: Int) {
    val c = s.store.find("COURSES", id)
    var picked by rememberSaveable { mutableIntStateOf(-1) }
    val check = c.obj("check"); val done = "$id|$index" in s.p.arr("lessonsDone").strings()
    val openedAt = remember { System.currentTimeMillis() }
    Heading(c.arr("lessons").optString(index)); Note("${c.optInt("min")} minute sample lesson · English content")
    GrowCard("Today's ideas") { c.arr("points").strings().forEach { Text("• $it") } }
    Note("This prototype bundles a short course summary for every lesson; full lesson materials are still to be authored.")
    Secondary("Listen to these ideas") { s.speak("${id}_$index", c.arr("points").strings().joinToString(". ")) }
    GrowCard("Check your understanding", check.text("q")) {
        check.arr("o").strings().forEachIndexed { i, option -> Secondary("${if (picked == i) "● " else ""}$option") { picked = i } }
        if (picked >= 0) Note(if (picked == check.optInt("a")) "That's right. You are ready to continue." else "Try again. Read the ideas above for a hint.")
    }
    Primary(if (done) "Completed · return to course" else "Complete lesson", done || picked == check.optInt("a")) {
        if (!done) s.update("Lesson completed") { p ->
            p.arr("lessonsDone").addUnique("$id|$index")
            val map = p.obj("mapProgress").optJSONObject(p.text("career"))
            if (map != null && map.optInt("stage") in 3..4 && id in s.career().arr("courses").strings()) map.put("lessons", map.optInt("lessons") + 1)
            val usage = p.arr("usage"); while (usage.length() < 7) usage.put(0)
            val day = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
            val elapsed = ((System.currentTimeMillis() - openedAt) / 60_000).toInt()
            usage.put(day, usage.optInt(day) + elapsed)
            p.put("lastActive", "today")
        }
        s.go("course/$id")
    }
}
@Composable fun QuizScreen(s: AppSession, id: String) {
    val quiz = s.store.find("QUIZZES", id)
    val questions = quiz.arr("qs").objects().ifEmpty { quiz.arr("items").objects() }
    var answers by rememberSaveable { mutableStateOf(listOf<Int>()) }
    Heading(quiz.text("title", "Practice quiz"))
    if (questions.isEmpty()) { Note("Quiz content is not available."); return }
    if (answers.size >= questions.size) {
        val score = questions.indices.count { questions[it].optInt("a") == answers[it] }
        GrowCard("Practice complete", "$score / ${questions.size} correct. Every attempt helps you learn.")
        Primary("Back to Learn") { s.go("learn") }; Secondary("Try again") { answers = emptyList() }; return
    }
    val q = questions[answers.size]
    Note("${answers.size + 1} / ${questions.size}"); Heading(q.text("q"))
    q.arr("o").strings().forEachIndexed { i, label -> Secondary(label) {
        answers = answers + i
        if (answers.size == questions.size) s.update("Quiz completed") { it.obj("quizzes").put(id, questions.indices.count { n -> questions[n].optInt("a") == answers[n] }) }
    } }
}
@Composable fun SimulatorScreen(s: AppSession, id: String) {
    val sim = s.store.data.obj("SIMS").obj(id); val steps = sim.arr("steps").objects()
    var step by rememberSaveable { mutableIntStateOf(0) }; var choice by rememberSaveable { mutableIntStateOf(-1) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    Heading(sim.text("title")); Note("Simulated career scenario · educational only")
    if (id == "electric") Note("Never attempt electrical repairs yourself. This scenario is not practical safety training; seek a qualified adult professional.")
    GrowCard("The situation", sim.text("scene"))
    if (step >= steps.size) { GrowCard("Scenario complete", "$score / ${steps.size} strong choices. Explore the linked courses to keep practising."); Primary("Back to Discover") { s.go("discover") }; return }
    val current = steps[step]; val options = current.arr("c").objects()
    Heading(current.text("p"))
    options.forEachIndexed { i, option -> Secondary(option.text("l"), choice == -1) { choice = i } }
    if (choice >= 0) {
        GrowCard("Feedback", options[choice].text("fb"))
        Primary("Continue") {
            if (options[choice].optBoolean("ok")) score++
            step++; choice = -1
            if (step == steps.size) s.update("Career scenario completed") { it.obj("sims").put(id, score) }
        }
    }
}
@Composable fun CompareScreen(s: AppSession) {
    val careers = s.store.list("CAREERS")
    var left by rememberSaveable { mutableStateOf(careers.first().text("id")) }
    var right by rememberSaveable { mutableStateOf(careers[1].text("id")) }
    Heading("Compare pathways")
    Note("Choose two routes. All requirements and costs are illustrative.")
    listOf(left, right).forEachIndexed { index, selected ->
        Expand("${if (index == 0) "First" else "Second"} · ${s.store.find("CAREERS", selected).text("title")}") {
            careers.forEach { c -> Secondary(c.text("title")) { if (index == 0) left = c.text("id") else right = c.text("id") } }
        }
    }
    listOf("edu" to "Education", "dur" to "Duration", "fin" to "Cost", "loc" to "Location").forEach { (key, label) ->
        GrowCard(label) { listOf(left, right).forEach { id -> val c = s.store.find("CAREERS", id); Text(c.text("title")); Note(c.text(key)) } }
    }
}
@Composable fun FutureScreen(s: AppSession) {
    Heading("My Future"); Note("Your progress belongs to you. Small steps count.")
    GrowCard("Achievements", "${s.p.arr("lessonsDone").length()} lessons completed\n${s.p.obj("quizzes").length()} quizzes practised\n${if (s.p.optJSONObject("assessment") != null) "Skills Compass completed" else "Skills Compass ready when you are"}")
    if (s.career().length() > 0) GrowCard("Your dream", s.career().text("title"), { s.go("lifemap") })
    Expand("Stories of possibility · fictional") { s.store.list("STORIES").forEach { GrowCard(it.text("title"), it.text("text")) } }
    GrowCard("A family can make room for a dream", "Ask for a quiet ${s.p.optInt("study", 30)} minutes with the shared phone. Your family view keeps conversations private.")
}
@Composable fun HelperScreen(s: AppSession) {
    Heading("Ask CitiGrow"); Note(s.t("helperSoon")); Text(s.t("helperPlaceholder"))
    s.store.data.arr("HELPER_Q").let { qs -> (0 until qs.length()).forEach { i -> GrowCard(qs.getJSONArray(i).optString(0), "Planned sources: ${qs.getJSONArray(i).getJSONArray(1).strings().joinToString(", ")}") } }
    Note("No AI service is connected. Your question will not be sent anywhere.")
}
@Composable fun PrivacyScreen() {
    Heading("Your learning, your choice")
    GrowCard("On this device", "Profiles, conversations, progress and demo requests stay in app-private local storage. No analytics, ad network or server is connected. Device backup is disabled.")
    GrowCard("Sharing", "Students choose whether their progress appears in teacher and parent views. Conversations never appear in those views. Shared-phone PINs are a demo safeguard; role PINs are not production authentication.")
    GrowCard("Safe connections", "Group mentorship is moderated by a teacher. There is no private adult–student chat and no contact details are collected. Application documents stay on official portals.")
    GrowCard("Your data", "Profile → Reset demo removes local student records and restores sample personas. Official sites open in your browser and have their own privacy policies.")
}
