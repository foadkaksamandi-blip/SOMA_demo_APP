package com.soma.merchant.ble

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BlePeripheralService : Service() {

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        val CHAR_TX_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")
        private const val TAG = "BlePeripheral"
    }

    private val binder = LocalBinder()
    private var gattServer: BluetoothGattServer? = null
    private var isAdvertising = false

    inner class LocalBinder : Binder() {
        fun service(): BlePeripheralService = this@BlePeripheralService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        setupGattServer()
    }

    override fun onDestroy() {
        stopAdvertising()
        gattServer?.close()
        super.onDestroy()
    }

    private fun bluetoothAdapter(): BluetoothAdapter? {
        val manager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter
    }

    private fun setupGattServer() {
        val manager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
        gattServer = manager?.openGattServer(this, gattCallback)

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val txChar = BluetoothGattCharacteristic(
            CHAR_TX_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val cccd = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        txChar.addDescriptor(cccd)
        service.addCharacteristic(txChar)

        gattServer?.addService(service)
    }

    fun startAdvertising(): Boolean {
        if (isAdvertising) return true
        val adapter = bluetoothAdapter() ?: return false
        if (!adapter.isEnabled) return false

        val advertiser = adapter.bluetoothLeAdvertiser ?: return false

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))   // <-- این خط
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
        Log.d(TAG, "startAdvertising()")
        return true
    }

    fun stopAdvertising() {
        if (!isAdvertising) return
        val advertiser = bluetoothAdapter()?.bluetoothLeAdvertiser ?: return
        advertiser.stopAdvertising(advertiseCallback)
        isAdvertising = false
        Log.d(TAG, "stopAdvertising()")
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "Advertise success")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertise failed: $errorCode")
            isAdvertising = false
        }
    }

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (characteristic?.uuid == CHAR_TX_UUID) {
                val payload = "HELLO_FROM_MERCHANT".toByteArray()
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, payload)
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }
    }
}
