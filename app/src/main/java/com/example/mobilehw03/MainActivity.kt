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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.BackoffPolicy
import com.example.mobilehw03.ui.theme.MobileHW03Theme
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
import android.os.FileObserver
import androidx.compose.material3.Surface
import com.example.mobilehw03.network.NetworkStatus
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
                    writeInternetLogToFile(NetworkStatus.Available, this)
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.Available.toString()
                            startService(it)
                        })
                }
                NetworkStatus.UnAvailable->{
                    writeInternetLogToFile(NetworkStatus.UnAvailable, this)
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.UnAvailable.toString()
                            startService(it)
                        })
                }
                NetworkStatus.NotConnected->{
                    writeInternetLogToFile(NetworkStatus.NotConnected, this)
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.NotConnected.toString()
                            startService(it)
                        })
                }

                NetworkStatus.Unknown -> {
                    writeInternetLogToFile(NetworkStatus.Unknown, this)
                    sendBroadcast(Intent(applicationContext, ForegroundService::class.java)
                        .also {
                            it.action = ForegroundService.Actions.NotConnected.toString()
                            startService(it)
                        })
                }
            }
            MobileHW03Theme {
                Surface {
                    LaunchedEffect(key1 = Unit) {
                        val workRequest = PeriodicWorkRequestBuilder<StatusCheckWorker>(
                            repeatInterval = 1,
                            repeatIntervalTimeUnit =TimeUnit.SECONDS
                        ).setBackoffCriteria(
                            backoffPolicy = BackoffPolicy.LINEAR,
                            duration = Duration.ofSeconds(15)
                        ).build()

                        val workManager = WorkManager.getInstance(applicationContext)
                        workManager.enqueue(workRequest)
                    }
                    val logs = remember { mutableStateOf(readJsonFromFile(this, "logs.txt")) }

                    val observer = remember {
                        JsonFileObserver(this, "logs.txt") {
                            logs.value = readJsonFromFile(this, "logs.txt")
                        }
                    }

                    DisposableEffect(this) {
                        observer.startWatching()
                        onDispose {
                            observer.stopWatching()
                        }
                    }

                    LazyColumn() {
                        items(logs.value) { log ->
                            Text(text = log)
                            Divider()
                        }
                    }
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

class JsonFileObserver(
    private val context: Context,
    private val fileName: String,
    private val onFileChanged: () -> Unit
) : FileObserver(File(context.filesDir, fileName).absolutePath, CLOSE_WRITE) {

    override fun onEvent(event: Int, path: String?) {
        if (event == CLOSE_WRITE) {
            onFileChanged()
        }
    }
}

fun readJsonFromFile(context: Context, fileName: String): List<String> {
    val logFile = File(context.filesDir, "logs.txt")
    val logs = mutableListOf<String>()
    if (logFile.exists()) {
        logFile.useLines { lines ->
            lines.forEach { line ->
                val jsonObject = JSONObject(line)
                val timestamp = jsonObject.getString("timestamp")
                var logMessage = ""
                if (jsonObject.getString("type") == "worker"){
                    val bluetoothEnabled = jsonObject.getBoolean("bluetooth status is :")
                    val airplaneModeOn = jsonObject.getBoolean("airplane status is :")

                    logMessage =
                        "time: ${timestamp}\nBluetooth status is :${if (bluetoothEnabled) "Enabled" else "Disabled"},\nAirplane mode status is :" + " ${
                            if
                                    (airplaneModeOn) "On" else "Off"
                        }"
                }
                else if (jsonObject.getString("type") == "service"){
                    val internetStatus = jsonObject.getString("internet status is :")
                    logMessage =
                        "time: ${timestamp}\nInternet Status Is :${internetStatus}"
                }

                logs.add(logMessage)
            }
        }
    }

    return logs.reversed()
}

private fun writeInternetLogToFile(internetStatus: NetworkStatus, context: Context) {
    val logFile = File(context.filesDir, "logs.txt")
    val jsonObject = JSONObject()
    jsonObject.put("type", "service")
    jsonObject.put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
        Date(System.currentTimeMillis())
    ))
    jsonObject.put("internet status is :", internetStatus.toString())
    val jsonString = jsonObject.toString() + "\n"
    logFile.appendText(jsonString)
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
