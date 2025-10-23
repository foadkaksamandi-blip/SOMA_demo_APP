package com.soma.merchant.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat

/**
 * BLE Peripheral advertiser used by the Merchant app.
 * Requires API 21+ (Android 5.0+)
 */
class BlePeripheralService {

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null

    // UUID اختصاصی سرویس BLE
    private val serviceUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")

    /**
     * بررسی آماده‌بودن Bluetooth برای تبلیغ (Advertise)
     */
    fun isReady(context: Context): Boolean {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter = mgr?.adapter ?: return false
        if (!adapter.isEnabled || !adapter.isMultipleAdvertisementSupported) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasAdvertise = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
            val hasConnect = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasAdvertise || !hasConnect) return false
        }
        return true
    }

    /**
     * شروع تبلیغ BLE با payload دلخواه
     */
    fun startAdvertising(
        context: Context,
        payload: ByteArray? = null,
        onStart: (() -> Unit)? = null,
        onFail: ((Int) -> Unit)? = null
    ) {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter = mgr?.adapter ?: return onFail?.invoke(-1)!!
        advertiser = adapter.bluetoothLeAdvertiser ?: return onFail?.invoke(-2)!!

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .setIncludeDeviceName(false)

        payload?.let {
            val safe = it.copyOf(minOf(it.size, 20))
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

    /**
     * توقف تبلیغ BLE
     */
    fun stopAdvertising() {
        advertiser?.let { adv ->
            advertiseCallback?.let { adv.stopAdvertising(it) }
        }
        advertiseCallback = null
        advertiser = null
    }
}
