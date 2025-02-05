package com.d4rk.netprobe.data.model.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.Scanner
import androidx.compose.material.icons.sharp.Scanner
import androidx.compose.ui.graphics.vector.ImageVector
import com.d4rk.netprobe.R
import com.d4rk.netprobe.utils.constants.ui.bottombar.BottomBarRoutes

sealed class BottomNavigationScreen(
    val route : String , val icon : ImageVector , val selectedIcon : ImageVector , val title : Int
) {
    data object SpeedTest :
        BottomNavigationScreen(BottomBarRoutes.SPEED_TEST , Icons.Outlined.Speed , Icons.Filled.Speed , R.string.speed_test)

    data object LinkScan : BottomNavigationScreen(
        BottomBarRoutes.IP_ANALYZER , Icons.Sharp.Scanner , Icons.Rounded.Scanner , R.string.scanner
    )
}