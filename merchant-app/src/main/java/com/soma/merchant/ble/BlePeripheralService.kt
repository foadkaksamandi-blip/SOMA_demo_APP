package com.soma.merchant.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

class BLEPeripheralService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val advertiser: BluetoothLeAdvertiser? by lazy {
        bluetoothAdapter?.bluetoothLeAdvertiser
    }

    private var advertising = false
    private val serviceUuid = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")

    private val advertiseCallback = object : AdvertiseCallback() {}

    fun startAdvertising() {
        if (advertising) return
        val adv = advertiser ?: return
        advertising = true

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(serviceUuid))
            .setIncludeDeviceName(true)
            .build()

        try {
            adv.startAdvertising(settings, data, advertiseCallback)
        } catch (t: Throwable) {
            advertising = false
            throw t
        }
    }

    fun stopAdvertising() {
        if (!advertising) return
        advertising = false
        advertiser?.stopAdvertising(advertiseCallback)
    }

    fun simulatePayment(amount: Double): Boolean = amount > 0
}
