package com.example.mobilehw03

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobilehw03.ui.theme.MobileHW03Theme
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.mobilehw03.network.ForegroundService
import com.example.mobilehw03.network.NetworkChangeReceiver
//import com.example.mobilehw03.network.NetworkService
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import com.example.mobilehw03.network.NetworkStatus


class MainActivity : ComponentActivity() {
//    private var network_Status by mutableStateOf(NetworkChangeReceiver().network_status)
    private val networkChangeReceiver = NetworkChangeReceiver()
//    private var _networkStatus = MutableLiveData<NetworkStatus>(NetworkStatus.Unknown) // Use MutableLiveData for live updates
//    val networkStatus: LiveData<NetworkStatus> = _networkStatus // Expose networkStatus for binding
//    private val networkService: NetworkService? = null
//    var networkStatus by remember { mutableStateOf(NetworkStatus.Unknown) }

//    override fun onNetworkStatusChange(newStatus: MutableLiveData<NetworkStatus>) {
//        // Update networkStatus LiveData and UI based on newStatus
//        _networkStatus.value = newStatus
//    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else {
            // Permission is already granted, create the notification channel
            createNotificationChannel()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        registerReceiver(
            networkChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
//        networkChangeReceiver.setNetworkStatusListener(this)
//        networkChangeReceiver.onNetworkStatusChange = { newStatus ->
//            _networkStatus.value = newStatus
//        }

//        networkService?.addNetworkStatusListener(this)

        createNotificationChannel()

        setContent {
            when(networkChangeReceiver.network_status){
                NetworkStatus.Available->{
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.Available.toString()
                            startService(it)
                        })
                }
                NetworkStatus.UnAvailable->{
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.UnAvailable.toString()
                            startService(it)
                        })
                }
                NetworkStatus.NotConnected->{
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.NotConnected.toString()
                            startService(it)
                        })
                }

                NetworkStatus.Unknown -> {
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.NotConnected.toString()
                            startService(it)
                        })
                }
            }
            MobileHW03Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
//                    val networkStatusText = networkChangeReceiver.network_status.value?.name ?: "Unknown"
                    Text(text = "Network Status Is : ${networkChangeReceiver.network_status}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "Network Status"
        val channelName = "Network Status"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    override fun onDestroy() {
        sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
            .also {
                it.action = ForegroundService.Actions.STOP.toString()
                startService(it)})
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobileHW03Theme {
        Greeting("Android")
    }
}
