package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BLEClient
import com.soma.consumer.qr.QrScanner // اگر کلاس QR داری نگه‌ش دار؛ اگر نداری این خط و دکمه‌های QR را حذف کن.

class MainActivity : AppCompatActivity() {

    // Viewها
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQr: Button
    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button

    // سرویس‌ها
    private lateinit var bleClient: BLEClient
    private lateinit var qrScanner: QrScanner // اگر QrScanner نداری، این خط و استفاده‌هایش را حذف کن.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnQr = findViewById(R.id.btnQr)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStopScan = findViewById(R.id.btnStopScan)

        // مجوزها
        checkAndRequestPerms { initAfterPerms() }
    }

    /** بعد از گرفتن مجوزها صدا می‌خورد */
    private fun initAfterPerms() {
        bleClient = BLEClient(this)
        qrScanner = QrScanner(this) // اگر QR نداری، پاک کن.

        // دکمه QR
        btnQr.setOnClickListener {
            qrScanner.startScan(
                onResult = { code ->
                    tvResult.text = "کد: $code"
                    tvStatus.text = "QR دریافت شد ✅"
                },
                onCancel = {
                    Toast.makeText(this, "اسکن لغو شد", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // شروع اسکن BLE
        btnStartScan.setOnClickListener {
            if (ensureBleReady()) {
                tvStatus.text = "در حال اسکن BLE ..."
                bleClient.startScan(
                    onFound = { deviceOrMsg ->
                        tvStatus.text = "پیدا شد: $deviceOrMsg"
                    },
                    onStop = {
                        tvStatus.text = "اسکن متوقف شد"
                    }
                )
            }
        }

        // توقف اسکن BLE
        btnStopScan.setOnClickListener {
            bleClient.stopScan {
                tvStatus.text = "اسکن متوقف شد ⛔"
            }
        }
    }

    /** بررسی روشن بودن BT و… */
    private fun ensureBleReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "بلوتوث در دسترس نیست", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!adapter.isEnabled) {
            Toast.makeText(this, "بلوتوث خاموش است", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /** درخواست مجوزها (برای APIهای مختلف) */
    private fun checkAndRequestPerms(after: () -> Unit) {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!has(Manifest.permission.BLUETOOTH_SCAN)) need += Manifest.permission.BLUETOOTH_SCAN
            if (!has(Manifest.permission.BLUETOOTH_CONNECT)) need += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (!has(Manifest.permission.ACCESS_FINE_LOCATION)) need += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (!has(Manifest.permission.CAMERA)) need += Manifest.permission.CAMERA

        if (need.isEmpty()) {
            after()
        } else {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), 200)
            // after() بعد از onRequestPermissionsResult صدا بزن اگر لازم شد.
            after() // اگر می‌خواهی ساده بگیری و همان لحظه ادامه بدهی
        }
    }

    private fun has(p: String): Boolean =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}
