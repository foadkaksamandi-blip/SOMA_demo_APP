package com.soma.consumer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {

    private val bleClient by lazy { BleClient(this) }

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var balance = 300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance = findViewById(R.id.tvBalance)
        tvStatus = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvBalance.text = "-${balance}"

        btnScanQR.setOnClickListener { startQRScan() }
        btnStartBLE.setOnClickListener { startBLEScan() }
        btnStopBLE.setOnClickListener { stopBLEScan() }
    }

    private fun startQRScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("اسکن QR برای پرداخت")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val amount = 200000
            balance -= amount
            tvBalance.text = "-${balance}"
            tvStatus.text = "پرداخت با موفقیت: $amount\n(txId=SOMA-${System.currentTimeMillis()})"
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startBLEScan() {
        if (!bleClient.isReady()) {
            tvStatus.text = "اجازه یا روشن بودن BLE لازم است"
            return
        }

        tvStatus.text = "در حال اسکن BLE…"
        bleClient.startScan(
            onFound = { msg -> runOnUiThread { tvStatus.text = msg } },
            onStop = { runOnUiThread { tvStatus.text = "اسکن متوقف شد" } }
        )
    }

    private fun stopBLEScan() {
        bleClient.stopScan {
            runOnUiThread { tvStatus.text = "BLE متوقف شد" }
        }
    }
}
