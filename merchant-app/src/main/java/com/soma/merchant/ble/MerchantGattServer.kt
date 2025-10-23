package com.soma.merchant.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.nio.charset.Charset
import java.util.UUID

class MerchantGattServer(
    private val context: Context,
    private val serviceUuid: UUID,
    private val charTxUuid: UUID,
    private val onState: (String) -> Unit = {}
) {
    private val btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val btAdapter = btMgr.adapter

    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var txChar: BluetoothGattCharacteristic? = null

    fun start(jsonPayload: String) {
        if (!btAdapter.isEnabled) { onState("Bluetooth خاموش است"); return }
        setupGatt(jsonPayload)
        startAdvertising()
    }

    fun stop() {
        try { advertiser?.stopAdvertising(advCb) } catch (_: Throwable) {}
        advertiser = null
        try { gattServer?.close() } catch (_: Throwable) {}
        gattServer = null
        onState("BLE متوقف شد")
    }

    private fun setupGatt(jsonPayload: String) {
        gattServer = btMgr.openGattServer(context, gattCb)
        val service = BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        txChar = BluetoothGattCharacteristic(
            charTxUuid,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        ).apply {
            value = jsonPayload.toByteArray(Charset.forName("UTF-8"))
        }

        service.addCharacteristic(txChar)
        gattServer?.addService(service)
        onState("GATT Server آماده شد")
    }

    private fun startAdvertising() {
        advertiser = btAdapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true) // خیلی مهم
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(serviceUuid))
            .build()

        advertiser?.startAdvertising(settings, data, advCb)
        onState("Advertising شروع شد")
    }

    private val advCb = object : android.bluetooth.le.AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) { onState("Advertising موفق") }
        override fun onStartFailure(errorCode: Int) { onState("Advertising خطا: $errorCode") }
    }

    private val gattCb = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            onState("اتصال=${device.address} state=$newState")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == charTxUuid) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.value)
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
            }
        }
    }
}
