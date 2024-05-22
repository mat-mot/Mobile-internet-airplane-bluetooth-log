package com.example.mobilehw03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.BackoffPolicy
import com.example.mobilehw03.ui.theme.MobileHW03Theme
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobilehw03.cWorker.StatusCheckWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
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