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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bleClient = BleClient(this)
        qrScanner = QrScanner(this)

        val btnStartBle: Button? = findViewById(R.id.btnStartBle)
        val btnStopBle : Button? = findViewById(R.id.btnStopBle)
        val btnScanQr  : Button? = findViewById(R.id.btnScanQr)
        val tvStatus   : TextView? = findViewById(R.id.tvStatus)

        if (btnStartBle == null) Toast.makeText(this, "btnStartBle در layout پیدا نشد", Toast.LENGTH_SHORT).show()
        if (btnStopBle  == null) Toast.makeText(this, "btnStopBle در layout پیدا نشد", Toast.LENGTH_SHORT).show()
        if (btnScanQr   == null) Toast.makeText(this, "btnScanQr در layout پیدا نشد", Toast.LENGTH_SHORT).show()
        if (tvStatus    == null) Toast.makeText(this, "tvStatus در layout پیدا نشد", Toast.LENGTH_SHORT).show()

        btnScanQr?.setOnClickListener {
            qrScanner.startScan(
                onResult = { code ->
                    tvStatus?.text = "QR: $code"
                },
                onCancel = {
                    Toast.makeText(this, "اسکن لغو شد", Toast.LENGTH_SHORT).show()
                }
            )
        }

        btnStartBle?.setOnClickListener {
            if (ensureBleScanPermissions()) {
                tvStatus?.text = "وضعیت: در حال اسکن BLE"
                bleClient.startScan(
                    onFound = { deviceName ->
                        tvStatus?.text = "پیدا شد: $deviceName"
                    },
                    onStop = {
                        tvStatus?.text = "اسکن متوقف شد"
                    }
                )
            }
        }

        btnStopBle?.setOnClickListener {
            bleClient.stopScan()
            tvStatus?.text = "اسکن متوقف شد"
        }
    }

    private fun ensureBleScanPermissions(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "این دستگاه بلوتوث ندارد", Toast.LENGTH_SHORT).show()
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
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 20)
            false
        } else true
    }
}
