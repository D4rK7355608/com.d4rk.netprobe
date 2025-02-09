package com.d4rk.netprobe.data.model.ui.screens

data class UiSpeedTestScreen(
    val inProgress : Boolean = false , val arcValue : Float = 0f , val speed : String = "0.0" , val ping : String = "-" , val wifiStrength : String = "-" , val maxSpeed : String = "-"
)