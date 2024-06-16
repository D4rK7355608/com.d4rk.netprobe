package com.d4rk.netprobe.ui.speedtest

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.database.table.UiState
import com.d4rk.netprobe.ui.animation.SpeedSmoothAnimation
import com.d4rk.netprobe.utils.bounceClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

@Composable
fun SpeedTestComposable() {
    val coroutineScope = rememberCoroutineScope()
    val downloadSpeed = remember { mutableFloatStateOf(0f) }
    val maxSpeed = remember { mutableFloatStateOf(0f) }
    val ping = remember { mutableStateOf<String?>(null) }
    val wifiStrength = remember { mutableStateOf<Int?>(null) }
    var testRunning by remember { mutableStateOf(false) }
    var currentScan by remember { mutableIntStateOf(0) }
    val speedSmooth = remember { SpeedSmoothAnimation() }
    val context = LocalContext.current
    val scanProgresses = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }
    val testUrl =
        "https://github.com/D4rK7355608/GoogleProductSansFont/releases/download/v2.0_r1/GoogleProductSansFont-v2.0_r1.zip"
    val pingHost = "www.google.com"
    val checkingString = stringResource(id = R.string.checking)

    LaunchedEffect(downloadSpeed.floatValue, scanProgresses) {
        speedSmooth.animateTo(downloadSpeed.floatValue)
        maxSpeed.floatValue = maxOf(maxSpeed.floatValue, downloadSpeed.floatValue)
    }

    val uiState = UiState(
        inProgress = testRunning,
        arcValue = speedSmooth.value,
        speed = "%.1f".format(speedSmooth.value),
        ping = ping.value ?: "-",
        wifiStrength = if (wifiStrength.value != null) (wifiStrength.value.toString() + " dBm") else "-",
        maxSpeed = if (maxSpeed.floatValue > 0f) "%.1f mbps".format(maxSpeed.floatValue) else "-"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            CircularSpeedIndicator(uiState.arcValue, 240f)
            StartButton(isEnabled = !uiState.inProgress && !testRunning) {
                testRunning = true
                coroutineScope.launch {
                    if (currentScan == 0) {
                        ping.value = checkingString
                        withContext(Dispatchers.IO) {
                            ping.value = getPing(pingHost)
                            wifiStrength.value = getWifiStrength(context)
                        }
                    }

                    repeat(5) {
                        currentScan++
                        val startTime = System.currentTimeMillis()
                        downloadFileWithProgress(testUrl) { currentBytes, totalBytes ->
                            val scanProgress =
                                (currentBytes.toFloat() / totalBytes.toFloat()) * 100f
                            if (scanProgresses.size > it) {
                                scanProgresses[it] = scanProgress
                            } else {
                                scanProgresses.add(scanProgress)
                            }
                            downloadSpeed.floatValue = calculateSpeed(currentBytes, startTime)
                        }
                    }
                    currentScan = 0
                    testRunning = false
                }
            }
            SpeedValue(uiState.speed)
        }
        AdditionalInfo(ping = ping.value ?: "-", wifiStrength = uiState.wifiStrength, maxSpeed = uiState.maxSpeed)
    }
}

fun calculateSpeed(bytesDownloaded: Long, startTime: Long): Float {
    val timeElapsedMillis = System.currentTimeMillis() - startTime
    return if (timeElapsedMillis > 0) {
        (bytesDownloaded * 8).toFloat() / (timeElapsedMillis * 1000)
    } else 0f
}

suspend fun downloadFileWithProgress(
    url: String, onProgressUpdate: (downloadedBytes: Long, totalBytes: Long) -> Unit
): ByteArray = withContext(Dispatchers.IO) {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connect()

    val totalBytes = connection.contentLengthLong
    val data = ByteArray(1024)
    var downloadedBytes = 0L

    connection.inputStream.buffered().use { input ->
        while (true) {
            val bytesRead = input.read(data)
            if (bytesRead == -1) break
            downloadedBytes += bytesRead
            onProgressUpdate(downloadedBytes, totalBytes)
        }
    }
    data
}

fun getPing(host: String): String {
    return try {
        val reachable = InetAddress.getByName(host).isReachable(3000)
        if (reachable) {
            val startTime = System.currentTimeMillis()
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $host")
            val result = process.waitFor() == 0
            val endTime = System.currentTimeMillis()
            if (result) "${endTime - startTime} ms" else "Timeout"
        } else {
            "Unreachable"
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

fun getWifiStrength(context: Context): Int {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (wifiManager.isWifiEnabled) {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        var rssiStrength = -1
        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    rssiStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        networkCapabilities?.signalStrength ?: -1
                    } else {
                        @Suppress("DEPRECATION")
                        val rssi = wifiManager.connectionInfo.rssi
                        @Suppress("DEPRECATION")
                        WifiManager.calculateSignalLevel(rssi, 5)
                    }
                }
            })

        return rssiStrength
    } else {
        return -1
    }
}

@Composable
fun SpeedValue(value: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(id = R.string.download))
        Text(
            text = value, style = MaterialTheme.typography.titleLarge
        )
        Text(stringResource(id = R.string.mbps))
    }
}

@Composable
fun StartButton(isEnabled: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .padding(bottom = 24.dp)
            .bounceClick(),
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
    ) {
        Text(
            text = stringResource(id = R.string.start), modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AdditionalInfo(ping: String, wifiStrength: String, maxSpeed: String) {
    @Composable
    fun RowScope.InfoColumn(title: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)
        ) {
            Text(title, fontSize = 12.sp)
            Text(
                value, modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp
            )
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        InfoColumn(title = stringResource(id = R.string.ping), value = ping)
        VerticalDivider()
        InfoColumn(title = stringResource(id = R.string.wifi_strength), value = wifiStrength)
        VerticalDivider()
        InfoColumn(title = stringResource(id = R.string.max_speed), value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.LightGray)
            .width(1.dp)
    )
}

@Composable
fun CircularSpeedIndicator(value: Float, angle: Float) {
    val progress = animateFloatAsState(targetValue = value, label = "")
    val drawLinesColor = MaterialTheme.colorScheme.secondary
    val drawArcsColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        drawLines(progress.value, angle, color = drawLinesColor)
        drawArcs(progress.value, angle, color = drawArcsColor)
    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float, color: Color) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * (progress / 100f)

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    for (i in 0..20) {
        drawArc(
            color = color.copy(alpha = i / 900f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
        )
    }

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = 86f, cap = StrokeCap.Round)
    )
}

fun DrawScope.drawLines(
    progress: Float, maxValue: Float, numberOfLines: Int = 40, color: Color
) {
    val oneRotation = maxValue / numberOfLines
    val startValue = (progress / 100f) * numberOfLines

    for (i in 0 until numberOfLines) {
        val alpha = if (i >= startValue) 1f else 0f
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                color.copy(alpha = alpha),
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                6f,
                StrokeCap.Round
            )
        }
    }
}