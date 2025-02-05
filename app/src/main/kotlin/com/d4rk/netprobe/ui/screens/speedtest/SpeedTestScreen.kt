package com.d4rk.netprobe.ui.screens.speedtest

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.model.ui.screens.UiSpeedTestScreen

@Composable
fun SpeedTestScreen() {
    val viewModel : SpeedTestViewModel = viewModel()
    val uiState = UiSpeedTestScreen(
        inProgress = viewModel.testRunning , arcValue = viewModel.speedSmooth.value , speed = "%.1f".format(viewModel.speedSmooth.value) , ping = viewModel.ping.value ?: "-" , wifiStrength = if (viewModel.wifiStrength.value != null) {
            viewModel.wifiStrength.value.toString() + " dBm"
        }
        else {
            "-"
        } , maxSpeed = viewModel.maxSpeed.value ?: "-"
    )

    LaunchedEffect(key1 = viewModel.downloadSpeed.floatValue , key2 = viewModel.scanProgresses) {
        viewModel.speedSmooth.animateTo(targetValue = viewModel.downloadSpeed.floatValue)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp) , verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter , modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1f)
        ) {
            CircularSpeedIndicator(value = uiState.arcValue , angle = 240f)
            StartButton(isEnabled = ! uiState.inProgress && ! viewModel.testRunning) {
                viewModel.startSpeedTest()
            }
            SpeedValue(value = uiState.speed)
        }
        AdditionalInfo(
            ping = uiState.ping , wifiStrength = uiState.wifiStrength , maxSpeed = uiState.maxSpeed
        )
    }
}


@Composable
fun SpeedValue(value : String) {
    Column(
        Modifier.fillMaxSize() , verticalArrangement = Arrangement.Center , horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(id = R.string.download))
        Text(
            text = value , style = MaterialTheme.typography.titleLarge
        )
        Text(stringResource(id = R.string.mbps))
    }
}

@Composable
fun StartButton(isEnabled : Boolean , onClick : () -> Unit) {
    FilledTonalButton(
        onClick = onClick ,
        modifier = Modifier.padding(bottom = 24.dp) ,
        enabled = isEnabled ,
        shape = RoundedCornerShape(size = 24.dp) ,
    ) {
        Text(
            text = stringResource(id = R.string.start) , modifier = Modifier.padding(horizontal = 24.dp , vertical = 4.dp)
        )
    }
}

@Composable
fun AdditionalInfo(ping : String , wifiStrength : String , maxSpeed : String) {

    @Composable
    fun RowScope.InfoColumn(title : String , value : String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.weight(weight = 1f)) {
            Text(text = title , fontSize = 12.sp)
            Text(text = value , modifier = Modifier.padding(vertical = 8.dp) , fontSize = 16.sp)
        }
    }

    Row(modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)) {
        InfoColumn(title = stringResource(id = R.string.ping) , value = ping)
        VerticalDivider()
        InfoColumn(
            title = stringResource(id = R.string.wifi_strength) , value = wifiStrength
        )
        VerticalDivider()
        InfoColumn(title = stringResource(id = R.string.max_speed) , value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
                .fillMaxHeight()
                .background(color = Color.LightGray)
                .width(width = 1.dp)
    )
}

@Composable
fun CircularSpeedIndicator(value : Float , angle : Float) {
    val progress : State<Float> = animateFloatAsState(targetValue = value , label = "")
    val drawLinesColor : Color = MaterialTheme.colorScheme.secondary
    val drawArcsColor : Color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
                .fillMaxSize()
                .padding(all = 40.dp)
    ) {
        drawLines(progress = progress.value , maxValue = angle , color = drawLinesColor)
        drawArcs(progress = progress.value , maxValue = angle , color = drawArcsColor)
    }
}

fun DrawScope.drawArcs(progress : Float , maxValue : Float , color : Color) {
    val startAngle : Float = 270 - maxValue / 2
    val sweepAngle : Float = maxValue * (progress / 100f)

    val topLeft = Offset(x = 50f , y = 50f)
    val size = Size(width = size.width - 100f , height = size.height - 100f)

    (0..20).forEach { animationProgress ->
        drawArc(
            color = color.copy(alpha = animationProgress / 900f) , startAngle = startAngle , sweepAngle = sweepAngle , useCenter = false , topLeft = topLeft , size = size , style = Stroke(width = 480f - animationProgress * 20f , cap = StrokeCap.Round)
        )
    }

    drawArc(
        color = color , startAngle = startAngle , sweepAngle = sweepAngle , useCenter = false , topLeft = topLeft , size = size , style = Stroke(width = 86f , cap = StrokeCap.Round)
    )
}

fun DrawScope.drawLines(
    progress : Float , maxValue : Float , numberOfLines : Int = 40 , color : Color
) {
    val oneRotation : Float = maxValue / numberOfLines
    val startValue : Float = (progress / 100f) * numberOfLines

    (0 until numberOfLines).forEach { index ->
        val alpha : Float = if (index >= startValue) 1f else 0f;
        rotate(degrees = index * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                color = color.copy(alpha = alpha) , start = Offset(x = if (index % 5 == 0) 80f else 30f , y = size.height / 2) , end = Offset(x = 0f , y = size.height / 2) , strokeWidth = 6f , cap = StrokeCap.Round
            )
        }
    }
}