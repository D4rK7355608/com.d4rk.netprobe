package com.d4rk.netprobe.ui.screens.speedtest

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.model.ui.screens.UiSpeedTestScreen
import com.d4rk.netprobe.ui.components.animation.SpeedSmoothAnimation
import com.d4rk.netprobe.ui.screens.speedtest.repository.SpeedTestRepository
import com.d4rk.netprobe.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

open class SpeedTestViewModel(application : Application) : BaseViewModel(application) {

    private val _uiState : MutableState<UiSpeedTestScreen> = mutableStateOf(value = UiSpeedTestScreen())
    val uiState : State<UiSpeedTestScreen> get() = _uiState

    val speedSmooth : SpeedSmoothAnimation = SpeedSmoothAnimation()
    private val repository : SpeedTestRepository = SpeedTestRepository()
    private var currentScan : Int by mutableIntStateOf(value = 0)

    fun startSpeedTest(context : Context) {
        viewModelScope.launch(context = coroutineExceptionHandler) {
            resetStates()
            updateUiState { it.copy(inProgress = true) }

            if (currentScan == 0) {
                updateUiState {
                    it.copy(
                        ping = context.getString(R.string.checking) , wifiStrength = context.getString(R.string.checking) , maxSpeed = context.getString(R.string.checking)
                    )
                }

                repository.getPingRepository { pingResult ->
                    updateUiState { it.copy(ping = pingResult) }
                }

                repository.getWifiStrengthRepository(context = getApplication()) { wifiStrengthResult ->
                    updateUiState { it.copy(wifiStrength = "$wifiStrengthResult dBm") }
                }
            }

            repeat(5) {
                currentScan ++
                val startTime = System.currentTimeMillis()

                repository.downloadFileWithProgressRepository(onProgressUpdate = { currentBytes , totalBytes ->
                    val arcValue : Float = currentBytes.toFloat() / totalBytes.toFloat() * 240f

                    updateUiState { it.copy(arcValue = arcValue) }
                    updateSpeed(currentBytes = currentBytes , startTime = startTime)
                } , onSuccess = {})
            }

            currentScan = 0
            updateUiState { it.copy(inProgress = false) }
        }
    }

    private fun updateSpeed(currentBytes : Long , startTime : Long) {
        viewModelScope.launch(context = coroutineExceptionHandler) {
            repository.calculateSpeedRepository(bytesDownloaded = currentBytes , startTime = startTime) { currentSpeed ->
                updateUiState {
                    it.copy(speed = "%.1f".format(currentSpeed) , maxSpeed = "%.1f".format(maxOf(it.maxSpeed.toFloatOrNull() ?: 0f , currentSpeed)))
                }
            }
        }
    }

    private fun resetStates() {
        viewModelScope.launch(context = coroutineExceptionHandler) {
            speedSmooth.resetTo(targetValue = 0f)
            currentScan = 0
            updateUiState {
                UiSpeedTestScreen(
                    inProgress = false , arcValue = 0f , speed = "0.0" , ping = "-" , wifiStrength = "-" , maxSpeed = "-"
                )
            }
        }
    }

    private fun updateUiState(update : (UiSpeedTestScreen) -> UiSpeedTestScreen) {
        viewModelScope.launch(context = coroutineExceptionHandler) {
            _uiState.value = update(_uiState.value)
        }
    }
}
