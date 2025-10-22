package com.soma.consumer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.qr.QRScanner
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {

    private lateinit var qrScanner: QRScanner
    private var bleClient: BleClient? = null

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnScan: Button
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    companion object {
        private const val REQ_CAMERA = 1001
        private const val REQ_BLE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views (ای‌دی‌ها باید با activity_main.xml یکی باشند)
        tvStatus   = findViewById(R.id.tvStatus)
        tvResult   = findViewById(R.id.tvResult)
        btnScan    = findViewById(R.id.btnScan)
        btnStartBle= findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        // QR
        qrScanner = QRScanner(this) { contents ->
            tvResult.text = contents
        }

        btnScan.setOnClickListener {
            if (hasCameraPermission()) {
                qrScanner.startScan()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA
                )
            }
        }

        // BLE
        btnStartBle.setOnClickListener {
            if (ensureBlePermissions()) {
                startBle()
            }
        }

        btnStopBle.setOnClickListener {
            bleClient?.stop()
            tvStatus.text = "BLE متوقف شد"
        }
    }

    // ---------------- QR result forwarding ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        qrScanner.handleActivityResult(requestCode, resultCode, data)
    }

    // ---------------- Permissions helpers ----------------
    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun ensureBlePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 31) {
            val needed = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_SCAN
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_CONNECT
            if (needed.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQ_BLE)
                return false
            }
        }
        return true
    }

    private fun startBle() {
        try {
            if (bleClient == null) {
                // براساس امضاهای قبلی: سازنده فقط Context می‌گیرد
                bleClient = BleClient(applicationContext)
            }
            tvStatus.text = "تلاش برای اتصال BLE…"
            // با توجه به پیاده‌سازی شما، این دو متد معمولاً وجود دارند
            bleClient?.start()
            Toast.makeText(this, "BLE شروع شد", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            tvStatus.text = "خطا در شروع BLE"
            Toast.makeText(this, "BLE error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // اگر کاربر بعد از درخواست مجوزها پاسخ دهد:
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CAMERA -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    qrScanner.startScan()
                } else {
                    Toast.makeText(this, "مجوز دوربین لازم است", Toast.LENGTH_SHORT).show()
                }
            }
            REQ_BLE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startBle()
                } else {
                    Toast.makeText(this, "مجوزهای BLE لازم است", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient?.stop()
    }
}
