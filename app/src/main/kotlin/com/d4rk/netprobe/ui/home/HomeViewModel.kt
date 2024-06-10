package com.d4rk.netprobe.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.db.table.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

import java.util.Enumeration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application : Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _ipAddress = MutableStateFlow("")
    val ipAddress : StateFlow<String> = _ipAddress.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent : SharedFlow<String> = _snackbarEvent.asSharedFlow()


    init {
        _ipAddress.value = getIPAddress() ?: context.getString(R.string.ip_not_found)
    }

    private var serverCount = 0

    fun scanIpAddress(ipAddress : String , onResult : (ScanResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val responseCode : String
            val ipFunctions = mutableMapOf<String , String>()
            val isCamera : Boolean
            val responseTime : Long
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                val inetAddress = InetAddress.getByName(ipAddress)
                if (inetAddress.isLoopbackAddress || inetAddress.hostAddress == ipAddress || inetAddress.hostAddress == "192.168.1.100") {
                    throw IllegalArgumentException("Invalid IP address")
                }
                socket.connect(InetSocketAddress(inetAddress , 80) , 5000)
                responseTime = System.currentTimeMillis() - startTime
                if (socket.isConnected) {
                    responseCode = socket.getInputStream().read().toString()
                    isCamera = withContext(Dispatchers.IO) {
                        val reader =
                                BufferedReader(InputStreamReader(socket.getInputStream() , "UTF-8"))
                        val response = StringBuilder()
                        var line : String? = reader.readLine()
                        while (line != null) {
                            response.append(line)
                            line = reader.readLine()
                        }
                        response.contains("camera" , ignoreCase = true)
                    }
                    ipFunctions[ipAddress] = when (responseCode.toInt()) {
                        200 -> context.getString(R.string.web_page)
                        22 -> context.getString(R.string.server)
                        169 -> context.getString(R.string.pc)
                        else -> context.getString(R.string.server)
                    }
                    if (isCamera) {
                        serverCount ++
                    }
                    withContext(Dispatchers.Main) {
                        onResult(
                            ScanResult(
                                ipAddress ,
                                responseCode ,
                                ipFunctions[ipAddress] ?: context.getString(R.string.unknown) ,
                                responseTime ,
                                isCamera ,
                                serverCount
                            )
                        )
                    }
                }
                socket.close()
            } catch (e : UnknownHostException) {
                _snackbarEvent.emit(context.getString(R.string.snack_invalid_ip_address))
            } catch (e : IllegalArgumentException) {
                _snackbarEvent.emit(context.getString(R.string.snack_invalid_ip_address))
            } catch (e : SocketTimeoutException) {
                _snackbarEvent.emit(context.getString(R.string.snack_connection_timeout))
            } catch (e : IOException) {
                _snackbarEvent.emit(context.getString(R.string.snack_connection_error))
            }
        }
    }

    private fun getIPAddress() : String? {
        val interfaces : Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface : NetworkInterface = interfaces.nextElement()
            val addresses : Enumeration<InetAddress> = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address : InetAddress = addresses.nextElement()
                if (! address.isLinkLocalAddress && ! address.isLoopbackAddress && address is Inet4Address) {
                    return address.hostAddress
                }
            }
        }
        return null
    }
}