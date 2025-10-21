package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleClient(private val context: Context) {

    companion object {
        // همان UUID سرویس که بین دو اپ مشترک است
        // اگر جای دیگری مقداردهی کرده‌ای، این را با همان مقدار یکی کن
        val SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        private const val TAG = "BleClient"
    }

    private val btManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val btAdapter: BluetoothAdapter? = btManager?.adapter
    private val scanner: BluetoothLeScanner? = btAdapter?.bluetoothLeScanner

    private var scanning = false
    private var externalListener: ((ScanResult) -> Unit)? = null

    fun startScan(onFound: (ScanResult) -> Unit) {
        externalListener = onFound
        if (scanning) return
        if (btAdapter == null || !btAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth adapter off or null")
            return
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID.toString()))
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(
                if (Build.VERSION.SDK_INT >= 23)
                    ScanSettings.SCAN_MODE_LOW_LATENCY
                else
                    ScanSettings.SCAN_MODE_LOW_POWER
            )
            .build()

        scanner?.startScan(filters, settings, callback)
        scanning = true
        Log.d(TAG, "startScan() called")
    }

    fun stopScan() {
        if (!scanning) return
        scanner?.stopScan(callback)
        scanning = false
        Log.d(TAG, "stopScan() called")
    }

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // فقط نتایجی که سرویس ما را دارند پاس بده
            val uuids = result.scanRecord?.serviceUuids
            if (uuids?.any { it.uuid == SERVICE_UUID } == true) {
                externalListener?.invoke(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { onScanResult(0, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
        }
    }
}
