package com.soma.merchant.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.util.*

class BleServer(
    private val context: Context,
    private val listener: Listener
) {
    interface Listener {
        fun onStatus(msg: String)
        fun onClientConnected(addr: String?)
        fun onClientDisconnected()
        fun onError(msg: String)
    }

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")
    }

    private val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bm.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null

    fun isReady(): Boolean = adapter?.isEnabled == true

    fun stop() {
        try { advertiser?.stopAdvertising(null) } catch (_: Throwable) {}
        try { gattServer?.close() } catch (_: Throwable) {}
        advertiser = null
        gattServer = null
        listener.onStatus("متوقف شد")
    }

    fun start() {
        if (!isReady()) {
            listener.onError("Bluetooth روشن نیست")
            return
        }
        if (!hasPerms()) {
            listener.onError("مجوزهای BLE داده نشده")
            return
        }
        startGattServer()
        startAdvertising()
    }

    private fun hasPerms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        listener.onStatus("ایجاد GATT Server…")
        gattServer = bm.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    listener.onClientConnected(device.address)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    listener.onClientDisconnected()
                }
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?
            ) {
                val value = "HELLO".toByteArray()
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
            ) {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        })

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or
                    BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
        listener.onStatus("سرویس آماده شد ✓")
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        listener.onStatus("پخـش آگهی…")
        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            listener.onError("Advertiser در دسترس نیست")
            return
        }
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser!!.startAdvertising(settings, data, object : android.bluetooth.le.AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                listener.onStatus("در حال انتشار آگهی ✓")
            }

            override fun onStartFailure(errorCode: Int) {
                listener.onError("Advertise failed: $errorCode")
            }
        })
    }
}
