package com.soma.merchant.ble

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelUuid // برای رفع Unresolved reference
import android.util.Log

class BlePeripheralService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("BlePeripheralService", "Peripheral ready")
    }
}
