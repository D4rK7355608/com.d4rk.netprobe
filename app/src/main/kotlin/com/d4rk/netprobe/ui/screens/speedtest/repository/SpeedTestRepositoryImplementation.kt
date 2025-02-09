package com.d4rk.netprobe.ui.screens.speedtest.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

abstract class SpeedTestRepositoryImplementation {

    private val testUrl : String = "https://github.com/D4rK7355608/GoogleProductSansFont/releases/download/v2.0_r1/GoogleProductSansFont-v2.0_r1.zip"
    private val pingHost : String = "www.google.com"

    protected suspend fun performDownloadWithProgressImplementation(onProgressUpdate: (downloadedBytes: Long, totalBytes: Long) -> Unit): ByteArray = withContext(Dispatchers.IO) { // as vrea sa nu folosim aici dispatcher pentru ca o apelam deja de pe un dispatcher
        val connection : HttpURLConnection = URL(testUrl).openConnection() as HttpURLConnection
        connection.connect()

        val totalBytes : Long = connection.contentLengthLong
        val data = ByteArray(size = 1024)
        var downloadedBytes = 0L

        connection.inputStream.buffered().use { input ->
            while (true) {
                val bytesRead : Int = input.read(data)
                if (bytesRead == -1) break
                downloadedBytes += bytesRead
                onProgressUpdate(downloadedBytes, totalBytes)
            }
        }
        data
    }

    protected fun calculateSpeedImplementation(bytesDownloaded: Long , startTime: Long): Float {
        val timeElapsedMillis : Long = System.currentTimeMillis() - startTime
        return if (timeElapsedMillis > 0) {
            (bytesDownloaded * 8).toFloat() / (timeElapsedMillis * 1000)
        } else 0f
    }

    fun getPingImplementation(): String {
        return runCatching {
            val reachable: Boolean = InetAddress.getByName(pingHost).isReachable(3000)
            if (!reachable) {
                return@runCatching "Unreachable"
            }

            val startTime: Long = System.currentTimeMillis()
            val process: Process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $pingHost")
            val result: Boolean = process.waitFor() == 0
            val endTime: Long = System.currentTimeMillis()
            if (result) "${endTime - startTime} ms" else "Timeout"
        }.getOrElse { e ->
            "Error: ${e.message}"
        }
    }

    fun getWifiStrengthImplementation(context: Context): Int {
        val wifiManager : WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (wifiManager.isWifiEnabled) {
            val networkRequest : NetworkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
            var rssiStrength = -1
            connectivityManager.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val networkCapabilities : NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)
                        rssiStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            networkCapabilities?.signalStrength ?: -1
                        } else {
                            @Suppress("DEPRECATION")
                            val rssi : Int = wifiManager.connectionInfo.rssi
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