package com.example.mobilehw03.network

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mobilehw03.R

class ForegroundService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.Available.toString() -> available();
            Actions.UnAvailable.toString() -> unAvailable();
            Actions.NotConnected.toString() -> notConnected();
            Actions.Unknown.toString() -> unknown();
            Actions.STOP.toString() -> stop();
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ForegroundServiceType")
    private fun available() {
        val notification = NotificationCompat.Builder(this, "Network Status")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Network Status")
            .setContentText("Network Status is ${NetworkStatus.Available}")
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    @SuppressLint("ForegroundServiceType")
    private fun unAvailable() {
        val notification = NotificationCompat.Builder(this, "Network Status")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Network Status")
            .setContentText("Network Status is ${NetworkStatus.UnAvailable}")
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    @SuppressLint("ForegroundServiceType")
    private fun notConnected() {
        val notification = NotificationCompat.Builder(this, "Network Status")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Network Status")
            .setContentText("Network Status is ${NetworkStatus.NotConnected}")
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    @SuppressLint("ForegroundServiceType")
    private fun unknown() {
        val notification = NotificationCompat.Builder(this, "Network Status")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Network Status")
            .setContentText("Network Status is ${NetworkStatus.Unknown}")
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }
    private fun stop() {
        stopSelf()
    }

    enum class Actions {
        Available, UnAvailable, NotConnected, STOP, Unknown
    }
}