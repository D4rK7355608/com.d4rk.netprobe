package com.d4rk.netprobe.ui.home


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d4rk.netprobe.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun HomeComposable(
    viewModel : HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collectLatest { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message , duration = SnackbarDuration.Short
                )
            }
        }
    }

    val ipAddress = viewModel.ipAddress.collectAsState()
    var editTextIp by remember { mutableStateOf("") }
    var textViewIp by remember {
        mutableStateOf(
            context.getString(
                R.string.ip_address_result , ""
            )
        )
    }
    var textViewResponseCode by remember {
        mutableStateOf(
            context.getString(
                R.string.response_code , ""
            )
        )
    }
    var textViewFunction by remember { mutableStateOf(context.getString(R.string.function , "")) }
    var textViewResponseTime by remember {
        mutableStateOf(
            context.getString(
                R.string.response , ""
            )
        )
    }
    var textViewIsCamera by remember { mutableStateOf(context.getString(R.string.is_camera , "")) }
    var textViewServerCount by remember {
        mutableStateOf(
            context.getString(
                R.string.server_count , ""
            )
        )
    }
    var progressBarVisibility by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (scrollView , fab) = createRefs()

        Column(modifier = Modifier
                .fillMaxSize()
                .constrainAs(scrollView) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(24.dp)) {
            OutlinedTextField(value = editTextIp ,
                              onValueChange = { editTextIp = it } ,
                              label = { Text(text = context.getString(R.string.ip_address)) } ,
                              keyboardOptions = KeyboardOptions(
                                  keyboardType = KeyboardType.Text , imeAction = ImeAction.Next
                              ) ,
                              modifier = Modifier.fillMaxWidth() ,
                              singleLine = true)
            Text(text = context.getString(R.string.your_ip , ipAddress.value) ,
                 modifier = Modifier
                         .padding(top = 4.dp)
                         .clickable {
                             val clipboardManager =
                                     getSystemService(context , ClipboardManager::class.java)
                             val clipData = ClipData.newPlainText("Label" , ipAddress.value)
                             clipboardManager?.setPrimaryClip(clipData)
                             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) Toast
                                     .makeText(
                                         context , R.string.copied_to_clipboard , Toast.LENGTH_SHORT
                                     )
                                     .show()
                         })
            Button(onClick = {
                if (editTextIp.isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.snack_enter_ip) ,
                            duration = SnackbarDuration.Short ,
                            withDismissAction = true
                        )
                    }
                    return@Button
                }
                progressBarVisibility = true
                viewModel.scanIpAddress(editTextIp) {
                    textViewIp = context.getString(R.string.ip_address_result , it.ipAddress)
                    textViewResponseCode =
                            context.getString(R.string.response_code , it.responseCode)
                    textViewFunction = context.getString(R.string.function , it.function)
                    textViewResponseTime =
                            context.getString(R.string.response , "${it.responseTime} ms")
                    textViewIsCamera = if (it.isCamera) context.getString(
                        R.string.is_camera , context.getString(R.string.yes)
                    )
                    else context.getString(R.string.is_camera , context.getString(R.string.no))
                    textViewServerCount =
                            context.getString(R.string.server_count , it.serverCount.toString())
                    progressBarVisibility = false
                }
            } , modifier = Modifier
                    .padding(top = 4.dp)
                    .wrapContentSize()) {
                Text(text = context.getString(R.string.scan))
            }
            Text(
                text = textViewIp , modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = textViewResponseCode , modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = textViewFunction , modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = textViewResponseTime , modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = textViewIsCamera , modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = textViewServerCount , modifier = Modifier.padding(top = 4.dp)
            )
            if (progressBarVisibility) {
                CircularProgressIndicator(
                    modifier = Modifier
                            .padding(top = 4.dp)
                            .wrapContentSize()
                )
            }

            // TODO: Add ad view
        }

        SnackbarHost(hostState = snackbarHostState)

        FloatingActionButton(onClick = {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent().setClassName(
                    "com.android.phone" , "com.android.phone.settings.RadioInfo"
                )
            }
            else {
                Intent().setClassName("com.android.settings" , "com.android.settings.RadioInfo")
            }
            context.startActivity(intent)
        } , modifier = Modifier
                .padding(24.dp)
                .constrainAs(fab) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }) {
            Icon(
                imageVector = Icons.Default.PermDeviceInformation ,
                contentDescription = context.getString(R.string.content_description_radio)
            )
        }
    }
}