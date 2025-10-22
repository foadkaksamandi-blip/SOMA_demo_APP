package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper

@SuppressLint("MissingPermission")
class BleClient(
    private val context: Context,
    private val onFound: (BluetoothDevice) -> Unit
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val handler = Handler(Looper.getMainLooper())

    private var isScanning = false

    fun startScan() {
        if (isScanning) return
        bluetoothAdapter?.bluetoothLeScanner?.startScan { callbackType, result ->
            result.device?.let { onFound(it) }
        }
        isScanning = true
        handler.postDelayed({ stopScan() }, 10000) // اسکن ۱۰ ثانیه‌ای
    }

    fun stopScan() {
        if (!isScanning) return
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(null)
        isScanning = false
    }
}
