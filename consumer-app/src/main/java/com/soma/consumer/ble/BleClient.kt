package com.soma.consumer.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * کلاینت BLE بسیار مینیمال برای دمو.
 * امضا دقیقاً مطابق MainActivity:
 *   startScan(onFound = { device -> ... }, onStop = { ... })
 *   stopScan()
 */
class BleClient(private val context: Context) {

    private val btAdapter: BluetoothAdapter? by lazy {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mgr.adapter
    }
    private val scanner: BluetoothLeScanner? get() = btAdapter?.bluetoothLeScanner

    private var onStopCb: (() -> Unit)? = null
    private var running = false

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // به محض مشاهده اولین دیوایس، همان را گزارش بدهیم (برای دمو کافی است)
            onFoundCb?.invoke(result.device)
        }
    }

    private var onFoundCb: ((android.bluetooth.BluetoothDevice) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startScan(
        onFound: (android.bluetooth.BluetoothDevice) -> Unit,
        onStop: () -> Unit
    ) {
        onFoundCb = onFound
        onStopCb = onStop

        if (!hasScanPermission()) {
            // اجازه‌ها باید بیرون از این کلاس گرفته شود.
            onStopCb?.invoke()
            return
        }
        if (btAdapter == null || !btAdapter!!.isEnabled) {
            onStopCb?.invoke()
            return
        }
        if (running) return
        running = true
        scanner?.startScan(callback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!running) return
        scanner?.stopScan(callback)
        running = false
        onStopCb?.invoke()
    }

    private fun hasScanPermission(): Boolean {
        val need = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return need.all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }
    }
}
