package com.soma.consumer.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * کلاینت ساده BLE برای اسکن دستگاه‌ها.
 * دکمه شروع اسکن -> startScan
 * دکمه توقف -> stopScan
 */
class BleClient(private val context: Context) {

    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var btScanner: BluetoothLeScanner? = null
    private var isScanning = false

    private var currentCallback: ScanCallback? = null

    /** بررسی مجوزهای لازم در اندروید 12+ */
    private fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            // برای نسخه‌های قدیمی‌تر، ACCESS_FINE_LOCATION ممکن است لازم باشد
            val fine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            fine || coarse
        }
    }

    fun startScan(onFound: (String) -> Unit, onStopped: (() -> Unit)? = null) {
        if (isScanning) {
            Log.d("BLE", "Already scanning")
            Toast.makeText(context, "در حال اسکن هستیم…", Toast.LENGTH_SHORT).show()
            return
        }

        if (btAdapter == null) {
            Log.e("BLE", "Bluetooth adapter is null")
            Toast.makeText(context, "این دستگاه بلوتوث ندارد", Toast.LENGTH_LONG).show()
            return
        }

        if (!hasScanPermission()) {
            Log.e("BLE", "Scan permission not granted")
            Toast.makeText(context, "مجوزهای بلوتوث/موقعیت را بدهید", Toast.LENGTH_LONG).show()
            return
        }

        btScanner = btAdapter.bluetoothLeScanner
        if (btScanner == null) {
            Log.e("BLE", "BluetoothLeScanner is null")
            Toast.makeText(context, "اسکنر BLE در دسترس نیست", Toast.LENGTH_LONG).show()
            return
        }

        currentCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device: BluetoothDevice? = result.device
                val name = device?.name ?: "(بدون‌نام)"
                val addr = device?.address ?: "?"
                Log.d("BLE", "Found: $name | $addr | rssi=${result.rssi}")
                onFound("$name ($addr)")
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE", "Scan failed: $errorCode")
                Toast.makeText(context, "خطا در اسکن ($errorCode)", Toast.LENGTH_LONG).show()
                stopScan()
            }
        }

        Log.d("BLE", "Starting BLE scan…")
        Toast.makeText(context, "🔎 شروع اسکن BLE…", Toast.LENGTH_SHORT).show()
        btScanner!!.startScan(currentCallback)
        isScanning = true

        // اگر خواستی بعد از مدت مشخص خودش متوقف شود، می‌توان تایمر گذاشت
        // Handler(Looper.getMainLooper()).postDelayed({ stopScan(); onStopped?.invoke() }, 15_000)
    }

    fun stopScan(onStopped: (() -> Unit)? = null) {
        if (!isScanning) {
            Log.d("BLE", "Not scanning")
            return
        }

        try {
            btScanner?.stopScan(currentCallback)
            Log.d("BLE", "Scan stopped")
            Toast.makeText(context, "⏹ اسکن متوقف شد", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Log.e("BLE", "Stop scan error: ${t.message}", t)
        } finally {
            isScanning = false
            currentCallback = null
            onStopped?.invoke()
        }
    }
}
