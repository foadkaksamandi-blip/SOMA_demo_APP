package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

/**
 * اسکن BLE برای یک Service مشخص، اتصال، و خواندن یک Characteristic متنی
 *
 * API سطح استفاده شده: BluetoothAdapter/BluetoothLeScanner/BluetoothGatt
 * امضاها مطابق MainActivity:
 *  - startScan(onFound: (String) -> Unit, onStop: () -> Unit)
 *  - stopScan()
 */
class BLEClient(private val context: Context) {

    companion object {
        // این‌ها را در صورت نیاز با UUIDهای واقعی عوض کن
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

        private const val TAG = "BLEClient"
        private const val TIMEOUT_MS = 15_000L
    }

    private val btMgr: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? get() = btMgr.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    private var gatt: BluetoothGatt? = null

    private val main = Handler(Looper.getMainLooper())
    private var timeoutPosted = false

    private var onFoundCb: ((String) -> Unit)? = null
    private var onStopCb: (() -> Unit)? = null

    // ----------------- Public API -----------------

    @SuppressLint("MissingPermission")
    fun startScan(onFound: (String) -> Unit, onStop: () -> Unit) {
        onFoundCb = onFound
        onStopCb = onStop

        val ad = adapter ?: run {
            onFoundCb?.invoke("Bluetooth adapter not available")
            onStopInternal()
            return
        }
        if (!ad.isEnabled) {
            onFoundCb?.invoke("Bluetooth is disabled")
            onStopInternal()
            return
        }

        scanner = ad.bluetoothLeScanner ?: run {
            onFoundCb?.invoke("BLE scanner not available")
            onStopInternal()
            return
        }

        // فیلتر روی سرویس مورد نظر
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val dev = result.device
                val name = dev.name ?: "بدون‌نام"
                val addr = dev.address ?: "?"
                onFoundCb?.invoke("دستگاه: $name | $addr")
                // به محض اولین نتیجه متصل شو
                stopOnlyScan()
                connect(dev)
            }

            override fun onScanFailed(errorCode: Int) {
                onFoundCb?.invoke("Scan failed: $errorCode")
                onStopInternal()
            }
        }

        scanner?.startScan(filters, settings, scanCallback)
        onFoundCb?.invoke("اسکن شروع شد...")

        if (!timeoutPosted) {
            timeoutPosted = true
            main.postDelayed({
                timeoutPosted = false
                onFoundCb?.invoke("اسکن به زمان‌بندی رسید")
                onStopInternal()
            }, TIMEOUT_MS)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        onFoundCb?.invoke("اسکن متوقف شد")
        onStopInternal()
    }

    // ----------------- Internals -----------------

    @SuppressLint("MissingPermission")
    private fun stopOnlyScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: Throwable) {
        }
        scanCallback = null
        scanner = null
    }

    @SuppressLint("MissingPermission")
    private fun onStopInternal() {
        stopOnlyScan()
        closeGatt()
        onStopCb?.invoke()
        onStopCb = null
        onFoundCb = null
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        onFoundCb?.invoke("اتصال به فروشنده ...")
        gatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onFoundCb?.invoke("به دستگاه متصل شد ✅")
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onFoundCb?.invoke("ارتباط قطع شد")
                closeGatt()
                onStopCb?.invoke()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val svc = g.getService(SERVICE_UUID)
            if (svc == null) {
                onFoundCb?.invoke("Service یافت نشد")
                g.disconnect()
                return
            }
            val tx = svc.getCharacteristic(CHAR_TX_UUID)
            if (tx == null) {
                onFoundCb?.invoke("Characteristic یافت نشد")
                g.disconnect()
                return
            }
            // یک‌بار بخوان
            val ok = g.readCharacteristic(tx)
            if (!ok) {
                onFoundCb?.invoke("خطا در readCharacteristic")
                g.disconnect()
            }
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristic.uuid == CHAR_TX_UUID) {
                val bytes = characteristic.value ?: ByteArray(0)
                val msg = try { String(bytes) } catch (_: Throwable) { "[bytes:${bytes.size}]" }
                onFoundCb?.invoke("پیام دریافت‌شده: $msg")
                g.disconnect()
            }
        }
    }

    private fun closeGatt() {
        try { gatt?.close() } catch (_: Throwable) {}
        gatt = null
    }
}
