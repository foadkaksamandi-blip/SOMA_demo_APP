package com.soma.merchant.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

/**
 * سرویس ساده‌ی BLE Peripheral برای ادورتایز کردن یک سرویس با UUID ثابت.
 * - بدون آرگومان ساخته می‌شود.
 * - متدهای start/stop کاملاً public هستند.
 */
class BLEPeripheralService {

    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var isAdvertising = false

    // UUID دلخواه برای دمو
    private val serviceUuid: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")

    private val advertiseCallback = object : AdvertiseCallback() {}

    private val gattCallback = object : BluetoothGattServerCallback() {}

    @SuppressLint("MissingPermission")
    fun startAdvertising(context: Context) {
        if (isAdvertising) return

        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btManager.adapter ?: return
        advertiser = adapter.bluetoothLeAdvertiser

        // (اختیاری) اگر خواستی GATT هم باز کنی:
        gattServer = btManager.openGattServer(context, gattCallback)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(serviceUuid))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (!isAdvertising) return
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
        advertiser = null
        isAdvertising = false
    }
}
