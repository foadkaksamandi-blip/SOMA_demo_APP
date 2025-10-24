package com.soma.merchant.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat

class BLEPeripheralService {

    private var advertiser = (null as BluetoothAdapter?)?.bluetoothLeAdvertiser
    private var advertiseCallback: AdvertiseCallback? = null

    // همان UUID سرویس که Client به‌دنبالش می‌گردد
    private val serviceUuid = ParcelUuid.fromString("0000FEED-0000-1000-8000-00805F9B34FB")

    fun isReady(context: Context): Boolean {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter = mgr?.adapter ?: return false
        if (!adapter.isEnabled) return false
        if (!adapter.isMultipleAdvertisementSupported) return false

        if (Build.VERSION.SDK_INT >= 31) {
            val hasAdvertise = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
            val hasConnect = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasAdvertise || !hasConnect) return false
        }
        advertiser = adapter.bluetoothLeAdvertiser
        return advertiser != null
    }

    fun startAdvertising(
        context: Context,
        payload: ByteArray? = null,
        onStart: (() -> Unit)? = null,
        onFail: ((Int) -> Unit)? = null
    ) {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter = mgr?.adapter ?: run { onFail?.invoke(-1); return }
        advertiser = adapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .setIncludeDeviceName(false)

        payload?.let {
            val safe = it.copyOf(minOf(it.size, 20)) // Service Data حداکثر ~۲۰ بایت
            dataBuilder.addServiceData(serviceUuid, safe)
        }

        val data = dataBuilder.build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                onStart?.invoke()
            }
            override fun onStartFailure(errorCode: Int) {
                onFail?.invoke(errorCode)
            }
        }

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertising() {
        advertiseCallback?.let { cb -> advertiser?.stopAdvertising(cb) }
        advertiseCallback = null
        advertiser = null
    }
}
