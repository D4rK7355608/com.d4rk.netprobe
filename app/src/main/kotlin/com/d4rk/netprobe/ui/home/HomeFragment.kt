package com.d4rk.netprobe.ui.home
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.d4rk.netprobe.R
import com.d4rk.netprobe.databinding.FragmentHomeBinding
import com.d4rk.netprobe.ui.viewmodel.ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.Socket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Enumeration
class HomeFragment : Fragment() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: FragmentHomeBinding
    private var serverCount = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[ViewModel::class.java]
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        MobileAds.initialize(requireContext())
        binding.adView.loadAd(AdRequest.Builder().build())
        binding.editTextIp.setText(R.string.default_ip_address)
        val ipAddress = getIPAddress()
        binding.textViewIp.text = String.format(resources.getString(R.string.ip_address_result), "")
        binding.textViewIp.isEnabled = false
        binding.textViewResponseCode.text = String.format(resources.getString(R.string.response_code), "")
        binding.textViewResponseCode.isEnabled = false
        binding.textViewFunction.text = String.format(resources.getString(R.string.function), "")
        binding.textViewFunction.isEnabled = false
        binding.textViewResponseTime.text = String.format(resources.getString(R.string.response), "")
        binding.textViewResponseTime.isEnabled = false
        binding.textViewIsCamera.text = String.format(resources.getString(R.string.is_camera), "")
        binding.textViewIsCamera.isEnabled = false
        binding.textViewServerCount.text = String.format(resources.getString(R.string.server_count), "")
        binding.textViewServerCount.isEnabled = false
        binding.ipAddressTextView.text = String.format(resources.getString(R.string.your_ip), ipAddress)
        binding.ipAddressTextView.setOnLongClickListener {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData: ClipData = ClipData.newPlainText("Label",ipAddress)
            clipboardManager.setPrimaryClip(clipData)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            true
        }
        binding.floatingActionButtonShortcut.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent().setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
            } else {
                Intent().setClassName("com.android.settings", "com.android.settings.RadioInfo")
            }
            startActivity(intent)
        }
        binding.buttonScan.setOnClickListener {
            val ip = binding.editTextIp.text.toString()
            if (ip.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.snack_enter_ip), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progressBar.isVisible = true
            lifecycleScope.launch(Dispatchers.IO) {
                val responseCode: Int
                val ipFunctions = mutableMapOf<String, String>()
                val isCamera: Boolean
                val responseTime: Long
                try {
                    val startTime = System.currentTimeMillis()
                    val socket = Socket()
                    val inetAddress = InetAddress.getByName(ip)
                    if (inetAddress.isLoopbackAddress || inetAddress.hostAddress == ip || inetAddress.hostAddress == "192.168.1.100") {
                        throw IllegalArgumentException("Invalid IP address")
                    }
                    socket.connect(InetSocketAddress(inetAddress, 80), 5000)
                    responseTime = System.currentTimeMillis() - startTime
                    if (socket.isConnected) {
                        responseCode = socket.getInputStream().read()
                        isCamera = withContext(Dispatchers.IO) {
                            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
                            val response = StringBuilder()
                            var line: String? = reader.readLine()
                            while (line != null) {
                                response.append(line)
                                line = reader.readLine()
                            }
                            response.contains("camera", ignoreCase = true)
                        }
                        ipFunctions[ip] = when (responseCode) {
                            200 -> getString(R.string.web_page)
                            22 -> getString(R.string.server)
                            169 -> getString(R.string.pc)
                            else -> getString(R.string.server)
                        }
                        if (isCamera) {
                            serverCount++
                        }
                        withContext(Dispatchers.Main) {
                            binding.textViewIp.isEnabled = true
                            binding.textViewResponseCode.isEnabled = true
                            binding.textViewFunction.isEnabled = true
                            binding.textViewResponseTime.isEnabled = true
                            binding.textViewIsCamera.isEnabled = true
                            binding.textViewServerCount.isEnabled = true
                            binding.textViewIp.text = String.format(resources.getString(R.string.ip_address_result), ip)
                            binding.textViewResponseCode.text = String.format(resources.getString(R.string.response_code), responseCode.toString())
                            binding.textViewFunction.text = String.format(resources.getString(R.string.function), ipFunctions[ip] ?: getString(R.string.unknown))
                            binding.textViewResponseTime.text = String.format(resources.getString(R.string.response), "$responseTime ms")
                            binding.textViewIsCamera.text = if (isCamera) String.format(resources.getString(R.string.is_camera), getString(R.string.yes)) else String.format(resources.getString(R.string.is_camera), getString(R.string.no))
                            binding.textViewServerCount.text = String.format(resources.getString(R.string.server_count), serverCount.toString())
                            binding.progressBar.isVisible = false
                        }
                    }
                    socket.close()
                } catch (e: UnknownHostException) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, getString(R.string.snack_invalid_ip_address), Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }
                } catch (e: IllegalArgumentException) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, getString(R.string.snack_invalid_ip_address), Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }
                } catch (e: SocketTimeoutException) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, getString(R.string.snack_connection_timeout), Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, getString(R.string.snack_connection_error), Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }
        return binding.root
    }
    private fun getIPAddress(): String? {
        val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface: NetworkInterface = interfaces.nextElement()
            val addresses: Enumeration<InetAddress> = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address: InetAddress = addresses.nextElement()
                if (!address.isLinkLocalAddress && !address.isLoopbackAddress && address is Inet4Address) {
                    return address.hostAddress
                }
            }
        }
        return getString(R.string.ip_not_fount)
    }
}