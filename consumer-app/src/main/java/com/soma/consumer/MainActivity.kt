package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QRScanner
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQR: Button
    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button

    private lateinit var qrScanner: QRScanner
    private var bleClient: BleClient? = null

    companion object {
        private const val REQ_CAMERA = 1001
        private const val REQ_BLE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnQR = findViewById(R.id.btnQR)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStopScan = findViewById(R.id.btnStopScan)

        // QR
        qrScanner = QRScanner(this) { contents ->
            handleQrPayload(contents)
        }
        btnQR.setOnClickListener {
            if (hasCameraPermission()) qrScanner.startScan()
            else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }

        // BLE
        btnStartScan.setOnClickListener {
            if (ensureBlePermissions()) startBleScan()
        }
        btnStopScan.setOnClickListener {
            bleClient?.stopScan()
            tvStatus.text = "وضعیت: اسکن متوقف شد"
        }
    }

    private fun handleQrPayload(raw: String) {
        // انتظار JSON با کلیدهای: type, merchantId, amount, txId, ts
        try {
            val obj = JSONObject(raw)
            if (obj.optString("type") == "PAY_REQUEST") {
                val merchantId = obj.optString("merchantId")
                val amount = obj.optLong("amount")
                val txId = obj.optString("txId")
                val ts = obj.optLong("ts")
                tvResult.text = "درخواست پرداخت:\nپذیرنده: $merchantId\nمبلغ: $amount\nTX: $txId\nزمان: $ts"
            } else {
                tvResult.text = "QR ناشناخته: $raw"
            }
        } catch (t: Throwable) {
            tvResult.text = "QR نامعتبر: $raw"
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun ensureBlePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= 31) {
            val need = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                need += Manifest.permission.BLUETOOTH_SCAN
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                need += Manifest.permission.BLUETOOTH_CONNECT
            if (need.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, need.toTypedArray(), REQ_BLE)
                return false
            }
        }
        return true
    }

    private fun startBleScan() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            tvStatus.text = "این دستگاه بلوتوث ندارد"
            return
        }
        if (!adapter.isEnabled) {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            Toast.makeText(this, "لطفاً بلوتوث را روشن کنید", Toast.LENGTH_SHORT).show()
            return
        }

        if (bleClient == null) {
            // توجه: امضای کلاس BleClient شما باید متناسب با پیاده‌سازی‌ فعلی باشد
            // اگر از نسخه ساده استفاده می‌کنی: BleClient(this) { device -> ... }
            // اگر از نسخه پیشرفته استفاده می‌کنی: همان را جایگزین کن
            bleClient = BleClient(this) { device ->
                tvStatus.text = "دستگاه پیدا شد: ${device.name ?: "نامشخص"}"
            }
        }
        bleClient?.startScan()
        tvStatus.text = "وضعیت: در حال اسکن BLE…"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        qrScanner.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CAMERA -> if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) qrScanner.startScan()
            REQ_BLE -> if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) startBleScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient?.stopScan()
    }
}
