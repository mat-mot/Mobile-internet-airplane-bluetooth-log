package com.example.mobilehw03.network

import android.content.Context
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.Executors
import java.net.InetAddress


private var isConnected = false


open class NetworkChangeReceiver: BroadcastReceiver() {

    var network_status by mutableStateOf(NetworkStatus.Unknown)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val connectivityManager =
                    context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                isConnected = networkInfo?.isConnected ?: false
                if (isConnected) {
                    val executor = Executors.newSingleThreadExecutor()
                    val future = executor.submit {
                        network_status = try {
                            val address = InetAddress.getByName("google.com")
                            val isReachable = address.isReachable(2000)
                            Log.i("Network State", "Internet accessible")
                            NetworkStatus.Available
                        } catch (e: Exception) {
                            Log.i("Network State", "Internet not accessible")
                            NetworkStatus.UnAvailable
                        } finally {
                            executor.shutdown()
                        }
                    }
                } else {
                    Log.i("Network State", "No network connection")
                    network_status = NetworkStatus.NotConnected
                }
            }
        }
    }
}


enum class NetworkStatus(){
    Available, UnAvailable, NotConnected, Unknown
}