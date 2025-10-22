package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

/**
 * Central BLE:
 * - Scan با فیلتر SERVICE_UUID
 * - Connect به اولین فروشنده
 * - Discover و Read از CHAR_TX_UUID
 * - onFound: پیام وضعیت/داده برای UI
 * - onStop: اتمام عملیات (یا تایم‌اوت)
 */
class BleClient(private val context: Context) {

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")
        private const val TAG = "BleClient"
        private const val TIMEOUT_MS = 15_000L
    }

    private val btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? get() = btMgr.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null

    private val main = Handler(Looper.getMainLooper())
    private var timeoutPosted = false

    @SuppressLint("MissingPermission")
    fun startScan(onFound: (String) -> Unit, onStop: () -> Unit) {
        val ad = adapter ?: run {
            onFound("Bluetooth adapter not available")
            onStop(); return
        }
        scanner = ad.bluetoothLeScanner ?: run {
            onFound("BLE scanner not available")
            onStop(); return
        }

        // فیلتر روی Service UUID فروشنده
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val dev = result.device
                onFound("یافت شد: ${dev.name ?: "(بدون‌نام)"} | ${dev.address}")
                stopInternal(onStop) // توقف اسکن
                connect(dev, onFound, onStop)  // اتصال به همان فروشنده
            }

            override fun onScanFailed(errorCode: Int) {
                onFound("Scan failed: $errorCode")
                stopInternal(onStop)
            }
        }

        scanner?.startScan(filters, settings, scanCallback)
        onFound("🔎 اسکن شروع شد…")

        // تایم‌اوت برای اسکن
        if (!timeoutPosted) {
            timeoutPosted = true
            main.postDelayed({
                timeoutPosted = false
                onFound("⏱️ اسکن به زمان‌سنج رسید")
                stopInternal(onStop)
            }, TIMEOUT_MS)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan(onStop: (() -> Unit)? = null) {
        stopInternal(onStop)
    }

    @SuppressLint("MissingPermission")
    private fun connect(
        device: BluetoothDevice,
        onFound: (String) -> Unit,
        onStop: () -> Unit
    ) {
        onFound("اتصال به فروشنده…")
        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onFound("متصل شد ✅ — در حال کشف سرویس‌ها…")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onFound("قطع اتصال")
                    closeGatt()
                    onStop()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val svc = gatt.getService(SERVICE_UUID)
                if (svc == null) {
                    onFound("Service یافت نشد")
                    gatt.disconnect()
                    return
                }
                val tx = svc.getCharacteristic(CHAR_TX_UUID)
                if (tx == null) {
                    onFound("Characteristic یافت نشد")
                    gatt.disconnect(); return
                }
                val ok = gatt.readCharacteristic(tx)
                if (!ok) onFound("خواندن آغاز نشد")
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (characteristic.uuid == CHAR_TX_UUID) {
                    val bytes = characteristic.value ?: ByteArray(0)
                    val msg = try { String(bytes) } catch (_: Throwable) { "[bytes:${bytes.size}]" }
                    onFound("دریافت از فروشنده: $msg")
                    // اینجا می‌تونی validate کنی و ادامه فلو پرداخت رو جلو ببری
                    gatt.disconnect()
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun stopInternal(onStop: (() -> Unit)?) {
        try { scanner?.stopScan(scanCallback) } catch (_: Throwable) {}
        scanCallback = null
        scanner = null
        onStop?.invoke()
    }

    private fun closeGatt() {
        try { gatt?.close() } catch (_: Throwable) {}
        gatt = null
    }
}
