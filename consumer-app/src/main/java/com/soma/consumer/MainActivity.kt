package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BLEClient
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQr: Button
    private lateinit var btnStartScan: Button
    private lateinit var btnStopScan: Button

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

        requestPermsIfNeeded()

        bleClient = BLEClient(this)
        qrScanner = QrScanner(this)

        btnQr.setOnClickListener {
            qrScanner.startScan(
                onResult = { code ->
                    tvResult.text = "QR دریافت شد ✅ : $code"
                    tvStatus.text = "کد QR دریافت شد"
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
            } else {
                Toast.makeText(this, "بلوتوث خاموش است", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopScan.setOnClickListener {
            bleClient.stopScan {
                tvStatus.text = "اسکن متوقف شد"
            }
        }
    }

    private fun requestPermsIfNeeded() {
        val perms = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), 100)
        }
    }

    private fun ensureBleReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter != null && adapter.isEnabled
    }
}
