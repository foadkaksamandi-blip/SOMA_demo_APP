package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper

class BleClient(
    private val context: Context,
    private val onDeviceFound: (BluetoothDevice) -> Unit
) {

    private var adapter: BluetoothAdapter? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    private val scanCallback = @SuppressLint("MissingPermission")
    object : android.bluetooth.le.ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            result.device?.let { onDeviceFound(it) }
        }
    }

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = manager.adapter
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (scanning) return
        adapter?.bluetoothLeScanner?.startScan(scanCallback)
        scanning = true
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!scanning) return
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanning = false
    }
}
