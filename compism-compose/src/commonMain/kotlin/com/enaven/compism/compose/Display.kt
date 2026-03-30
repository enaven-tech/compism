package com.enaven.compism.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier

@Composable
fun <K : Any, S : HasScreen<K>> CompismDisplay(
    state: S,
    transitionFor: (K, K) -> ContentTransform,
    screensToReset: (S) -> List<K>,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (S) -> Unit
) {
    val holder = rememberSaveableStateHolder()

    val currentScreen = state.getScreen()

    val statePerScreen = remember { mutableMapOf<K, S>() }
    statePerScreen[currentScreen] = state

    LaunchedEffect(currentScreen) {
        val keysToClear = screensToReset(state)
        keysToClear.forEach { key ->
            holder.removeState(key)
            statePerScreen.remove(key)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            transitionFor(initialState, targetState)
        },
        modifier = modifier,
        label = "nav"
    ) { screen ->
        val screenState = statePerScreen[screen] ?: state

        holder.SaveableStateProvider(key = screen) {
            content(screenState)
        }
    }
}

interface HasScreen<K> {
    fun getScreen(): K
}