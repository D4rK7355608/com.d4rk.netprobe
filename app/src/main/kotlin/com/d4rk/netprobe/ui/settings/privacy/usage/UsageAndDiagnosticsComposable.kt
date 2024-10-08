package com.d4rk.netprobe.ui.settings.privacy.usage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.datastore.DataStore
import com.d4rk.netprobe.utils.compose.components.SwitchCardComposable
import com.d4rk.netprobe.utils.IntentUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageAndDiagnosticsComposable(activity : UsageAndDiagnosticsActivity) {
    val context = LocalContext.current
    val dataStore = DataStore.getInstance(context)
    val switchState = dataStore.usageAndDiagnostics.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) , topBar = {
        LargeTopAppBar(title = { Text(stringResource(R.string.usage_and_diagnostics)) } ,
                       navigationIcon = {
                           IconButton(onClick = {
                               activity.finish()
                           }) {
                               Icon(
                                   Icons.AutoMirrored.Filled.ArrowBack , contentDescription = null
                               )
                           }
                       } ,
                       scrollBehavior = scrollBehavior)
    }) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding) ,
            ) {
                item {
                    SwitchCardComposable(
                        title = stringResource(R.string.usage_and_diagnostics) ,
                        switchState = switchState
                    ) { isChecked ->
                        scope.launch(Dispatchers.IO) {
                            dataStore.saveUsageAndDiagnostics(isChecked)
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Info , contentDescription = null)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.summary_usage_and_diagnostics))
                        val annotatedString = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary ,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(stringResource(R.string.learn_more))
                            }
                            addStringAnnotation(
                                tag = "URL" ,
                                annotation = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy" ,
                                start = 0 ,
                                end = stringResource(R.string.learn_more).length
                            )
                        }
                        ClickableText(text = annotatedString , onClick = { offset ->
                            annotatedString.getStringAnnotations("URL" , offset , offset)
                                    .firstOrNull()?.let { annotation ->
                                        IntentUtils.openUrl(context , annotation.item)
                                    }
                        })
                    }
                }
            }
        }
    }
}