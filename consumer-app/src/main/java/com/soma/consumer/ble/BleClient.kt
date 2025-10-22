package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

/**
 * BLE Client – اسکن دستگاه فروشنده و خواندن پیام (Notify/Read) از روی ویژگی TX
 */
class BLEClient(private val context: Context) {

    companion object {
        // UUID ها را در صورت نیاز با مقادیر خودت عوض کن
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

        private const val TAG = "BLEClient"
        private const val TIMEOUT_MS = 15_000L
    }

    private val btMgr: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val adapter: BluetoothAdapter? get() = btMgr?.adapter

    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null

    private val main = Handler(Looper.getMainLooper())
    private var timeoutPosted = false

    /**
     * اسکن را شروع می‌کند.
     * @param onFound وقتی پیام/دیواس پیدا شد صدا می‌خورد (متن پیام یا توضیح)
     * @param onStop وقتی اسکن/اتصال پایان یافت صدا می‌خورد
     */
    @SuppressLint("MissingPermission")
    fun startScan(onFound: (String) -> Unit, onStop: () -> Unit) {
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

        // فقط سرویس مورد نظر
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val dev = result.device
                onFound("دستگاه یافت شد: ${dev.name ?: "(بی‌نام)"} | ${dev.address}")
                stopInternal(onStop) // اسکن را متوقف کن
                connect(dev, onFound, onStop) // وصل شو
            }

            override fun onScanFailed(errorCode: Int) {
                onFound("Scan failed: $errorCode")
                stopInternal(onStop)
            }
        }

        scanner!!.startScan(filters, settings, scanCallback)
        onFound("اسکن شروع شد …")

        // تایم‌اوت
        if (!timeoutPosted) {
            timeoutPosted = true
            main.postDelayed({
                timeoutPosted = false
                onFound("اسکن به زمان‌بندی رسید (Timeout)")
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
        onFound("اتصال به فروشنده …")
        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onFound("متصل شد ✅ – در حال کشف سرویس‌ها")
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
                    gatt.disconnect()
                    return
                }

                // اول یک read ساده (اگر notify هم داشتی می‌تونی بعداً اضافه کنی)
                val ok = gatt.readCharacteristic(tx)
                if (!ok) onFound("درخواست Read ارسال نشد")
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (characteristic.uuid == CHAR_TX_UUID) {
                    val bytes = characteristic.value ?: ByteArray(0)
                    val msg = try { String(bytes) } catch (_: Throwable) { "[bytes:${bytes.size}]" }
                    onFound("پیام دریافتی: $msg")
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

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        try { gatt?.close() } catch (_: Throwable) {}
        gatt = null
    }
}
