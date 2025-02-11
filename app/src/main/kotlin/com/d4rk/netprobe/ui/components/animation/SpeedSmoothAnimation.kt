package com.d4rk.netprobe.ui.components.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
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
    initialValue : Float = 0f , private val animationSpec : AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy , stiffness = Spring.StiffnessLow
    )
) {
    var value : Float by mutableFloatStateOf(initialValue)

    private val animationState : Animatable<Float , AnimationVector1D> = Animatable(initialValue = initialValue)
    private val scope : CoroutineScope = CoroutineScope(context = Job() + Dispatchers.Main)

    suspend fun animateTo(targetValue : Float) {
        animationState.animateTo(targetValue = targetValue , animationSpec = animationSpec) {
            this@SpeedSmoothAnimation.value = this.value
        }
    }

    fun resetTo(targetValue : Float) {
        value = targetValue
        scope.launch {
            animationState.snapTo(targetValue = targetValue)
        }
    }
}