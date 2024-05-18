package com.example.mobilehw03.network

import android.content.Context
import android.net.ConnectivityManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import com.example.mobilehw03.MainActivity
import java.util.concurrent.Executors
import java.net.InetAddress


private var isConnected = false


open class NetworkChangeReceiver: BroadcastReceiver() {

    //    var network_status = NetworkStatus.NotConnected
//    var network_status: NetworkStatus = NetworkStatus.Unknown
//    var network_status: MutableLiveData<NetworkStatus> = MutableLiveData(NetworkStatus.Unknown)
    var network_status by mutableStateOf(NetworkStatus.Unknown)
    // Define a callback interface to notify listeners (MainActivity) about network changes
//    interface NetworkStatusListener {
//        fun onNetworkStatusChange(newStatus: MutableLiveData<NetworkStatus>)
//    }
//
//    private var listener: NetworkStatusListener? = null // Holder for the listener
//
//    fun setNetworkStatusListener(listener: MainActivity) {
//        this.listener = listener
//    }

//    var onNetworkStatusChange: ((NetworkStatus) -> Unit)? = null


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
                    //                            network_status = MutableLiveData(NetworkStatus.Available)
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
//                listener?.onNetworkStatusChange(network_status)
            }
        }
    }

    interface NetworkServiceListener {
        fun onNetworkStatusChange(networkStatus: NetworkStatus)
    }




//    fun getNetworkStatus(): NetworkStatus {
//        return this.network_status
//    }

}

//class NetworkService : Service() {
//
//    // ... service implementation
//
//    private val listeners = mutableSetOf<NetworkChangeReceiver.NetworkServiceListener>()
//
//    fun addNetworkStatusListener(listener: MainActivity) {
//        listeners.add(listener)
//    }
//
//    fun removeNetworkStatusListener(listener: NetworkChangeReceiver.NetworkServiceListener) {
//        listeners.remove(listener)
//    }
//
//    private fun notifyNetworkStatusChange(networkStatus: NetworkStatus) {
//        listeners.forEach { it.onNetworkStatusChange(networkStatus) }
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    // ... service methods that determine network status and send broadcasts
//}

enum class NetworkStatus(){
    Available, UnAvailable, NotConnected, Unknown
}