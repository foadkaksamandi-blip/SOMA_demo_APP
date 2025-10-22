package com.soma.consumer.ble

import android.content.Context
import android.os.ParcelUuid // برای رفع ارور Unresolved reference
import android.util.Log

class BleClient(private val context: Context) {

    fun start(
        onConnected: () -> Unit = {},
        onDisconnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Log.d("BleClient", "Start scanning BLE devices")
        onConnected.invoke()
    }

    fun stop() {
        Log.d("BleClient", "Stop scanning BLE devices")
    }
}
