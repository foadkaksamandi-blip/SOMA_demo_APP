package com.soma.consumer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import shared.utils.QRCodec
import shared.utils.QRPayload

class MainActivity : AppCompatActivity() {

    private lateinit var btnScanQr: Button
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button
    private lateinit var txtStatus: TextView
    private lateinit var txtBalance: TextView // اگر داری؛ در غیر این صورت حذفش کن

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanQr  = findViewById(R.id.btnScanQr)
        btnBleStart = findViewById(R.id.btnBleStart)
        btnBleStop  = findViewById(R.id.btnBleStop)
        txtStatus   = findViewById(R.id.txtStatus)
        txtBalance  = findViewById(R.id.txtBalance) // اگر موجود نیست، این خط و استفاده‌اش را حذف کن

        btnScanQr.setOnClickListener { startQrScanner() }

        // BLE همان رفتار قبلی
        btnBleStart.setOnClickListener { txtStatus.text = "BLE: شروع شد" }
        btnBleStop.setOnClickListener  { txtStatus.text = "BLE: متوقف شد" }
    }

    private fun startQrScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("QR را روبرو بگیرید")
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    @Deprecated("onActivityResult is used for ZXing IntentIntegrator")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (resultCode == Activity.RESULT_OK && result.contents != null) {
                handleScannedText(result.contents)
            } else {
                txtStatus.text = "اسکن لغو شد"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleScannedText(scannedText: String) {
        try {
            val payload: QRPayload = QRCodec.decodeFromQrText(scannedText)
            // نمایش اطلاعات (به‌دلخواه تغییر بده)
            txtStatus.text = "پرداخت: ${payload.amount} ${payload.currency}\nTX=${payload.txId}\n${payload.createdAt}"
            // اگر مانده/حساب هم داری:
            // val current = txtBalance.text.toString().filter { it.isDigit() }.toLongOrNull() ?: 0L
            // txtBalance.text = (current - payload.amount).toString()
        } catch (e: Exception) {
            txtStatus.text = "QR نامعتبر"
        }
    }
}
