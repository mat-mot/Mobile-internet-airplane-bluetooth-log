package com.example.mobilehw03.cWorker

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.provider.Settings
import androidx.work.WorkerParameters
import android.util.Log
import androidx.work.Worker

class StatusCheckWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        const val TAG_AIRPLANE_WORKER = "worker_airplane"
        const val TAG_BLUETOOTH_WORKER = "worker_bluetooth"
    }
    override fun doWork(): Result {
        val bluetoothStatus = checkBluetoothStatus()
        val airplaneModeStatus = checkAirplaneModeStatus()

        Log.i(TAG_BLUETOOTH_WORKER, "Bluetooth is ${if (bluetoothStatus) "ON" else "OFF"}")
        Log.i(TAG_AIRPLANE_WORKER, "Airplane mode is ${if (airplaneModeStatus) "ON" else "OFF"}")
        return Result.success()
    }

    private fun checkBluetoothStatus(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    private fun checkAirplaneModeStatus(): Boolean {
        return Settings.Global.getInt(
            applicationContext.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}