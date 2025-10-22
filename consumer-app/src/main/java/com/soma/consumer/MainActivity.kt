package com.soma.consumer

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
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QRScanner

class MainActivity : AppCompatActivity() {

    private lateinit var bleClient: BleClient
    private lateinit var qrScanner: QRScanner

    private val blePerms =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ) else emptyArray()

    private val reqPermsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) startBleIfReady()
            else Toast.makeText(this, "اجازه‌های بلوتوث داده نشد", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bleClient = BleClient(this) { device ->
            Toast.makeText(this, "دستگاه پیدا شد: ${device.name ?: "نامشخص"}", Toast.LENGTH_SHORT).show()
        }
        qrScanner = QRScanner(this) { result ->
            Toast.makeText(this, "نتیجه QR: $result", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStartScan).setOnClickListener { ensurePermsThenStart() }
        findViewById<Button>(R.id.btnStopScan).setOnClickListener { bleClient.stopScan() }
        findViewById<Button>(R.id.btnQR).setOnClickListener { qrScanner.startScan() }

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
        bleClient.startScan()
        Toast.makeText(this, "در حال اسکن دستگاه‌ها...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient.stopScan()
    }
}
