package com.soma.consumer.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.util.*

/**
 * یک کلاینت بسیار ساده BLE که:
 * - با فیلتر UUID اسکن می‌کند
 * - به Merchant متصل می‌شود
 * - سرویس را کشف می‌کند
 * - وضعیت را از طریق callback برمی‌گرداند
 */
class BleClient(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onStatus(msg: String)
        fun onConnected(deviceName: String?)
        fun onDisconnected()
        fun onError(msg: String)
    }

    companion object {
        // UUID مشترک بین دو اپ (در Merchant هم همین‌ها را گذاشته‌ایم)
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

        private const val SCAN_TIMEOUT_MS = 15_000L
    }

    private val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bm.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null
    private val main = Handler(Looper.getMainLooper())

    fun isBleReady(): Boolean = adapter?.isEnabled == true

    fun stop() {
        try { scanner?.stopScan(scanCallback) } catch (_: Throwable) {}
        scanCallback = null
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        listener.onStatus("متوقف شد")
    }

    fun start() {
        if (!isBleReady()) {
            listener.onError("Bluetooth روشن نیست")
            return
        }
        if (!hasScanPerms()) {
            listener.onError("مجوزهای BLE داده نشده")
            return
        }
        listener.onStatus("در حال اسکن…")
        startScanInternal()
    }

    private fun hasScanPerms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // برای اندروید‌های قدیمی‌تر موقع اجرا Location لازم است (Manifest هم باید داشته باشیم)
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanInternal() {
        scanner = adapter.bluetoothLeScanner
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val timeoutRunnable = Runnable {
            try { scanner?.stopScan(scanCallback) } catch (_: Throwable) {}
            listener.onError("چیزی پیدا نشد (Timeout)")
        }
        main.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                main.removeCallbacks(timeoutRunnable)
                try { scanner?.stopScan(this) } catch (_: Throwable) {}
                listener.onStatus("یافت شد: ${result.device?.name ?: result.device?.address}")
                connect(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                listener.onError("Scan failed: $errorCode")
            }
        }

        scanner?.startScan(filters, settings, scanCallback!!)
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        listener.onStatus("در حال اتصال…")
        gatt = if (Build.VERSION.SDK_INT >= 31) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                main.post {
                    listener.onConnected(gatt.device.name)
                    listener.onStatus("اتصال برقرار شد. کشف سرویس…")
                }
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                main.post {
                    listener.onDisconnected()
                    listener.onStatus("قطع اتصال")
                }
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                main.post { listener.onError("سرویس موردنظر پیدا نشد") }
                return
            }
            main.post { listener.onStatus("سرویس کشف شد ✓") }
        }
    }
}
