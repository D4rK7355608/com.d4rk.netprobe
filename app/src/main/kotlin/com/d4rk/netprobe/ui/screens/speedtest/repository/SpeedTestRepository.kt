package com.d4rk.netprobe.ui.screens.speedtest.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpeedTestRepository : SpeedTestRepositoryImplementation() {

    suspend fun calculateSpeedRepository(bytesDownloaded : Long , startTime : Long , onSuccess : (Float) -> Unit) {
        withContext(Dispatchers.IO) {
            val speed = calculateSpeedImplementation(bytesDownloaded , startTime)
            withContext(Dispatchers.Main) {
                onSuccess(speed)
            }
        }
    }

    suspend fun downloadFileWithProgressRepository(
        onProgressUpdate : (downloadedBytes : Long , totalBytes : Long) -> Unit , onSuccess : (Boolean) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            performDownloadWithProgressImplementation(onProgressUpdate)
            withContext(Dispatchers.Main) {
                onSuccess(true)
            }
        }
    }

    suspend fun getPingRepository(onSuccess : (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val pingResult = getPingImplementation()
            withContext(Dispatchers.Main) {
                onSuccess(pingResult)
            }
        }
    }

    suspend fun getWifiStrengthRepository(context : Context , onSuccess : (Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val wifiStrengthResult = getWifiStrengthImplementation(context)
            withContext(Dispatchers.Main) {
                onSuccess(wifiStrengthResult)
            }
        }
    }
}