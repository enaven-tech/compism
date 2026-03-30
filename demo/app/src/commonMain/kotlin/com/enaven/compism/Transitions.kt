package com.enaven.compism

import androidx.compose.animation.ContentTransform
import com.enaven.compism.compose.CompismTransitions

fun transitionFor(
    from : AppScreen,
    to : AppScreen
) : ContentTransform {
    return when {
        from == AppScreen.ScreenA && to == AppScreen.ScreenB -> CompismTransitions.slideRight()
        from == AppScreen.ScreenB && to == AppScreen.ScreenA -> CompismTransitions.slideLeft()
        from == AppScreen.ScreenB && to == AppScreen.ScreenC -> CompismTransitions.slideRight()
        from == AppScreen.ScreenC && to == AppScreen.ScreenB -> CompismTransitions.slideLeft()
        else -> CompismTransitions.fade()
    }
}

fun screensToReset(state : AppState) : List<AppScreen> {
    return when (state) {
        // Clear the content of ScreenB as we now leave the screen permanently
        is AppState.ScreenA -> listOf(AppScreen .ScreenB)
        is AppState.ScreenB -> emptyList()
        is AppState.ScreenC -> emptyList()
    }
}