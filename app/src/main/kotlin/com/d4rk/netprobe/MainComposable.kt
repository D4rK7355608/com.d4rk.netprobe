package com.d4rk.netprobe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.d4rk.netprobe.ads.FullBannerAdsComposable
import com.d4rk.netprobe.data.datastore.DataStore
import com.d4rk.netprobe.data.model.ui.navigation.NavigationDrawerItem
import com.d4rk.netprobe.data.model.ui.navigation.BottomNavigationScreen
import com.d4rk.netprobe.ui.help.HelpActivity
import com.d4rk.netprobe.ui.scanner.HomeComposable
import com.d4rk.netprobe.ui.settings.SettingsActivity
import com.d4rk.netprobe.ui.speedtest.SpeedTestComposable
import com.d4rk.netprobe.ui.support.SupportActivity
import com.d4rk.netprobe.utils.IntentUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposable() {
    val bottomBarItems = listOf(
        BottomNavigationScreen.SpeedTest , BottomNavigationScreen.LinkScan
    )
    val drawerItems = listOf(
        NavigationDrawerItem(
            title = R.string.settings ,
            selectedIcon = Icons.Outlined.Settings ,
        ) ,
        NavigationDrawerItem(
            title = R.string.help_and_feedback ,
            selectedIcon = Icons.AutoMirrored.Outlined.HelpOutline ,
        ) ,
        NavigationDrawerItem(
            title = R.string.updates ,
            selectedIcon = Icons.AutoMirrored.Outlined.EventNote ,
        ) ,
        NavigationDrawerItem(
            title = R.string.share , selectedIcon = Icons.Outlined.Share
        ) ,
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStore = DataStore.getInstance(context)
    val selectedItemIndex by rememberSaveable { mutableIntStateOf(- 1) }
    ModalNavigationDrawer(drawerState = drawerState , drawerContent = {
        ModalDrawerSheet {
            Spacer(modifier = Modifier.height(16.dp))
            drawerItems.forEachIndexed { index , item ->
                val title = stringResource(item.title)
                NavigationDrawerItem(label = { Text(text = title) } ,
                                     selected = index == selectedItemIndex ,
                                     onClick = {
                                         when (item.title) {

                                             R.string.settings -> {
                                                 IntentUtils.openActivity(
                                                     context , SettingsActivity::class.java
                                                 )
                                             }

                                             R.string.help_and_feedback -> {
                                                 IntentUtils.openActivity(
                                                     context , HelpActivity::class.java
                                                 )
                                             }

                                             R.string.updates -> {
                                                 IntentUtils.openUrl(
                                                     context ,
                                                     "https://github.com/D4rK7355608/${context.packageName}/blob/master/CHANGELOG.md"
                                                 )
                                             }

                                             R.string.share -> {
                                                 IntentUtils.shareApp(context)
                                             }
                                         }
                                         scope.launch {
                                             drawerState.close()
                                         }
                                     } ,
                                     icon = {
                                         Icon(
                                             item.selectedIcon , contentDescription = title
                                         )
                                     } ,
                                     badge = {
                                         item.badgeCount?.let {
                                             Text(text = item.badgeCount.toString())
                                         }
                                     } ,
                                     modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            }
        }
    } , content = {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = stringResource(R.string.app_name))
            } , navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu , contentDescription = "Menu"
                    )
                }
            } , actions = {
                IconButton(onClick = {
                    IntentUtils.openActivity(context , SupportActivity::class.java)
                }) {
                    Icon(
                        Icons.Outlined.VolunteerActivism , contentDescription = "Support"
                    )
                }
            })
        } , bottomBar = {
            Column {
                FullBannerAdsComposable(modifier = Modifier.fillMaxWidth(), dataStore = dataStore)
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    bottomBarItems.forEach { screen ->
                        NavigationBarItem(icon = {
                            val iconResource =
                                if (currentRoute == screen.route) screen.selectedIcon else screen.icon
                            Icon(iconResource, contentDescription = null)
                        },
                            label = { Text(stringResource(screen.title)) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            })
                    }
                }
            }
        }) { innerPadding ->
            NavHost(navController , startDestination = BottomNavigationScreen.SpeedTest.route) {
                composable(BottomNavigationScreen.SpeedTest.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SpeedTestComposable()
                    }
                }
                composable(BottomNavigationScreen.LinkScan.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeComposable()
                    }
                }
            }
        }
    })
}