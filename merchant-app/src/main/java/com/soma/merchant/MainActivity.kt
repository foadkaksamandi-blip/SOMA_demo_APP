package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private lateinit var blePeripheralService: BLEPeripheralService

    private val blePerms =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        ) else emptyArray()

    private val reqPermsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                startBleIfReady()
            } else {
                Toast.makeText(this, "اجازه‌های بلوتوث داده نشد", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blePeripheralService = BLEPeripheralService(this)

        // دکمه‌ها (اختیاری: اگر در لایه‌ات دکمه داری)
        findViewById<Button?>(R.id.btnStartBle)?.setOnClickListener { ensurePermsThenStart() }
        findViewById<Button?>(R.id.btnStopBle)?.setOnClickListener { blePeripheralService.stopAdvertising() }

        // اگر دکمه نداری و می‌خوای خودکار شروع بشه:
        ensurePermsThenStart()
    }

    private fun ensurePermsThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val need = blePerms.any {
                ContextCompat.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED
            }
            if (need) {
                reqPermsLauncher.launch(blePerms)
                return
            }
        }
        startBleIfReady()
    }

    private fun startBleIfReady() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "این دستگاه بلوتوث ندارد", Toast.LENGTH_SHORT).show()
            return
        }
        if (!adapter.isEnabled) {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            Toast.makeText(this, "لطفاً بلوتوث را روشن کنید", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            blePeripheralService.startAdvertising()
            Toast.makeText(this, "BLE Advertising شروع شد", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(this, "خطا در شروع BLE: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blePeripheralService.stopAdvertising()
    }
}
