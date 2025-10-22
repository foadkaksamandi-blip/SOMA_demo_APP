package com.soma.consumer.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.nio.charset.Charset
import java.util.*

class BLEClient(private val context: Context) {

    companion object {
        private const val TAG = "BLEClient"
        val SERVICE_UUID: UUID = UUID.fromString("000018FF-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002AFF-0000-1000-8000-00805F9B34FB")
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var connectedDevice: BluetoothDevice? = null
    private var characteristic: BluetoothGattCharacteristic? = null

    private var connectionCallback: ((Boolean) -> Unit)? = null
    private var messageCallback: ((String) -> Unit)? = null

    fun initialize(adapter: BluetoothAdapter) {
        bluetoothAdapter = adapter
        Log.i(TAG, "BLE Client initialized.")
    }

    fun connect(device: BluetoothDevice, onConnected: (Boolean) -> Unit, onMessage: (String) -> Unit) {
        connectionCallback = onConnected
        messageCallback = onMessage

        connectedDevice = device
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.i(TAG, "Connecting to ${device.address}")
    }

    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        connectedDevice = null
        Log.i(TAG, "Disconnected from peripheral.")
    }

    fun sendMessage(message: String) {
        coroutineScope.launch {
            try {
                val charac = characteristic
                if (charac == null) {
                    Log.e(TAG, "Characteristic is null; cannot send.")
                    return@launch
                }

                charac.value = message.toByteArray(Charset.defaultCharset())
                val success = bluetoothGatt?.writeCharacteristic(charac) ?: false

                if (success) Log.i(TAG, "Message sent: $message")
                else Log.e(TAG, "Failed to send message.")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message: ${e.message}")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    bluetoothGatt = gatt
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    connectionCallback?.invoke(false)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    connectionCallback?.invoke(true)
                    Log.i(TAG, "Service and characteristic discovered.")
                } else {
                    Log.e(TAG, "Characteristic not found.")
                }
            } else {
                Log.e(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value?.toString(Charset.defaultCharset()) ?: ""
            Log.i(TAG, "Message received: $value")
            messageCallback?.invoke(value)
        }
    }
}
