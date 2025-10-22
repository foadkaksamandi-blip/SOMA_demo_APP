package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class BleClient(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var scanning = false

    fun startScan(onFound: (String) -> Unit, onStop: () -> Unit) {
        if (bluetoothAdapter == null) {
            Log.e("BLE", "Bluetooth not supported")
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return
        scanning = true
        scanner.startScan(callback(onFound))
        Log.d("BLE", "Scanning started")
    }

    fun stopScan() {
        if (bluetoothAdapter == null) return
        bluetoothAdapter.bluetoothLeScanner?.stopScan(callback { })
        scanning = false
        Log.d("BLE", "Scanning stopped")
    }

    private fun callback(onFound: (String) -> Unit): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device: BluetoothDevice? = result?.device
                device?.name?.let { onFound(it) }
            }
        }
    }
}
