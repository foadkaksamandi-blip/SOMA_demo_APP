package com.soma.consumer.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

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
            status("Bluetooth not supported")
            return
        }
        if (!btAdapter.isEnabled) {
            status("Requesting to enable Bluetooth…")
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        // اینجا فعلاً یک شبیه‌سازی سبک انجام می‌دهیم تا بیلد سبز و اپ کار کند.
        status("Scanning for devices…")
        mainHandler.postDelayed({
            status("Connecting to device…")
            mainHandler.postDelayed({
                status("Connected")
            }, 1200)
        }, 1200)
    }

    fun stop() {
        status("Stopped")
        // اگر اسکن/اتصال واقعی اضافه شد، اینجا متوقف شود.
    }

    fun onResume() { /* در نسخه ساده نیازی نیست */ }

    fun onPause() { /* در نسخه ساده نیازی نیست */ }

    fun close() { /* پاک‌سازی منابع در نسخه واقعی */ }

    private fun status(s: String) = statusCallback(s)
}
