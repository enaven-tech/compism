package com.enaven.compism.compose

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

object CompismTransitions {
    fun slideRight(durationMillis: Int = 400): ContentTransform =
        slideInHorizontally(
            animationSpec = tween(durationMillis),
            initialOffsetX = { it }
        ) togetherWith
                slideOutHorizontally(
                    animationSpec = tween(durationMillis),
                    targetOffsetX = { -it }
                )

    fun slideLeft(durationMillis: Int = 400): ContentTransform =
        slideInHorizontally(
            animationSpec = tween(durationMillis),
            initialOffsetX = { -it }
        ) togetherWith
                slideOutHorizontally(
                    animationSpec = tween(durationMillis),
                    targetOffsetX = { it }
                )

    fun fade(durationMillis: Int = 400): ContentTransform =
        fadeIn(tween(durationMillis)) togetherWith
                fadeOut(tween(durationMillis))
}