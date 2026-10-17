package com.example.citigrow.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.citigrow.data.GrowStore
import com.example.citigrow.ui.*

/** Compatibility entry point; shares the same welcome UI as the application. */
@Composable fun LandingScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { GrowStore(context) }
    val session = remember(store.revision, onContinue) {
        AppSession(store, { if (it == "roles") onContinue() }, {}, { _, _ -> }, {}, "")
    }
    CompositionLocalProvider(LocalGrowStore provides store) {
        Column(modifier) { LanguageScreen(session) }
    }
}
