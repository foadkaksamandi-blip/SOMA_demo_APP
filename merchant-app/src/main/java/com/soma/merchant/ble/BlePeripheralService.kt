package com.soma.merchant.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.soma.merchant.shared.Protocol
import java.nio.charset.StandardCharsets

class BleServer(private val context: Context) {
    private val btManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = btManager.adapter
    private var gattServer: BluetoothGattServer? = null

    private val service = BluetoothGattService(
        Protocol.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY
    ).apply {
        addCharacteristic(BluetoothGattCharacteristic(
            Protocol.CHAR_CMD_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        ))
        addCharacteristic(BluetoothGattCharacteristic(
            Protocol.CHAR_RESULT_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        ))
    }

    fun start(onLog: (String)->Unit) {
        if (!adapter.isEnabled) { onLog("Bluetooth خاموش است"); return }
        gattServer = btManager.openGattServer(context, gattCallback).also {
            it?.addService(service)
            onLog("GATT Server آماده شد")
        }
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(Protocol.SERVICE_UUID))
            .build()
        adapter.bluetoothLeAdvertiser?.startAdvertising(settings, data, advCallback)
        onLog("در حال تبلیغ سرویس BLE…")
    }

    fun stop(onLog: (String)->Unit) {
        adapter.bluetoothLeAdvertiser?.stopAdvertising(advCallback)
        gattServer?.close()
        onLog("BLE متوقف شد")
    }

    private val advCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("BleServer","Advertising started")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e("BleServer","Advertising failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d("BleServer","conn state=$newState")
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
            if (characteristic.uuid == Protocol.CHAR_CMD_UUID) {
                val msg = String(value, StandardCharsets.UTF_8) // amount|txId
                val parts = msg.split("|")
                val txId = parts.getOrNull(1) ?: "UNKNOWN"
                // اینجا می‌توانی هر منطقی خواستی بگذاری؛ فعلاً همیشه OK می‌دهیم
                val result = "OK|$txId"
                val resChar = service.getCharacteristic(Protocol.CHAR_RESULT_UUID)
                resChar.value = result.toByteArray(StandardCharsets.UTF_8)
                gattServer?.notifyCharacteristicChanged(device, resChar, false)
            }
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }
    }
}
