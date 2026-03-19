package com.enaven.compism.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier

@Composable
fun <K : Any, S : HasScreen<K>> CompismDisplay(
    state : S,
    transitionFor : (S, S) -> ContentTransform,
    screensToReset : (S) -> List<K>,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (S) -> Unit
) {
    val holder = rememberSaveableStateHolder()

    // ------------------------------------------------------------------------
    // Explicitly reset the state of certain screens (like when backing out)
    // ------------------------------------------------------------------------
    LaunchedEffect(state.getScreen()) {
        val keysToClear = screensToReset(state)
        keysToClear.forEach { key ->
            holder.removeState(key)
        }
    }
    // ------------------------------------------------------------------------

    AnimatedContent(
        targetState = state,
        contentKey = { it.getScreen() },
        transitionSpec = {
            if (initialState.getScreen() == targetState.getScreen()) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                transitionFor(initialState, targetState)
            }
         },
        modifier = modifier,
        label = "nav"
    ) { s ->
        holder.SaveableStateProvider(key = s.getScreen()) {
            content(s)
        }
    }
}

interface HasScreen<K> {
    fun getScreen(): K
}