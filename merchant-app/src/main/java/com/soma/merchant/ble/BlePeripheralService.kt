package com.soma.merchant.ble

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class BlePeripheralService : Service() {

    private var advertiser: BluetoothLeAdvertiser? = null
    private var bluetoothManager: BluetoothManager? = null
    private var gattServer: BluetoothGattServer? = null
    private val binder = LocalBinder()

    private val SERVICE_UUID = UUID.fromString("0000A000-0000-1000-8000-00805F9B34FB")
    private val CHAR_UUID = UUID.fromString("0000A001-0000-1000-8000-00805F9B34FB")

    inner class LocalBinder : Binder() {
        fun getService(): BlePeripheralService = this@BlePeripheralService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        gattServer = bluetoothManager?.openGattServer(this, gattCallback)
        advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
        setupGatt()
        startAdvertise()
    }

    private fun setupGatt() {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
    }

    private fun startAdvertise() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d("BLE", "Advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE", "Advertising failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            val amount = value?.toString(Charsets.UTF_8) ?: "?"
            Log.d("BLE", "Amount received: $amount")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }
    }

    override fun onDestroy() {
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        super.onDestroy()
    }
}
