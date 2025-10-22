package com.soma.merchant.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat

/**
 * نسخه‌ی تستی BLE Peripheral Service برای دمو پروژه SOMA
 * فقط جهت Build موفق و شبیه‌سازی سرویس فورگراند BLE
 */
class BlePeripheralService : Service() {

    private val btManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val btAdapter by lazy { btManager.adapter }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        simulatePeripheralAdvertising()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "BLE_CHANNEL"
        val channelName = "BLE Foreground Service"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOMA BLE Service")
            .setContentText("Broadcasting BLE data…")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()

        startForeground(1, notification)
    }

    private fun simulatePeripheralAdvertising() {
        // در نسخه دمو فقط پیام لاگ فرضی ارسال می‌شود
        if (btAdapter?.isEnabled == true) {
            println("🟢 BLE Peripheral active and advertising (demo mode)")
        } else {
            println("🔴 Bluetooth is disabled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("🔵 BLE Peripheral Service stopped")
    }
}
