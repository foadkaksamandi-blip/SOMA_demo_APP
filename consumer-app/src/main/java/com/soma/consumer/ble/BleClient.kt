package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * نسخه‌ی ساده و تستی BLE Client برای دمو پروژه SOMA
 * - فقط شبیه‌سازی اتصال BLE جهت نمایش وضعیت‌ها
 * - هیچ اتصال واقعی برقرار نمی‌کند تا Build سبز و پایدار شود
 */
class BleClient(
    private val context: Context,
    private val statusCallback: (String) -> Unit = {}
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val btManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val btAdapter: BluetoothAdapter? = btManager?.adapter

    fun startScanAndConnect(enableBtLauncher: ActivityResultLauncher<Intent>) {
        if (btAdapter == null) {
            status("Bluetooth not supported on this device")
            return
        }

        if (!btAdapter.isEnabled) {
            status("Requesting to enable Bluetooth…")
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        // شبیه‌سازی مراحل اسکن و اتصال برای تست
        status("Scanning for nearby devices…")
        mainHandler.postDelayed({
            status("Device found → Connecting…")
            mainHandler.postDelayed({
                status("Connected successfully ✅")
            }, 1500)
        }, 1500)
    }

    fun stop() {
        status("Scanning stopped ❌")
    }

    fun onResume() {
        // در نسخه نهایی برای مدیریت lifecycle می‌تونیم کد اضافه کنیم
    }

    fun onPause() {
        // در نسخه نهایی برای توقف موقت اتصال BLE
    }

    fun close() {
        status("Connection closed")
    }

    private fun status(s: String) {
        mainHandler.post { statusCallback(s) }
    }
}
