package com.soma.consumer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnScanBLE: Button
    private lateinit var btnStopBLE: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // آیدی‌ها دقیقاً با XML پیشنهادی قبلی یکی هستند
        tvBalance = findViewById(R.id.tvBalance)
        tvStatus  = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnScanBLE = findViewById(R.id.btnScanBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        btnScanQR.setOnClickListener {
            startQrScan()
        }

        btnScanBLE.setOnClickListener {
            // اینجا فعلاً فقط پیام وضعیت را آپدیت می‌کنیم
            tvStatus.text = "وضعیت: اسکن BLE شروع شد"
            // اگر BleClient داری، همین‌جا استارتش کن
        }

        btnStopBLE.setOnClickListener {
            tvStatus.text = "وضعیت: BLE متوقف شد"
            // اگر BleClient داری، همین‌جا استاپش کن
        }
    }

    private fun startQrScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("کد QR را مقابل دوربین قرار دهید")
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    @Deprecated("onActivityResult is deprecated but fine for IntentIntegrator")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (resultCode == Activity.RESULT_OK && result.contents != null) {
                tvStatus.text = "نتیجه QR: ${result.contents}"
                // اینجا لاجیک پرداخت/به‌روزرسانی موجودی را انجام بده
            } else {
                tvStatus.text = "اسکن لغو شد"
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
