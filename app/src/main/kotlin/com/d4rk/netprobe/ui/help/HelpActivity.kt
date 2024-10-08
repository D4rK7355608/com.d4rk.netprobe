package com.d4rk.netprobe.ui.help

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.d4rk.netprobe.ui.settings.display.theme.style.AppTheme
import com.d4rk.netprobe.utils.IntentUtils
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

class HelpActivity : AppCompatActivity() {
    private val viewModel: HelpViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background
                ) {
                    HelpComposable(this@HelpActivity, viewModel)
                }
            }
        }
    }
}