package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BleClient(private val context: Context) {

    private val TAG = "BleClient"
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mgr.adapter
    }

    private var gatt: BluetoothGatt? = null

    // example UUIDs (replace with real ones)
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    }

    fun startScan(onFound: (address: String) -> Unit) {
        // simplified: use bluetoothAdapter?.bluetoothLeScanner with ScanCallback in real
        Log.d(TAG, "startScan called")
        // implement scanning, then call onFound(address)
    }

    fun connect(address: String, callback: BluetoothGattCallback) {
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: run {
            Log.e(TAG, "No device found")
            return
        }
        gatt = device.connectGatt(context, false, callback)
    }

    fun disconnect() {
        gatt?.close()
        gatt = null
    }

    fun readCharacteristic(): ByteArray? {
        val service: BluetoothGattService? = gatt?.getService(SERVICE_UUID)
        val char: BluetoothGattCharacteristic? = service?.getCharacteristic(CHAR_UUID)
        if (char != null) {
            gatt?.readCharacteristic(char)
            return char.value
        }
        return null
    }
}
