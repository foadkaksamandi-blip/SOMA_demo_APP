package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private val bleClient by lazy { BleClient(this) }
    private var balance = 300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance = findViewById(R.id.tvBalance)
        tvStatus = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvBalance.text = "$balance-"

        btnScanQR.setOnClickListener { scanQR() }
        btnStartBLE.setOnClickListener { startBLE() }
        btnStopBLE.setOnClickListener { stopBLE() }
    }

    /** اسکن QR */
    private fun scanQR() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("اسکن کد QR فروشنده")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    /** شروع BLE */
    private fun startBLE() {
        tvStatus.text = "در حال جستجو برای دستگاه..."
        bleClient.startScan(
            onFound = { msg -> runOnUiThread { tvStatus.text = msg } },
            onStop = { runOnUiThread { tvStatus.text = "اتصال بلوتوث متوقف شد" } }
        )
    }

    /** توقف BLE */
    private fun stopBLE() {
        bleClient.stopScan { runOnUiThread { tvStatus.text = "BLE غیرفعال شد" } }
    }

    /** نتیجه QR اسکن‌شده */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val amount = result.contents.split("|").getOrNull(2)?.toIntOrNull()
            if (amount != null && balance >= amount) {
                balance -= amount
                tvBalance.text = "$balance-"
                tvStatus.text = "پرداخت با موفقیت: $amount (txId=SOMA-${System.currentTimeMillis()})"
            } else {
                tvStatus.text = "موجودی کافی نیست یا کد اشتباه است"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
