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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.BackoffPolicy
import com.example.mobilehw03.ui.theme.MobileHW03Theme
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobilehw03.cWorker.StatusCheckWorker
import java.time.Duration
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.mobilehw03.network.ForegroundService
import com.example.mobilehw03.network.NetworkChangeReceiver
import android.net.ConnectivityManager
import com.example.mobilehw03.network.NetworkStatus


class MainActivity : ComponentActivity() {
    private val networkChangeReceiver = NetworkChangeReceiver()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        } else {
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
                LaunchedEffect(key1 = Unit) {
                    val workRequest = PeriodicWorkRequestBuilder<StatusCheckWorker>(
                        repeatInterval = 2,
                        repeatIntervalTimeUnit = TimeUnit.MINUTES, 1, TimeUnit.SECONDS
                    ).setBackoffCriteria(
                        backoffPolicy = BackoffPolicy.LINEAR,
                        duration = Duration.ofSeconds(15)
                    ).build()

                    val workManager = WorkManager.getInstance(applicationContext)
                    workManager.enqueue(workRequest)
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
