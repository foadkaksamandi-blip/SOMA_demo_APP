package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper

class BleClient(
    private val context: Context,
    private val onFound: (BluetoothDevice) -> Unit
) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val scanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    // کال‌بک اسکن
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            onFound(result.device)
        }
    }

    fun startScan() {
        if (scanning) return
        scanning = true
        scanner?.startScan(scanCallback)

        // توقف خودکار پس از ۱۰ ثانیه
        handler.postDelayed({ stopScan() }, 10_000)
    }

    fun stopScan() {
        if (!scanning) return
        scanning = false
        scanner?.stopScan(scanCallback)
    }
}
