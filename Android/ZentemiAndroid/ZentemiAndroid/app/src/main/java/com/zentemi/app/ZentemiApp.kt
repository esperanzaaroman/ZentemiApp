package com.zentemi.app

import androidx.compose.runtime.Composable
import com.zentemi.app.navigation.ZentemiNavigation
import com.zentemi.app.state.AppState

@Composable
fun ZentemiApp(appState: AppState) {
    ZentemiNavigation(appState)
}
