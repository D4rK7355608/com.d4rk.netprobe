package com.d4rk.netprobe.ui.startup
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.netprobe.MainActivity
import com.d4rk.netprobe.databinding.ActivityStartupBinding
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import me.zhanghai.android.fastscroll.FastScrollerBuilder
class StartupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartupBinding
    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            if (consentInformation.isConsentFormAvailable) {
                loadForm()
            }
        }, {
        })
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        binding.buttonBrowsePrivacyPolicyAndTermsOfService.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy")))
        }
        binding.floatingButtonAgree.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val isReadPhoneStateGranted = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            if (!isReadPhoneStateGranted) {
                requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
                return
            }
        }
    }
    private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(
            this,
            { consentForm ->
                this.consentForm = consentForm
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(this) {
                        loadForm()
                    }
                }
            },
            {
            }
        )
    }
}