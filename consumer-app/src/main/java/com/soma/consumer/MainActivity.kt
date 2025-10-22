package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BLEClient
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    // Viewها
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQr: Button
    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button

    // سرویس‌ها
    private lateinit var bleClient: BLEClient
    private lateinit var qrScanner: QrScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnQr = findViewById(R.id.btnQr)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStopScan = findViewById(R.id.btnStopScan)

        checkAndRequestPerms {
            initAfterPerms()
        }

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

        btnStartScan.setOnClickListener {
            if (ensureBleReady()) {
                tvStatus.text = "در حال اسکن BLE ..."
                bleClient.startScan(
                    onFound = { device ->
                        tvStatus.text = "پیدا شد: $device"
                    },
                    onStop = {
                        tvStatus.text = "اسکن متوقف شد"
                    }
                )
            }
        }

        btnStopScan.setOnClickListener {
            bleClient.stopScan { tvStatus.text = "اسکن متوقف شد" }
        }
    }

    private fun initAfterPerms() {
        bleClient = BLEClient(this)
        qrScanner = QrScanner(this)
    }

    private fun checkAndRequestPerms(onGranted: () -> Unit) {
        val needs = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 31) {
            needs += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            needs += Manifest.permission.ACCESS_FINE_LOCATION
        }
        needs += Manifest.permission.CAMERA

        val toAsk = needs.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toAsk.isEmpty()) onGranted()
        else ActivityCompat.requestPermissions(this, toAsk.toTypedArray(), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initAfterPerms()
        } else {
            Toast.makeText(this, "اجازه‌ها لازم‌اند", Toast.LENGTH_LONG).show()
        }
    }

    private fun ensureBleReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "بلوتوث در دسترس نیست", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
