package com.soma.merchant.ble

import android.app.Service
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BlePeripheralService : Service() {
    private val TAG = "BlePeripheralService"
    private var advertiser: BluetoothLeAdvertiser? = null

    companion object {
        val AD_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    }

    override fun onCreate() {
        super.onCreate()
        val adapter = (getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
        advertiser = adapter.bluetoothLeAdvertiser
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAdvertising()
        return START_STICKY
    }

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(AD_UUID))
            .setIncludeDeviceName(true)
            .build()

        advertiser?.startAdvertising(settings, data, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "Advertise started")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e(TAG, "Advertise failed: $errorCode")
            }
        })
    }

    override fun onDestroy() {
        advertiser?.stopAdvertising(object : AdvertiseCallback() {})
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
