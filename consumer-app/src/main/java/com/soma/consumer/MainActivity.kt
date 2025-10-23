package com.soma.consumer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private val ble by lazy { BleClient(this) }

    // ---- permissions ----
    private val permsForScan: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // برای اسکن روی < Android 12 نیاز به لوکیشن داریم
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestBlePerms =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val granted = permsForScan.all { p ->
                grants[p] == true || ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
            }
            if (granted) {
                startBleInternal()
            } else {
                tvStatus.text = "اجازه BLE داده نشد"
            }
        }

    private fun ensureBlePermissions(onGranted: () -> Unit) {
        val need = permsForScan.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isEmpty()) onGranted() else requestBlePerms.launch(permsForScan)
    }
    // ---------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance   = findViewById(R.id.tvBalance)
        tvStatus    = findViewById(R.id.tvStatus)
        btnScanQR   = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE  = findViewById(R.id.btnStopBLE)

        // مقادیر نمایشی شما
        tvBalance.text = "-300000"
        tvStatus.text  = "وضعیت: آماده"

        btnScanQR.setOnClickListener {
            // همون کدی که مرحله‌ی QR داشتید (اسکن واقعی). اگر الان موقت می‌خواهید:
            tvStatus.text = "لطفاً QR را اسکن کنید…"
            // TODO: اینجا متد اسکن QR قبلی‌تان را صدا بزنید.
        }

        btnStartBLE.setOnClickListener {
            ensureBlePermissions { startBleInternal() }
        }

        btnStopBLE.setOnClickListener {
            ble.stopScan {
                tvStatus.text = "اسکن BLE متوقف شد"
            }
        }
    }

    private fun startBleInternal() {
        tvStatus.text = "در حال اسکن BLE…"
        btnStartBLE.isEnabled = false
        ble.startScan(
            onFound = { msg ->
                runOnUiThread {
                    tvStatus.text = msg
                    btnStartBLE.isEnabled = true
                }
            },
            onStop = {
                runOnUiThread {
                    btnStartBLE.isEnabled = true
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopScan {}
    }
}
