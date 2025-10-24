package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import java.util.UUID

class BleClient(private val context: Context) {

    companion object {
        // همان UUID سرویس که Merchant تبلیغ می‌کند
        private val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        private const val TIMEOUT_MS = 15_000L
    }

    private val btMgr: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val adapter: BluetoothAdapter? get() = btMgr?.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutPosted = false

    @SuppressLint("MissingPermission")
    fun startScan(
        onFound: (String) -> Unit,
        onStop: () -> Unit = {}
    ) {
        val ad = adapter ?: run {
            onFound("Bluetooth adapter not available")
            onStop()
            return
        }

        if (!ad.isEnabled) {
            onFound("Bluetooth adapter is disabled")
            onStop()
            return
        }

        scanner = ad.bluetoothLeScanner ?: run {
            onFound("BLE scanner not available")
            onStop()
            return
        }

        // فیلتر بر اساس سرویس فروشنده
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device?.name ?: "Merchant"
                onFound(name)
            }
        }

        scanner?.startScan(filters, settings, scanCallback)

        if (!timeoutPosted) {
            timeoutPosted = true
            handler.postDelayed({
                stopScan()
                onStop()
                timeoutPosted = false
            }, TIMEOUT_MS)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanCallback?.let { cb ->
            scanner?.stopScan(cb)
        }
        scanCallback = null
        scanner = null
        timeoutPosted = false
    }
}
