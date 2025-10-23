package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.*

/**
 * BLE Client — مسئول اسکن، اتصال، و خواندن داده از BLE فروشنده
 * برای انتقال داده امن بین اپ خریدار و فروشنده
 */

class BleClient(private val context: Context) {

    companion object {
        // UUID مشترک بین خریدار و فروشنده برای شناسایی سرویس و ویژگی
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

        private const val TAG = "BleClient"
        private const val TIMEOUT_MS = 15_000L
    }

    private val btMgr: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val adapter: BluetoothAdapter? get() = btMgr.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null
    private val main = Handler(Looper.getMainLooper())
    private var timeoutPosted = false

    /** بررسی فعال بودن بلوتوث */
    fun isReady(): Boolean {
        val ad = adapter ?: return false
        if (!ad.isEnabled) return false
        if (!ad.isMultipleAdvertisementSupported) return false
        return true
    }

    /** شروع اسکن دستگاه فروشنده */
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

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.device?.let { device ->
                    Log.d(TAG, "Found device: ${device.name} - ${device.address}")
                    connectToDevice(device, onFound, onStop)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                onFound("Scan failed: $errorCode")
                onStop()
            }
        }

        scanner?.startScan(filters, settings, scanCallback)
        onFound("در حال جستجو برای دستگاه فروشنده...")

        main.postDelayed({
            if (!timeoutPosted) {
                stopScan(onStop)
                onFound("زمان اسکن به پایان رسید")
            }
        }, TIMEOUT_MS)
    }

    /** توقف اسکن */
    @SuppressLint("MissingPermission")
    fun stopScan(onStop: () -> Unit) {
        scanner?.stopScan(scanCallback)
        onStop()
    }

    /** اتصال به دستگاه فروشنده */
    @SuppressLint("MissingPermission")
    private fun connectToDevice(
        device: BluetoothDevice,
        onFound: (String) -> Unit,
        onStop: () -> Unit
    ) {
        onFound("در حال اتصال به ${device.name ?: "Unknown"}...")

        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onFound("اتصال برقرار شد، در حال کشف سرویس‌ها...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onFound("ارتباط قطع شد")
                    onStop()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHAR_UUID)
                if (characteristic != null) {
                    onFound("سرویس BLE شناسایی شد ✅")
                    gatt.setCharacteristicNotification(characteristic, true)
                } else {
                    onFound("سرویس مورد نظر یافت نشد ❌")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                val value = characteristic.value?.decodeToString()
                onFound("داده دریافت شد: $value")
            }
        })
    }

    /** قطع ارتباط */
    @SuppressLint("MissingPermission")
    fun stopConnection(onStop: () -> Unit) {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        onStop()
    }
}
