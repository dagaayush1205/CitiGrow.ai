package com.example.citigrow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import com.example.citigrow.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable fun HomeScreen(s: AppSession) {
    val c = s.career()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        Text("${s.t(if (hour < 12) "morning" else if (hour < 17) "afternoon" else "evening")},", fontSize = 16.sp, color = Muted)
        Text(s.p.text("name"), style = MaterialTheme.typography.displayMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Badge(if (c.length() > 0) c.text("title") else "Exploring my future", icon = "target")
            Badge("Class ${s.p.text("edu")} · ${s.p.optInt("study", 30)} min/day", GreyTint, Color(0xFF334E68))
        }
    }
    val currentStage = if (c.length() > 0) s.p.obj("mapProgress").obj(c.text("id")).optInt("stage") else 0
    val pendingLesson = if (currentStage in 3..4) c.arr("courses").strings().asSequence().map { s.store.find("COURSES", it) }
        .filter { !s.store.state.optBoolean("offline") || it.text("id") in s.p.arr("downloads").strings() }
        .flatMap { course -> course.arr("lessons").strings().mapIndexed { i, title -> Triple(course.text("id"), i, title) } }
        .firstOrNull { (course, index, _) -> "$course|$index" !in s.p.arr("lessonsDone").strings() } else null
    val next = when {
        s.p.optJSONObject("discovery") == null -> s.t("na_talk_t") to "chat"
        s.p.optJSONObject("assessment") == null -> s.t("na_assess_t") to "assessment"
        c.length() == 0 -> "Explore your career matches" to "discover"
        pendingLesson != null -> s.t("na_lesson_t").replace("{l}", pendingLesson.third) to "lesson/${pendingLesson.first}/${pendingLesson.second}"
        else -> "Continue your Life Map" to "lifemap"
    }
    Surface(color = Blue, contentColor = Color.White, shape = RoundedCornerShape(22.dp), shadowElevation = 6.dp) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(s.t("nextStep"), fontWeight = FontWeight.Bold, fontSize = 14.sp); Badge("15 min", Color(0xFF0F5A99), Color.White, "clock") }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconTile(if (next.second == "chat") "mic" else if (next.second == "assessment") "target" else if (pendingLesson != null) "book" else "map", Color.White, Blue, 52.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(next.first, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold)
                    Text(if (c.length() > 0) "Build the foundations for ${c.text("title")} at your own pace." else "Your everyday interests are a starting point for your future.", color = Color(0xFFDCEBFA), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { GrowIcon("sparkles", Modifier.size(14.dp), Color(0xFFDCEBFA)); Text("Simulated AI · rule-based suggestion", style = MaterialTheme.typography.bodySmall, color = Color(0xFFDCEBFA)) }
            Button(onClick = { s.go(next.second) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Blue)) {
                Text(if (next.second == "lifemap") s.t("openLifeMap") else if (pendingLesson != null) s.t("na_les_cta") else s.t("na_start"), style = MaterialTheme.typography.labelLarge); Spacer(Modifier.width(8.dp)); GrowIcon("chevR")
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Triple("Life Map", "map", BlueTint), Triple("My Future", "heart", Gold), Triple("Mentors", "users", Mint)).forEachIndexed { index, (title, icon, tint) ->
            Surface(Modifier.weight(1f).clickable { s.go(when (index) { 0 -> if (c.length() > 0) "lifemap" else "discover"; 1 -> "future"; else -> "mentors" }) }, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border), color = Color.White, shadowElevation = 2.dp) {
                Column(Modifier.padding(horizontal = 6.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { IconTile(icon, tint, if (index == 1) GoldInk else if (index == 2) TealInk else Blue); Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
            }
        }
    }
    val deadline = s.store.list("OPPS").filter { it.optInt("sort") > 0 && GrowRules.eligibility(s.p, it).status != "Does not meet a requirement" && (c.length() == 0 || s.p.text("career") in it.arr("careers").strings() || "all" in it.arr("careers").strings()) }.minByOrNull { it.optInt("sort") }
    if (deadline != null) GrowPanel(action = { s.go("opp/${deadline.text("id")}") }) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile("calendar", RedTint, RedInk)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Note(s.t("upcomingDeadline")); Text(deadline.text("name"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge); Note(deadline.text("deadline")); Badge("Sample data · unverified date", Gold, GoldInk) }
        }
    }
    val recs = s.store.state.arr("recommendations").objects().filter { it.text("student") == s.p.text("id") }
    if (s.p.obj("consent").optBoolean("teacher") && recs.isNotEmpty()) GrowCard("From your teacher", recs.last().text("note"))
    GrowPanel {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { IconTile("download"); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { SectionTitle(s.t("offlineContent")); Note("${s.p.arr("downloads").length()} courses saved for offline learning"); Note("${s.store.state.arr("queue").length()} changes saved locally · sync not connected") } }
        Secondary(s.t("manageDownloads")) { s.go("learn") }
    }
}
@Composable fun DiscoverScreen(s: AppSession) {
    var segment by rememberSaveable { mutableStateOf("Career paths") }
    var filter by rememberSaveable { mutableStateOf("All") }
    Heading(s.t("discoverTitle"))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Career paths", "Opportunities", "Connect").forEachIndexed { index, label ->
            val selected = segment == label
            Surface(onClick = { segment = label; filter = "All" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = if (selected) Blue else Color.White, contentColor = if (selected) Color.White else Navy, border = androidx.compose.foundation.BorderStroke(2.dp, if (selected) Blue else InputBorder)) {
                Column(Modifier.padding(horizontal = 4.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) { GrowIcon(listOf("compass", "briefcase", "users")[index]); Text(s.t(label), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
            }
        }
    }
    when (segment) {
        "Career paths" -> {
            Surface(onClick = { s.go("chat") }, shape = RoundedCornerShape(20.dp), color = Navy, contentColor = Color.White) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile("mic", Teal, Navy, 52.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(s.t("talkTitle"), fontWeight = FontWeight.Bold, fontSize = 17.sp); Note("Start with what you enjoy · simulated AI") }; GrowIcon("chevR", tint = Color(0xFF9FB3C8))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { SectionTitle(s.t("storiesTitle")); Badge(s.t("fictional"), Gold, GoldInk) }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                s.store.list("ALUMNI").forEach { a ->
                    GrowPanel(Modifier.width(272.dp), padding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Avatar(a.text("name"), GoldFill, size = 42.dp); Column(Modifier.weight(1f)) { Text(a.text("name"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge); Note(a.text("place")) } }
                        Badge(s.store.find("CAREERS", a.text("career")).text("title"))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) { a.arr("path").strings().forEach { Badge(it, GreyTint, Navy) } }
                        Text("“${a.text("quote")}”", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        Text(a.text("now"), style = MaterialTheme.typography.labelMedium, color = TealInk)
                        Secondary(s.t("seeCareer")) { s.go("career/${a.text("career")}") }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle(s.t("careersForYou")); TextButton(onClick = { s.go("compare") }) { Text(s.t("compare")) } }
            Choices(listOf("All", "Vocational", "Academic"), filter) { filter = it }
            val careers = s.store.list("CAREERS").filter { filter == "All" || it.text("route") == if (filter == "Vocational") "voc" else "aca" }
                .sortedByDescending { GrowRules.match(s.p, it)?.percent ?: 0 }
            careers.forEach { c ->
                val match = GrowRules.match(s.p, c)
                GrowPanel(padding = 14.dp, action = { s.go("career/${c.text("id")}") }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconTile(c.text("icon"), if (c.text("route") == "voc") Mint else BlueTint, if (c.text("route") == "voc") TealInk else Blue)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) { Text(c.text("title"), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall); if (match != null) Badge("${match.percent}%", Mint, TealInk) }
                            Note(c.text("short"))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Badge(if (c.text("route") == "voc") "Vocational" else "Academic", GreyTint, Navy); if (c.text("id") == s.p.text("career")) Badge(s.t("yourChoice")) }
                        }
                    }
                }
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
    Hero(c.text("title"), c.text("desc"), c.text("icon")) { Badge(if (c.text("route") == "voc") "Vocational pathway · sample" else "Academic pathway · sample", BlueTint, BlueInk) }
    if (match != null) GrowPanel(background = PurpleTint, border = Color(0xFFD4D0F3)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("Why this matches you", modifier = Modifier.weight(1f), color = PurpleInk, fontWeight = FontWeight.Bold); Badge("Simulated AI", PurpleTint, PurpleInk, "sparkles") }
        Text(match.why, style = MaterialTheme.typography.bodyLarge)
    }
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
    var selected by rememberSaveable(id) { mutableIntStateOf(stage.coerceAtMost(6)) }
    LaunchedEffect(stage) { selected = stage.coerceAtMost(6) }
    Note(s.t("lifeMapFor")); Heading(c.text("title"))
    GrowPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle(s.t("dailyTime")); Badge(s.t("adaptive"), PurpleTint, PurpleInk) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15,30,45,60).forEach { value ->
                ChoiceChip("$value min", value == minutes, Modifier.weight(1f), horizontalPadding = 4.dp) {
                    s.update("Study plan adjusted") { it.put("study", value) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf((minutes*6/15).toString() to "lessons / week", GrowRules.weeks(c.optInt("prepHours"), minutes).toString() to "prep weeks", (minutes*6).toString() to "min / week").forEach { (value,label) ->
                Column(Modifier.weight(1f).background(Cloud, RoundedCornerShape(14.dp)).padding(10.dp)) { Text(value, style = MaterialTheme.typography.headlineSmall); Note(label) }
            }
        }
        WeekBars(List(7) { if (it == 6) 0 else minutes })
        Note("Minutes per day. This estimates preparation effort, not the duration of a qualification.")
    }
    val titles = listOf("Explore the career", "Check eligibility", "Assess foundational skills", "Complete prerequisite preparation", "Follow a study and training plan", "Track official application dates", "Prepare for selection or your first job")
    GrowPanel(padding = 12.dp) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SectionTitle(s.t("yourRoadmap")); Note(s.t("tapStep")) }; RoadMap(stage, selected) { selected = it } }
    if (selected < 7) {
        val index = selected
        GrowPanel(border = if (index == stage) Blue else Border, borderWidth = if (index == stage) 2.dp else 1.dp) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Badge(when { index < stage -> "Completed"; index > stage -> "Coming next"; else -> "Current step" }, if (index < stage) Mint else BlueTint, if (index < stage) TealInk else BlueInk); Note("Step ${index+1} of 7") }
            Text(titles[index], style = MaterialTheme.typography.headlineSmall)
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
    else GrowCard(c.obj("alt").text("title"), c.obj("alt").text("text"))
    if (stage >= 7) GrowCard("Your preparation steps are complete", "Keep learning and check official application windows with your teacher.")
    Expand(c.obj("alt").text("title", "Alternate route")) { Note(c.obj("alt").text("text")) }
}
@Composable fun LearnScreen(s: AppSession) {
    var filter by rememberSaveable { mutableStateOf("All") }
    Heading(s.t("learnTitle"))
    val complete = s.p.optJSONObject("assessment") != null
    GrowPanel(background = GoldSurface, border = Color(0xFFF3D27A), borderWidth = 2.dp, action = { s.go("assessment") }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconTile("target", GoldFill, Navy, 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(s.t("compassTitle"), fontSize = 17.sp, fontWeight = FontWeight.Bold); Note(if (complete) s.t("compassAllDone") else s.t("compassTodo")) }
            GrowIcon("chevR", tint = Muted)
        }
        listOf("aserBasic" to "book", "aserBeyond" to "target").forEach { (label, icon) ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    GrowIcon(icon, Modifier.size(16.dp), if (complete) TealInk else GoldInk)
                    Text(s.t(label), modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Badge(if (complete) s.t("stCompleted") else s.t("stNotStarted"), if (complete) Mint else GreyTint, if (complete) TealInk else Muted)
                }
            }
        }
    }
    if (s.career().length() > 0) Expand("Exams for your career · illustrative") {
        Note("Exam names and timings from the prototype have not been verified. Check the current official notice.")
        s.store.data.obj("EXAMS").optJSONArray(s.p.text("career"))?.let { exams ->
            (0 until exams.length()).forEach { i -> val e = exams.getJSONArray(i); GrowCard(e.optString(0), e.optString(1)) }
        }
    }
    SectionTitle(s.t("allCourses")); Choices(listOf("All", "Free", "My path", "Saved"), filter) { filter = it }
    s.store.list("COURSES").filter { c -> when (filter) { "Free" -> c.obj("price").text("mode") == "free"; "My path" -> s.p.text("career") in c.arr("careers").strings() || "all" in c.arr("careers").strings(); "Saved" -> c.text("id") in s.p.arr("downloads").strings(); else -> true } }.forEach { c ->
        val free = c.obj("price").text("mode") == "free"
        val price = if (free) "Free" else "₹${c.obj("price").optInt("amount")} · sample"
        val count = s.p.arr("lessonsDone").strings().count { it.startsWith(c.text("id") + "|") }
        GrowPanel(action = { s.go("course/${c.text("id")}") }) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTile(c.text("icon"), if (free) Mint else BlueTint, if (free) TealInk else Blue)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(c.text("title"), style = MaterialTheme.typography.titleSmall); Note(c.text("by"))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(price, if (free) Mint else Gold, if (free) TealInk else GoldInk)
                        if (c.optBoolean("csr")) Badge("CSR seat", Mint, TealInk)
                        if (c.text("id") in s.p.arr("downloads").strings()) Badge("Saved", icon = "download")
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(progress = { count.toFloat() / c.arr("lessons").length().coerceAtLeast(1) }, modifier = Modifier.weight(1f).height(8.dp), color = Teal, trackColor = Border)
                Note("$count / ${c.arr("lessons").length()} lessons")
            }
        }
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
    SectionTitle(s.t("govPlatforms"))
    listOf("DIKSHA" to "https://diksha.gov.in/", "SWAYAM" to "https://swayam.gov.in/", "Skill India Digital Hub" to "https://www.skillindiadigital.gov.in/").forEach { (name, url) -> ActionRow(name, "Official learning platform · opens in browser", "landmark", Mint, TealInk) { s.openUrl(url) } }
    Note("Official websites need internet. Individual certification fees may apply.")
}
@Composable fun ProfileScreen(s: AppSession) {
    var reset by remember { mutableStateOf(false) }
    GrowPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Avatar(s.p.text("name"), size = 64.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(s.p.text("name"), style = MaterialTheme.typography.headlineSmall); Note("${s.p.text("studentId")} · Class ${s.p.text("edu")}"); Note("${s.p.text("district")}, ${s.p.text("state")}") }
        }
        Badge(if (s.p.text("phone") == "shared") "Shared family phone" else "Private learning profile", icon = "phone")
    }
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
    GrowPanel(background = Color(0xFFFBFCFE), padding = 14.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile("megaphone", GreyTint, Navy)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Badge("Ad", GreyTint, Navy); Box(Modifier.weight(1f)) { Note(ad.text("sponsor")) } }
                Text(ad.text("title"), style = MaterialTheme.typography.titleSmall)
                Note("Education-related sample ad · never uses personal data")
            }
        }
    }
}
