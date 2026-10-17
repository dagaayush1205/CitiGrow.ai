package com.example.citigrow.ui
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.citigrow.data.*
import com.example.citigrow.ui.theme.*
import com.example.citigrow.voice.VoiceController
import com.example.citigrow.voice.FormNarrator
import com.example.citigrow.voice.VoicePhase
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject

class AppSession(val store: GrowStore, val navigate: (String) -> Unit, val tell: (String) -> Unit,
    val speak: (String, String) -> Unit, val openUrl: (String) -> Unit, val studentId: String,
    val narrator: FormNarrator? = null) {
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
    val voice = remember { VoiceController(context) }
    val voiceState = voice.state
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
        voice.read(key, text, store.state.text("lang"))
    }, { url ->
        if (store.state.optBoolean("offline")) message = "Official portals need internet. Your checklist is saved on this device."
        else if (url.startsWith("https://")) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { message = "No browser is available on this device." }
    }, studentId, voice)
    DisposableEffect(voice, context) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) voice.stop() }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer); voice.close() }
    }
    val p = session.p
    val titleKey = when (route.substringBefore('/')) {
        "welcome" -> "chooseLang"; "roles" -> "chooseStart"; "profiles" -> "Who is learning today?"
        "home" -> "Home"; "discover" -> "segCareers"; "learn" -> "Learn"; "profile" -> "Profile"
        "chat" -> "q_" + store.data.arr("FLOW").optString(p.optInt("chatStep"), "enjoy")
        "assessment" -> "aserTitle"; "lifemap" -> "Life Map"; "parent" -> "Parent view"; "teacher" -> "Teacher view"
        else -> route.substringBefore('/').replaceFirstChar { it.uppercase() }
    }
    LaunchedEffect(route, studentId, store.state.text("lang"), p.optBoolean("readAloud", true)) {
        voice.stop()
        if (!route.startsWith("register") && p.optBoolean("readAloud", true)) session.speak(titleKey, session.t(titleKey))
    }
    BackHandler(route != "welcome") {
        voice.stop()
        if (history.isNotEmpty()) { route = history.last(); history = history.dropLast(1) }
        else navigate(if (route == "roles") "welcome" else if (studentId.isNotBlank() && route != "home") "home" else "roles")
    }
    val tabs = listOf("home", "discover", "learn", "profile")
    val onboarding = route in listOf("welcome", "roles", "profiles") || route.startsWith("pin/") || route.startsWith("register")
    SideEffect {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = if (route == "welcome") SystemBarStyle.dark(Navy.toArgb()) else SystemBarStyle.light(Cloud.toArgb(), Cloud.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.White.toArgb(), Color.White.toArgb()))
    }
    fun back() {
        if (history.isNotEmpty()) { route = history.last(); history = history.dropLast(1) }
        else navigate(if (route == "roles") "welcome" else if (studentId.isNotBlank()) "home" else "roles")
    }
    CompositionLocalProvider(LocalGrowStore provides store) {
    MaterialTheme(typography = com.example.citigrow.ui.theme.growTypography(store.state.text("lang", "en"))) {
    Scaffold(containerColor = if (route == "welcome") Navy else Cloud, topBar = {
        if (route != "chat" && route != "welcome") Column(Modifier.background(if (onboarding) Cloud else Color.White).statusBarsPadding()) {
            if (!onboarding) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(34.dp).background(Navy, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { GrowIcon("sprout", Modifier.size(18.dp), TealLight) }
                    Column(Modifier.weight(1f)) {
                        Row { Text("CitiGrow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold); Text(".ai", color = Blue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold) }
                        Text(if (studentId.isNotBlank()) "STUDENT" else route.substringBefore('/').uppercase(), style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                    val offline = store.state.optBoolean("offline")
                    Surface(onClick = { store.change { store.state.put("offline", !offline) } }, color = if (offline) Gold else Mint, contentColor = if (offline) GoldInk else TealInk, shape = RoundedCornerShape(50)) {
                        Row(Modifier.heightIn(min = 44.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            GrowIcon(if (offline) "wifiOff" else "wifi", Modifier.size(16.dp)); Text(if (offline) "Offline demo" else "Online", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Box(Modifier.clickable { navigate(if (studentId.isNotBlank()) "profile" else "roles") }) {
                        if (studentId.isNotBlank()) Avatar(p.text("name"), size = 40.dp) else IconTile("swap", GreyTint, Navy, 40.dp)
                    }
                }
                HorizontalDivider(color = Border)
            }
            if (onboarding || route !in tabs && route !in listOf("parent", "teacher", "partner")) Row(Modifier.fillMaxWidth().background(Cloud).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(onClick = ::back, shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 1.dp) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { GrowIcon("chevL", description = session.t("back")) }
                }
                if (!onboarding) Text(session.t(titleKey), style = MaterialTheme.typography.titleLarge)
            }
        }
    }, bottomBar = {
        Column(Modifier.background(if (route == "welcome") Cloud else Color.White)) {
            if (revision >= 0 && voiceState.phase != VoicePhase.IDLE) Surface(color = Navy, contentColor = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GrowIcon("volume", Modifier.size(18.dp), TealLight)
                    Text(voiceState.message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { voice.stop() }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text(session.t("stopReading"), style = MaterialTheme.typography.labelMedium) }
                }
            }
            if (route in tabs && studentId.isNotBlank()) {
                HorizontalDivider(color = Border)
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = route == tab
                        Column(Modifier.weight(1f).clickable { history = emptyList(); route = tab }.semantics { this.selected = selected; role = Role.Tab }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(Modifier.size(52.dp, 32.dp).background(if (selected) BlueTint else Color.Transparent, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                GrowIcon(listOf("home", "compass", "book", "user")[index], tint = if (selected) Blue else Muted)
                            }
                            Text(session.t(tab.replaceFirstChar { it.uppercase() }), style = MaterialTheme.typography.labelSmall, color = if (selected) Blue else Muted)
                        }
                    }
                }
            }
        }
    }, floatingActionButton = {
        if (route in tabs && studentId.isNotBlank()) FloatingActionButton(onClick = { navigate("helper") }, shape = RoundedCornerShape(26.dp), containerColor = Navy, contentColor = Color.White, modifier = Modifier.height(52.dp)) {
            Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { GrowIcon("sparkles", tint = TealLight); Text(session.t("askShort"), style = MaterialTheme.typography.labelLarge) }
        }
    }) { padding ->
        val scroll = key(route) { rememberScrollState() }
        Column(Modifier.fillMaxSize().padding(padding).background(Cloud).verticalScroll(scroll).then(if (route == "welcome") Modifier else Modifier.padding(horizontal = 16.dp, vertical = 14.dp)), verticalArrangement = Arrangement.spacedBy(if (route == "welcome") 0.dp else 14.dp)) {
            val savedKey = "$studentId:$route"
            SideEffect { savedKeys.add(savedKey) }
            savedScreens.SaveableStateProvider(savedKey) {
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
            if (route != "welcome") Spacer(Modifier.height(28.dp))
        }
    }
    if (message.isNotEmpty()) AlertDialog(onDismissRequest = { message = "" }, confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } }, text = { Text(message) })
    }
    }
}
