package com.d4rk.netprobe.ui.speedtest

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.d4rk.netprobe.data.db.table.UiState
import com.d4rk.netprobe.utils.bounceClick
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun SpeedTestComposable() {
    val coroutineScope = rememberCoroutineScope()
    val animation = remember { Animatable(0f) }
    val maxSpeed = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animation.value) {
        maxSpeed.floatValue = max(maxSpeed.floatValue , animation.value * 100f)
    }

    val state = animation.toUiState(maxSpeed.floatValue)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally ,
        modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding() ,
        verticalArrangement = Arrangement.SpaceBetween ,
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter ,
            modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
        ) {
            CircularSpeedIndicator(state.arcValue , 240f)
            StartButton(! state.inProgress) {
                coroutineScope.launch {
                    maxSpeed.floatValue = 0f
                    startAnimation(animation)
                }
            }
            SpeedValue(state.speed)
        }
        AdditionalInfo(state.ping , state.maxSpeed)
    }
}

suspend fun startAnimation(animation : Animatable<Float , AnimationVector1D>) {
    animation.animateTo(0.84f , keyframes {
        durationMillis = 9000
        0f at 0 using CubicBezierEasing(0f , 1.5f , 0.8f , 1f)
        0.72f at 1000 using CubicBezierEasing(0.2f , - 1.5f , 0f , 1f)
        0.76f at 2000 using CubicBezierEasing(0.2f , - 2f , 0f , 1f)
        0.78f at 3000 using CubicBezierEasing(0.2f , - 1.5f , 0f , 1f)
        0.82f at 4000 using CubicBezierEasing(0.2f , - 2f , 0f , 1f)
        0.85f at 5000 using CubicBezierEasing(0.2f , - 2f , 0f , 1f)
        0.89f at 6000 using CubicBezierEasing(0.2f , - 1.2f , 0f , 1f)
        0.82f at 7500 using LinearOutSlowInEasing
    })
}

fun Animatable<Float , AnimationVector1D>.toUiState(maxSpeed : Float) = UiState(
    arcValue = value ,
    speed = "%.1f".format(value * 100) ,
    ping = if (value > 0.2f) "${(value * 15).roundToInt()} ms" else "-" ,
    maxSpeed = if (maxSpeed > 0f) "%.1f mbps".format(maxSpeed) else "-" ,
    inProgress = isRunning
)

@Composable
fun SpeedValue(value : String) {
    Column(
        Modifier.fillMaxSize() ,
        verticalArrangement = Arrangement.Center ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DOWNLOAD")
        Text(
            text = value , style = MaterialTheme.typography.titleLarge
        )
        Text("mbps")
    }
}

@Composable
fun StartButton(isEnabled : Boolean , onClick : () -> Unit) {
    FilledTonalButton(
        onClick = onClick ,
        modifier = Modifier
                .padding(bottom = 24.dp)
                .bounceClick() ,
        enabled = isEnabled ,
        shape = RoundedCornerShape(24.dp) ,
    ) {
        Text(
            text = "START" , modifier = Modifier.padding(horizontal = 24.dp , vertical = 4.dp)
        )
    }
}

@Composable
fun AdditionalInfo(ping : String , maxSpeed : String) {

    @Composable
    fun RowScope.InfoColumn(title : String , value : String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.weight(1f)
        ) {
            Text(title)
            Text(
                value , modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    Row(
        Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
    ) {
        InfoColumn(title = "PING" , value = ping)
        VerticalDivider()
        InfoColumn(title = "MAX SPEED" , value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
                .fillMaxHeight()
                .background(Color(0xFF414D66))
                .width(1.dp)
    )
}


@Composable
fun CircularSpeedIndicator(value : Float , angle : Float) {
    val drawLinesColor = MaterialTheme.colorScheme.secondary
    val drawArcsColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
    ) {
        drawLines(value , angle , color = drawLinesColor)
        drawArcs(value , angle , color = drawArcsColor)
    }
}

fun DrawScope.drawArcs(progress : Float , maxValue : Float , color : Color) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f , 50f)
    val size = Size(size.width - 100f , size.height - 100f)

    for (i in 0..20) {
        drawArc(
            color = color.copy(alpha = i / 900f) ,
            startAngle = startAngle ,
            sweepAngle = sweepAngle ,
            useCenter = false ,
            topLeft = topLeft ,
            size = size ,
            style = Stroke(width = 80f + (20 - i) * 20 , cap = StrokeCap.Round)
        )
    }

    drawArc(
        color = color ,
        startAngle = startAngle ,
        sweepAngle = sweepAngle ,
        useCenter = false ,
        topLeft = topLeft ,
        size = size ,
        style = Stroke(width = 86f , cap = StrokeCap.Round)
    )
}

fun DrawScope.drawLines(
    progress : Float , maxValue : Float , numberOfLines : Int = 40 , color : Color
) {
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                color ,
                Offset(if (i % 5 == 0) 80f else 30f , size.height / 2) ,
                Offset(0f , size.height / 2) ,
                8f ,
                StrokeCap.Round
            )
        }
    }
}
