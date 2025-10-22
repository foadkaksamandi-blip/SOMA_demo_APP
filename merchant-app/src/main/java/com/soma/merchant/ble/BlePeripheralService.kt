package com.soma.merchant.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat

/**
 * سرویس تبلیغ‌کننده BLE بسیار مینیمال برای دمو.
 * از MainActivity صدا می‌زنیم: startAdvertising(this) / stopAdvertising()
 */
class BLEPeripheralService : Service() {

    inner class LocalBinder : Binder() {
        val service: BLEPeripheralService get() = this@BLEPeripheralService
    }
    private val binder = LocalBinder()

    private val btAdapter: BluetoothAdapter? by lazy {
        val mgr = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mgr.adapter
    }
    private val advertiser: BluetoothLeAdvertiser? get() = btAdapter?.bluetoothLeAdvertiser

    private var advertising = false

    override fun onBind(intent: Intent?): IBinder = binder

    private fun hasAdvertisePermission(): Boolean {
        val need = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            emptyArray()
        }
        return need.all { p ->
            ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(host: Any? = null) {
        if (advertising) return
        if (!hasAdvertisePermission()) return
        if (btAdapter == null || !btAdapter!!.isEnabled) return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val data = AdvertiseData.Builder()
            // Service UUID یا داده دلخواه دمو (اینجا خالی کافی است)
            .setIncludeDeviceName(true)
            .build()

        advertiser?.startAdvertising(settings, data, advCallback)
        advertising = true
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (!advertising) return
        advertiser?.stopAdvertising(advCallback)
        advertising = false
    }

    private val advCallback = object : AdvertiseCallback() {}
}
