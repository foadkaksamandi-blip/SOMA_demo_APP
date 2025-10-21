package com.soma.consumer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleClient(private val context: Context) {

    private val TAG = "SOMA-BLE-Client"

    // UUIDهای سرویس/ویژگی مشترک
    private val SERVICE_UUID = UUID.fromString("5e6d1a70-9f59-4d7e-9a2b-ff7b20a90a10")
    private val CHAR_RX_UUID = UUID.fromString("a3af3c56-2a2a-47cc-87a8-2f7d0f87b001") // write
    private val CHAR_TX_UUID = UUID.fromString("b7c2d6aa-2cf2-4b8f-9d0d-7c0f8b87b002") // notify

    private var bluetoothGatt: BluetoothGatt? = null
    private var txChar: BluetoothGattCharacteristic? = null
    private var rxChar: BluetoothGattCharacteristic? = null

    private val mgr: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? get() = mgr.adapter
    private val scanner: BluetoothLeScanner? get() = adapter?.bluetoothLeScanner

    private var onResult: ((Boolean, String) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun pay(amount: Long, txId: String, onResult: (Boolean, String) -> Unit) {
        this.onResult = onResult
        val f = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        val s = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(listOf(f), s, scanCallback)
        Log.d(TAG, "scan started for service $SERVICE_UUID …")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(TAG, "device found: ${result.device.address}")
            scanner?.stopScan(this)
            bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            onResult?.invoke(false, "scan failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "connected, discovering services…")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onResult?.invoke(false, "disconnected")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                onResult?.invoke(false, "service not found")
                return
            }
            rxChar = service.getCharacteristic(CHAR_RX_UUID)
            txChar = service.getCharacteristic(CHAR_TX_UUID)

            if (txChar == null || rxChar == null) {
                onResult?.invoke(false, "characteristics not found")
                return
            }

            // فعال‌سازی notify روی TX
            gatt.setCharacteristicNotification(txChar, true)
            txChar?.descriptors?.forEach {
                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(it)
            }

            // ارسال پیام پرداخت بعد از کشف سرویس
            val payload = lastPendingPayload ?: run {
                // اگر چیزی در صف نیست، پیام ساده تست
                "PING"
            }
            send(payload)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHAR_TX_UUID) {
                val msg = characteristic.getStringValue(0) ?: ""
                Log.d(TAG, "notify: $msg")
                if (msg.startsWith("OK:")) onResult?.invoke(true, msg) else onResult?.invoke(false, msg)
            }
        }
    }

    private var lastPendingPayload: String? = null

    @SuppressLint("MissingPermission")
    fun send(payload: String) {
        lastPendingPayload = payload
        val ch = rxChar ?: run {
            onResult?.invoke(false, "rx not ready")
            return
        }
        ch.value = payload.toByteArray(Charsets.UTF_8)
        val ok = bluetoothGatt?.writeCharacteristic(ch) ?: false
        if (!ok) onResult?.invoke(false, "write failed")
    }
}
