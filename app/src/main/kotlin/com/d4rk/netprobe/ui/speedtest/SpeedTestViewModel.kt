package com.d4rk.netprobe.ui.speedtest

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.netprobe.R
import com.d4rk.netprobe.ui.animation.SpeedSmoothAnimation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {
    private val coroutineScope = viewModelScope
    val downloadSpeed = mutableFloatStateOf(0f)
    val maxSpeed = mutableStateOf<String?>(null)
    val ping = mutableStateOf<String?>(null)
    val wifiStrength = mutableStateOf<String?>(null)
    var testRunning by mutableStateOf(false)
    private var currentScan by mutableIntStateOf(0)
    val scanProgresses = mutableStateListOf(0f, 0f, 0f, 0f, 0f)
    val speedSmooth = SpeedSmoothAnimation()
    private val testUrl =
        "https://github.com/D4rK7355608/GoogleProductSansFont/releases/download/v2.0_r1/GoogleProductSansFont-v2.0_r1.zip"
    private val pingHost = "www.google.com"
    private val checkingString =getApplication<Application>().getString(R.string.checking)

    init {
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                speedSmooth.animateTo(downloadSpeed.floatValue)
            }
        }
    }

    fun startSpeedTest() {
        resetStates()
        testRunning = true
        coroutineScope.launch {
            if (currentScan == 0) {
                ping.value = checkingString
                wifiStrength.value = checkingString
                maxSpeed.value = checkingString
                withContext(Dispatchers.IO) {
                    ping.value = getPing()
                    wifiStrength.value =
                        getWifiStrength(getApplication()).toString()
                    maxSpeed.value =  maxOf(
                        maxSpeed.value?.toFloatOrNull() ?: 0f,
                        downloadSpeed.floatValue
                    ).toString()
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

    private fun resetStates() {
        downloadSpeed.floatValue = 0f
        maxSpeed.value = null
        ping.value = null
        wifiStrength.value = null
        scanProgresses.clear()
        scanProgresses.addAll(listOf(0f, 0f, 0f, 0f, 0f))
        speedSmooth.resetTo(0f)
        currentScan = 0
    }

    private fun calculateSpeed(bytesDownloaded: Long, startTime: Long): Float {
        val timeElapsedMillis = System.currentTimeMillis() - startTime
        return if (timeElapsedMillis > 0) {
            (bytesDownloaded * 8).toFloat() / (timeElapsedMillis * 1000)
        } else 0f
    }

    private suspend fun downloadFileWithProgress(
        url: String,
        onProgressUpdate: (downloadedBytes: Long, totalBytes: Long) -> Unit
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

    private fun getPing(): String {
        return try {
            val reachable = InetAddress.getByName(pingHost).isReachable(3000)
            if (reachable) {
                val startTime = System.currentTimeMillis()
                val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $pingHost")
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

    private fun getWifiStrength(context: Context): Int {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (wifiManager.isWifiEnabled) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            var rssiStrength = -1
            connectivityManager.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val networkCapabilities =
                            connectivityManager.getNetworkCapabilities(network)
                        rssiStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            networkCapabilities?.signalStrength ?: -1
                        } else {
                            @Suppress("DEPRECATION")
                            val rssi = wifiManager.connectionInfo.rssi
                            @Suppress("DEPRECATION")
                            WifiManager.calculateSignalLevel(rssi, 5)
                        }
                    }
                }
            )

            return rssiStrength
        } else {
            return -1
        }
    }
}