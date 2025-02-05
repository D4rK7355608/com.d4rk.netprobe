package com.d4rk.netprobe.ui.screens.scanner


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.outlined.Scanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.ButtonIconSpacer
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.model.ui.screens.UiScannerScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen() {
    val viewModel : ScanViewModel = viewModel()

    val context : Context = LocalContext.current
    val state : State<UiScannerScreen> = viewModel.state.collectAsState()
    val snackbarHostState : SnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    var editTextIp : String by remember { mutableStateOf(value = "") }

    LaunchedEffect(key1 = viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collectLatest { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message , duration = SnackbarDuration.Short
                )
            }
        }
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (scrollView : ConstrainedLayoutReference , fab : ConstrainedLayoutReference) = createRefs()

        Column(modifier = Modifier
                .fillMaxSize()
                .constrainAs(ref = scrollView) {
                    top.linkTo(anchor = parent.top)
                    bottom.linkTo(anchor = parent.bottom)
                    start.linkTo(anchor = parent.start)
                    end.linkTo(anchor = parent.end)
                }
                .padding(all = 24.dp)) {
            OutlinedTextField(value = editTextIp ,
                              onValueChange = { editTextIp = it } ,
                              label = { Text(text = context.getString(R.string.ip_address)) } ,
                              placeholder = { Text(text = context.getString(R.string.placeholder_ip_address)) } ,
                              keyboardOptions = KeyboardOptions(
                                  keyboardType = KeyboardType.Text , imeAction = ImeAction.Next
                              ) ,
                              modifier = Modifier.fillMaxWidth() ,
                              singleLine = true)
            Text(text = context.getString(R.string.your_ip , state.value.ipAddress) ,
                 modifier = Modifier
                         .padding(top = 4.dp)
                         .clickable {
                             val clipboardManager =
                                     getSystemService(context , ClipboardManager::class.java)
                             val clipData = ClipData.newPlainText("Label" , state.value.ipAddress)
                             clipboardManager?.setPrimaryClip(clipData)
                             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) Toast
                                     .makeText(
                                         context , R.string.copied_to_clipboard , Toast.LENGTH_SHORT
                                     )
                                     .show()
                         })
            FilledTonalButton(onClick = {
                if (editTextIp.isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.snack_enter_ip) ,
                            duration = SnackbarDuration.Short ,
                            withDismissAction = true
                        )
                    }
                    return@FilledTonalButton
                }
                viewModel.scanIpAddress(editTextIp)
            } , modifier = Modifier
                    .padding(top = 4.dp)
                    .wrapContentSize()
                    .bounceClick()) {
                Icon(
                    imageVector = Icons.Outlined.Scanner , contentDescription = null
                )
                ButtonIconSpacer()
                Text(text = context.getString(R.string.scan))
            }

            state.value.scanResult?.let { scanResult ->
                AnimatedText(
                    text = context.getString(
                        R.string.ip_address_result , scanResult.ipAddress
                    ) , modifier = Modifier.padding(top = 4.dp)
                )
                AnimatedText(
                    text = context.getString(
                        R.string.response_code , scanResult.responseCode
                    ) , modifier = Modifier.padding(top = 4.dp)
                )
                AnimatedText(
                    text = context.getString(R.string.function , scanResult.function) ,
                    modifier = Modifier.padding(top = 4.dp)
                )
                AnimatedText(
                    text = context.getString(
                        R.string.response , "${scanResult.responseTime} ms"
                    ) , modifier = Modifier.padding(top = 4.dp)
                )
                AnimatedText(
                    text = if (scanResult.isCamera) context.getString(
                        R.string.is_camera , context.getString(R.string.yes)
                    )
                    else context.getString(R.string.is_camera , context.getString(R.string.no)) ,
                    modifier = Modifier.padding(top = 4.dp)
                )
                AnimatedText(
                    text = context.getString(
                        R.string.server_count , scanResult.serverCount.toString()
                    ) , modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (state.value.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                            .padding(top = 4.dp)
                            .wrapContentSize()
                )
            }
        }

        Column(modifier = Modifier
                .padding(all = 24.dp)
                .constrainAs(ref = fab) {
                    bottom.linkTo(anchor = parent.bottom)
                    end.linkTo(anchor = parent.end)
                }) {
            FloatingActionButton(onClick = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent().setClassName(
                        "com.android.phone" , "com.android.phone.settings.RadioInfo"
                    )
                }
                else {
                    Intent().setClassName(
                        "com.android.settings" , "com.android.settings.RadioInfo"
                    )
                }
                context.startActivity(intent)
            } , modifier = Modifier
                    .align(alignment = Alignment.End)
                    .bounceClick()) {
                Icon(
                    imageVector = Icons.Default.PermDeviceInformation ,
                    contentDescription = context.getString(R.string.content_description_radio)
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState , modifier = Modifier.align(alignment = Alignment.BottomCenter)
        )
    }
}

@Composable
fun AnimatedText(
    text : String , modifier : Modifier = Modifier , characterDurationMillis : Int = 50
) {
    val textToShow : MutableState<String> = remember { mutableStateOf(value = "") }
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    var job : Job? = remember { null }

    LaunchedEffect(key1 = text) {
        job?.cancel()
        textToShow.value = ""
        job = coroutineScope.launch {
            text.forEachIndexed { _ , char ->
                textToShow.value += char
                delay(timeMillis = characterDurationMillis.toLong())
            }
        }
    }

    Text(
        text = textToShow.value , modifier = modifier
    )
}