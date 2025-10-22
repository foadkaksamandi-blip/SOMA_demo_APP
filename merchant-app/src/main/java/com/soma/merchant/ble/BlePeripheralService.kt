package com.soma.merchant.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.charset.Charset
import java.util.UUID

/**
 * BLE Peripheral + GATT Server
 * - Advertising با SERVICE_UUID
 * - یک Characteristic خواندنی (TX) برای ارسال payload به خریدار
 * - یک Characteristic نوشتنی (RX) برای دریافت ack/confirm از خریدار (فعلاً اختیاری)
 */
class BLEPeripheralService {

    companion object {
        // UUIDهای ثابت دمو
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb") // Merchant -> Consumer
        val CHAR_RX_UUID: UUID = UUID.fromString("0000cafe-0000-1000-8000-00805f9b34fb") // Consumer -> Merchant (اختیاری)
        private const val TAG = "BLEPeripheral"
    }

    // وضعیت
    private var gattServer: BluetoothGattServer? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertising = false

    // Payload پیش‌فرض (با QR به‌روز می‌شود)
    @Volatile private var currentPayload: String = "SOMA|AMOUNT=0|TXID=NA"

    /** از UI (مثلاً بعد از ساخت QR) صدا بزن تا مقدار ارسالی به خریدار آپدیت شود. */
    fun updatePayload(text: String) {
        currentPayload = text
        txCharacteristic?.value = text.toByteArray(Charset.forName("UTF-8"))
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(context: Context) {
        if (advertising) return
        val btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btMgr.adapter ?: return
        advertiser = adapter.bluetoothLeAdvertiser ?: return

        // 1) راه‌اندازی GATT Server + Service/Characteristic
        gattServer = btMgr.openGattServer(context, gattServerCallback).apply {
            // Service
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            // TX: READable | NOTIFY (برای مصرف‌کننده)
            txCharacteristic = BluetoothGattCharacteristic(
                CHAR_TX_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
            ).also { c ->
                c.value = currentPayload.toByteArray(Charset.forName("UTF-8"))
                // Descriptor لازم برای notify
                val cccd = BluetoothGattDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                    BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
                )
                c.addDescriptor(cccd)
                service.addCharacteristic(c)
            }

            // RX: WRITE (اختیاری – تأیید/پیام از خریدار)
            rxCharacteristic = BluetoothGattCharacteristic(
                CHAR_RX_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            ).also { service.addCharacteristic(it) }

            addService(service)
            Log.d(TAG, "GATT Service added")
        }

        // 2) شروع Advertising با Service UUID
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
        advertising = true
        Log.d(TAG, "Advertising started")
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(advertiseCallback)
        } catch (_: Throwable) {}
        advertising = false

        try { gattServer?.close() } catch (_: Throwable) {}
        gattServer = null
        Log.d(TAG, "Advertising stopped & GATT closed")
    }

    // --- Callbacks ---

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertise onStartSuccess")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertise failed: $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d(TAG, "GATT state: device=${device.address} status=$status newState=$newState")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == CHAR_TX_UUID) {
                val value = (txCharacteristic?.value ?: currentPayload.toByteArray(Charset.forName("UTF-8")))
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
                Log.d(TAG, "TX read by ${device.address}: ${String(value)}")
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (characteristic.uuid == CHAR_RX_UUID) {
                val received = String(value)
                Log.d(TAG, "RX write from ${device.address}: $received")
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            } else {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
        }
    }
}
