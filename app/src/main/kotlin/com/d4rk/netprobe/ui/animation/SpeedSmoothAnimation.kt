package com.d4rk.netprobe.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

class SpeedSmoothAnimation(
    initialValue: Float = 0f,
    private val animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
) {
    var value by mutableFloatStateOf(initialValue)

    private val animatable = Animatable(initialValue)

    suspend fun animateTo(targetValue: Float) {
        animatable.animateTo(targetValue, animationSpec) {
            this@SpeedSmoothAnimation.value = this.value
        }
    }
}