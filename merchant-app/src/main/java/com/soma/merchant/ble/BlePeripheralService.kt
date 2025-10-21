package com.soma.merchant.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.UUID

class BlePeripheralService : Service() {

    private val TAG = "SOMA-BLE-Server"

    private val SERVICE_UUID = UUID.fromString("5e6d1a70-9f59-4d7e-9a2b-ff7b20a90a10")
    private val CHAR_RX_UUID = UUID.fromString("a3af3c56-2a2a-47cc-87a8-2f7d0f87b001") // client writes here
    private val CHAR_TX_UUID = UUID.fromString("b7c2d6aa-2cf2-4b8f-9d0d-7c0f8b87b002") // server notifies here

    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var txChar: BluetoothGattCharacteristic? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        val mgr = getSystemService(BluetoothManager::class.java)
        gattServer = mgr.openGattServer(this, gattCallback)
        advertiser = mgr.adapter.bluetoothLeAdvertiser

        // تعریف سرویس و ویژگی‌ها
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val rx = BluetoothGattCharacteristic(
            CHAR_RX_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )

        txChar = BluetoothGattCharacteristic(
            CHAR_TX_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(rx)
        service.addCharacteristic(txChar)
        gattServer?.addService(service)

        // تبلیغ
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser?.startAdvertising(settings, data, advCallback)
        Log.d(TAG, "BLE advertising started.")
    }

    override fun onDestroy() {
        advertiser?.stopAdvertising(advCallback)
        gattServer?.close()
        super.onDestroy()
    }

    private val advCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertise success")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertise failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d(TAG, "conn state: $newState with ${device.address}")
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
            val msg = value.toString(Charsets.UTF_8)
            Log.d(TAG, "RX: $msg")

            // نمونه پردازش: msg مثل "TXID:AMOUNT"
            val ok = msg.contains(":")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)

            // پاسخ Notify روی TX
            val reply = if (ok) "OK:$msg" else "ERR:FORMAT"
            txChar?.value = reply.toByteArray(Charsets.UTF_8)
            gattServer?.notifyCharacteristicChanged(device, txChar, false)
        }
    }

    private fun startForegroundService() {
        val chId = "ble_server"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(chId, "BLE Server", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
        val n: Notification = NotificationCompat.Builder(this, chId)
            .setContentTitle("SOMA Merchant — BLE")
            .setContentText("در حال سرویس‌دهی بلوتوث")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
        startForeground(1011, n)
    }
}
