package com.soma.consumer.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

class BleClient(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var connected = false

    private val SERVICE_UUID = UUID.fromString("0000A000-0000-1000-8000-00805F9B34FB")
    private val CHAR_UUID = UUID.fromString("0000A001-0000-1000-8000-00805F9B34FB")

    fun connect(device: BluetoothDevice, onReady: (Boolean) -> Unit) {
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                connected = newState == BluetoothProfile.STATE_CONNECTED
                if (connected) {
                    gatt.discoverServices()
                    onReady(true)
                } else onReady(false)
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS)
                    Log.d("BLE", "Write success")
                else
                    Log.e("BLE", "Write failed: $status")
            }
        })
    }

    fun send(amount: String) {
        val service = bluetoothGatt?.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(CHAR_UUID)
        characteristic.value = amount.toByteArray(Charsets.UTF_8)
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
