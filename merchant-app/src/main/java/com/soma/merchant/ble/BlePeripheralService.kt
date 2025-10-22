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
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*

import java.nio.charset.Charset
import java.util.*

class BLEPeripheralService : Service() {

    companion object {
        private const val TAG = "BLEPeripheralService"
        val SERVICE_UUID: UUID = UUID.fromString("000018FF-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002AFF-0000-1000-8000-00805F9B34FB")
    }

    private val binder = LocalBinder()
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var currentDevice: BluetoothDevice? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    inner class LocalBinder : Binder() {
        fun getService(): BLEPeripheralService = this@BLEPeripheralService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        startGattServer()
        startAdvertising()
        Log.i(TAG, "BLE Peripheral Service started.")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAdvertising()
        stopGattServer()
        coroutineScope.cancel()
        Log.i(TAG, "BLE Peripheral Service stopped.")
    }

    /** BLE Advertising **/
    private fun startAdvertising() {
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
        Log.i(TAG, "Advertising started with service UUID: $SERVICE_UUID")
    }

    private fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        Log.i(TAG, "Advertising stopped.")
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "BLE Advertising started successfully.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE Advertising failed: $errorCode")
        }
    }

    /** GATT Server setup **/
    private fun startGattServer() {
        gattServer = bluetoothManager?.openGattServer(this, gattServerCallback)

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or
                    BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
        Log.i(TAG, "GATT Server started and service added.")
    }

    private fun stopGattServer() {
        gattServer?.close()
        gattServer = null
        Log.i(TAG, "GATT Server stopped.")
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                currentDevice = device
                Log.i(TAG, "Device connected: ${device?.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                currentDevice = null
                Log.i(TAG, "Device disconnected")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val response = "READY_TO_RECEIVE"
            gattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                response.toByteArray(Charset.defaultCharset())
            )
            Log.i(TAG, "Read request responded with: $response")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            val received = value?.toString(Charset.defaultCharset()) ?: ""
            Log.i(TAG, "Received data from Consumer: $received")
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }

            coroutineScope.launch {
                delay(500)
                sendAcknowledgement("TX_OK")
            }
        }
    }

    /** Send response back to Consumer **/
    private fun sendAcknowledgement(message: String) {
        try {
            val characteristic =
                gattServer?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTERISTIC_UUID)
            characteristic?.value = message.toByteArray(Charset.defaultCharset())
            currentDevice?.let {
                gattServer?.notifyCharacteristicChanged(it, characteristic, false)
            }
            Log.i(TAG, "Acknowledgement sent: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending acknowledgement: ${e.message}")
        }
    }
}
