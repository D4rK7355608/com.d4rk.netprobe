package com.d4rk.netprobe.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SpeedSmoothAnimation(
    initialValue: Float = 0f,
    private val animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
) {
    var value by mutableFloatStateOf(initialValue)

    private val animatable = Animatable(initialValue)
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    suspend fun animateTo(targetValue: Float) {
        animatable.animateTo(targetValue, animationSpec) {
            this@SpeedSmoothAnimation.value = this.value
        }
    }

    fun resetTo(targetValue: Float) {
        value = targetValue
        scope.launch {
            animatable.snapTo(targetValue)
        }
    }
}