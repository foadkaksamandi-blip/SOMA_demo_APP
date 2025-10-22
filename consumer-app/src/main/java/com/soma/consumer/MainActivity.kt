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
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    private lateinit var bleClient: BleClient
    private lateinit var qrScanner: QrScanner
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQr: Button
    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnQr = findViewById(R.id.btnQr)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStopScan = findViewById(R.id.btnStopScan)

        bleClient = BleClient(this)
        qrScanner = QrScanner(this)

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
            if (ensureBleScanPermissions()) {
                tvStatus.text = "در حال اسکن BLE ..."
                bleClient.startScan(
                    onFound = { device ->
                        tvStatus.text = "یافت شد: $device"
                    },
                    onStop = {
                        tvStatus.text = "اسکن متوقف شد"
                    }
                )
            }
        }

        btnStopScan.setOnClickListener {
            bleClient.stopScan()
            tvStatus.text = "اسکن متوقف شد"
        }
    }

    private fun ensureBleScanPermissions(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "بلوتوث در دسترس نیست", Toast.LENGTH_SHORT).show()
            return false
        }

        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_SCAN
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.ACCESS_FINE_LOCATION
        }

        return if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 200)
            false
        } else true
    }
}
