package com.example.citigrow.ui
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import com.example.citigrow.ui.theme.*
import com.example.citigrow.voice.VoiceController
import org.json.JSONObject

class AppSession(val store: GrowStore, val navigate: (String) -> Unit, val tell: (String) -> Unit,
    val speak: (String, String) -> Unit, val openUrl: (String) -> Unit, val studentId: String) {
    val p get() = store.profiles.firstOrNull { it.text("id") == studentId } ?: store.profiles.first()
    fun t(key: String) = store.t(key)
    fun go(route: String) = navigate(route)
    fun update(event: String, edit: (JSONObject) -> Unit) = store.change(event) { edit(p) }
    fun career() = store.find("CAREERS", p.text("career"))
}
@Composable fun CitiGrowApp() {
    val context = LocalContext.current
    val store = remember { GrowStore(context) }
    val revision = store.revision
    var route by rememberSaveable { mutableStateOf("welcome") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }
    var message by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf("") }
    val voice = remember { VoiceController(context) }
    val savedScreens = rememberSaveableStateHolder()
    val savedKeys = remember { mutableSetOf<String>() }
    fun navigate(next: String) {
        if (next == "reset") {
            savedKeys.forEach { savedScreens.removeState(it) }; savedKeys.clear()
            voice.stop(); store.reset(); studentId = ""; history = emptyList(); route = "welcome"
            return
        }
        if (next in listOf("roles", "profiles", "welcome")) studentId = ""
        history = if (next in listOf("roles", "profiles", "home", "teacher", "parent", "partner")) emptyList() else history + route
        route = next
    }
    val session = AppSession(store, ::navigate, { message = it }, { key, text ->
        voiceStatus = if (store.state.optBoolean("offline")) "Voice is unavailable in offline demo mode."
        else if (voice.read(key, text, store.state.text("lang"))) "Playing recorded audio" else "Audio coming soon · $text"
    }, { url ->
        if (store.state.optBoolean("offline")) message = "Official portals need internet. Your checklist is saved on this device."
        else if (url.startsWith("https://")) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { message = "No browser is available on this device." }
    }, studentId)
    DisposableEffect(Unit) { onDispose { voice.stop() } }
    val p = session.p
    val titleKey = when (route.substringBefore('/')) {
        "welcome" -> "chooseLang"; "roles" -> "chooseStart"; "profiles" -> "Who is learning today?"
        "home" -> "Home"; "discover" -> "segCareers"; "learn" -> "Learn"; "profile" -> "Profile"
        "chat" -> "q_" + store.data.arr("FLOW").optString(p.optInt("chatStep"), "enjoy")
        "assessment" -> "aserTitle"; "lifemap" -> "Life Map"; "parent" -> "Parent view"; "teacher" -> "Teacher view"
        else -> route.substringBefore('/').replaceFirstChar { it.uppercase() }
    }
    LaunchedEffect(route, studentId, store.state.text("lang"), p.optBoolean("readAloud", true)) {
        voice.stop(); voiceStatus = ""
        if (p.optBoolean("readAloud", true)) session.speak(titleKey, session.t(titleKey))
    }
    BackHandler(route != "welcome") {
        voice.stop()
        if (history.isNotEmpty()) { route = history.last(); history = history.dropLast(1) }
        else navigate(if (route == "roles") "welcome" else if (studentId.isNotBlank() && route != "home") "home" else "roles")
    }
    val tabs = listOf("home", "discover", "learn", "profile")
    CompositionLocalProvider(LocalGrowStore provides store) {
    Scaffold(topBar = {
        if (route != "chat") Column(Modifier.background(Cloud).statusBarsPadding().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { navigate(if (studentId.isNotBlank()) "home" else "roles") }) { Text("CitiGrow.ai", fontWeight = FontWeight.Black, color = Navy) }
                TextButton(onClick = { store.change { store.state.put("offline", !store.state.optBoolean("offline")) } }) { Text(if (store.state.optBoolean("offline")) "Offline demo" else "Online") }
            }
            if (route !in tabs && route !in listOf("welcome", "roles")) TextButton(onClick = {
                if (history.isNotEmpty()) { route = history.last(); history = history.dropLast(1) } else navigate("roles")
            }) { Text("‹ ${session.t("back")}") }
        }
    }, bottomBar = {
        if (route in tabs && studentId.isNotBlank()) NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(route == tab, onClick = { history = emptyList(); route = tab }, icon = { Text(listOf("⌂", "◇", "▤", "○")[index]) },
                    label = { Text(session.t(tab.replaceFirstChar { it.uppercase() })) })
            }
        }
    }, floatingActionButton = {
        if (route in tabs && studentId.isNotBlank()) SmallFloatingActionButton(onClick = { navigate("helper") }, containerColor = Navy, contentColor = Cloud) { Text("Ask", Modifier.padding(8.dp)) }
    }) { padding ->
        val scroll = key(route) { rememberScrollState() }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val savedKey = "$studentId:$route"
            SideEffect { savedKeys.add(savedKey) }
            savedScreens.SaveableStateProvider(savedKey) {
                if (revision >= 0 && voiceStatus.isNotEmpty()) Surface(color = Mint, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) { Note(voiceStatus); TextButton(onClick = { voice.stop(); voiceStatus = "" }) { Text(session.t("stopReading")) } }
                }
                when {
                    route == "welcome" -> LanguageScreen(session)
                    route == "roles" -> RolesScreen(session)
                    route == "profiles" -> ProfilesScreen(session)
                    route.startsWith("pin/") -> PinScreen(session, route.removePrefix("pin/")) { id, next -> studentId = id; history = emptyList(); route = next }
                    route.startsWith("register") -> RegisterScreen(session, route.endsWith("assisted")) { id -> studentId = id; history = emptyList(); route = "chat" }
                    route == "home" -> HomeScreen(session)
                    route == "discover" -> DiscoverScreen(session)
                    route == "learn" -> LearnScreen(session)
                    route == "profile" -> ProfileScreen(session)
                    route == "chat" -> ChatScreen(session)
                    route == "assessment" -> AssessmentScreen(session)
                    route.startsWith("career/") -> CareerScreen(session, route.substringAfter('/'))
                    route == "lifemap" -> LifeMapScreen(session)
                    route.startsWith("course/") -> CourseScreen(session, route.substringAfter('/'))
                    route.startsWith("lesson/") -> LessonScreen(session, route.split('/')[1], route.split('/')[2].toInt())
                    route.startsWith("quiz/") -> QuizScreen(session, route.substringAfter('/'))
                    route.startsWith("opp/") -> OpportunityScreen(session, route.substringAfter('/'))
                    route.startsWith("sim/") -> SimulatorScreen(session, route.substringAfter('/'))
                    route == "future" -> FutureScreen(session)
                    route == "mentors" -> MentorsScreen(session)
                    route == "compare" -> CompareScreen(session)
                    route == "helper" -> HelperScreen(session)
                    route == "privacy" -> PrivacyScreen()
                    route == "parent" -> ParentScreen(session)
                    route == "teacher" -> TeacherScreen(session)
                    route == "partner" -> PartnerScreen(session)
                }
            }
            Spacer(Modifier.height(64.dp))
        }
    }
    if (message.isNotEmpty()) AlertDialog(onDismissRequest = { message = "" }, confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } }, text = { Text(message) })
    }
}
