// merchant-app/src/main/java/com/soma/merchant/MainActivity.kt
package com.soma.merchant

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Main screen for Merchant demo.
 * - Requests required Bluetooth permissions
 * - Starts/stops BlePeripheralService via explicit Intents
 * - Listens to status broadcasts from service and shows them in tvStatus
 *
 * Layout is expected to have:
 *   Button  id=btnPay        (تراکنش جدید)
 *   Button  id=btnRefund     (اختیاری/غیرفعال دمو)
 *   Button  id=btnReport     (اختیاری/غیرفعال دمو)
 *   TextView id=tvStatus
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnPay: Button
    private lateinit var btnRefund: Button
    private lateinit var btnReport: Button

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bm.adapter
    }

    // Broadcast actions shared with service
    companion object {
        const val ACTION_BLE_STATUS = "com.soma.merchant.ACTION_BLE_STATUS"
        const val EXTRA_STATUS_TEXT = "status_text"

        // Intents for service control
        const val ACTION_START_ADVERTISING = "com.soma.merchant.ACTION_START_ADVERTISING"
        const val ACTION_STOP_ADVERTISING = "com.soma.merchant.ACTION_STOP_ADVERTISING"

        private val RUNTIME_PERMS_31_PLUS = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        private val RUNTIME_PERMS_LEGACY = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_BLE_STATUS) {
                val text = intent.getStringExtra(EXTRA_STATUS_TEXT) ?: return
                tvStatus.text = text
            }
        }
    }

    private val requestEnableBt = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            updateStatus("بلوتوث فعال شد")
        } else {
            updateStatus("فعال‌سازی بلوتوث لغو شد")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnPay = findViewById(R.id.btnPay)
        btnRefund = findViewById(R.id.btnRefund)
        btnReport = findViewById(R.id.btnReport)

        btnRefund.isEnabled = false  // در دمو غیرفعال
        btnReport.isEnabled = false  // در دمو غیرفعال

        btnPay.setOnClickListener {
            if (ensureBluetoothReady()) {
                startBleAdvertising()
            }
        }

        btnPay.setOnLongClickListener {
            stopBleAdvertising()
            true
        }

        // ثبت گیرندهٔ وضعیت
        LocalBroadcastManager.getInstance(this).registerReceiver(
            statusReceiver,
            IntentFilter(ACTION_BLE_STATUS)
        )

        updateStatus("آماده")
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver)
        super.onDestroy()
    }

    // -------- BLE helpers --------

    private fun ensureBluetoothReady(): Boolean {
        // 1) مجوزها
        if (!hasAllPerms()) {
            requestPerms()
            return false
        }

        // 2) فعال بودن BT
        val enabled = bluetoothAdapter?.isEnabled == true
        if (!enabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBt.launch(intent)
            return false
        }

        // 3) برای برخی دستگاه‌ها نیاز به روشن بودن مکان
        if (Build.VERSION.SDK_INT < 31) {
            val locEnabled = try {
                Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE) != 0
            } catch (_: Exception) { true }
            if (!locEnabled) {
                updateStatus("برای BLE، مکان (Location) را روشن کنید")
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return false
            }
        }
        return true
    }

    private fun hasAllPerms(): Boolean {
        val perms = if (Build.VERSION.SDK_INT >= 31) RUNTIME_PERMS_31_PLUS else RUNTIME_PERMS_LEGACY
        return perms.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPerms() {
        val perms = if (Build.VERSION.SDK_INT >= 31) RUNTIME_PERMS_31_PLUS else RUNTIME_PERMS_LEGACY
        ActivityCompat.requestPermissions(this, perms, 1001)
    }

    // کنترل سرویس با Intent (بدون فراخوانی مستقیم متدهای Service)
    private fun startBleAdvertising() {
        updateStatus("شروع انتشار BLE…")
        val intent = Intent(this, BlePeripheralService::class.java).apply {
            action = ACTION_START_ADVERTISING
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopBleAdvertising() {
        updateStatus("توقف انتشار BLE…")
        val intent = Intent(this, BlePeripheralService::class.java).apply {
            action = ACTION_STOP_ADVERTISING
        }
        startService(intent)
    }

    private fun updateStatus(s: String) {
        tvStatus.text = s
    }
}
