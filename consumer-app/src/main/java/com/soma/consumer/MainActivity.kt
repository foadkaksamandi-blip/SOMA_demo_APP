package com.soma.consumer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var btnScanQR: Button      // ← مطابق XML شما
    private lateinit var btnScanBLE: Button
    private lateinit var btnStopBLE: Button
    private lateinit var tvStatus: TextView

    private var balance: Long = 0L

    private val cameraPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startQrScan()
        else Toast.makeText(this, "مجوز دوربین رد شد", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance = findViewById(R.id.tvBalance)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnScanBLE = findViewById(R.id.btnScanBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)
        tvStatus = findViewById(R.id.tvStatus)

        updateBalance()
        tvStatus.text = "وضعیت: BLE متوقف شد"

        btnScanQR.setOnClickListener { ensureCameraAndScan() }
        btnScanBLE.setOnClickListener { tvStatus.text = "وضعیت: BLE شروع شد" } // هنوز ماک
        btnStopBLE.setOnClickListener { tvStatus.text = "وضعیت: BLE متوقف شد" }
    }

    private fun ensureCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startQrScan()
        } else {
            cameraPerm.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startQrScan() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("QR را داخل کادر قرار دهید")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(false)
        }.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "اسکن لغو شد", Toast.LENGTH_SHORT).show()
            } else {
                handleQrPayload(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQrPayload(payload: String) {
        // انتظار JSON ساده: {"type":"PURCHASE","txId":"...","amount":100000,...}
        try {
            val obj = org.json.JSONObject(payload)
            val type = obj.optString("type")
            val amount = obj.optLong("amount", 0L)
            val txId = obj.optString("txId")

            if (type == "PURCHASE" && amount > 0) {
                // این‌جا می‌تونیم اعتبارسنجی txId/cc را هم اضافه کنیم
                balance -= amount
                updateBalance()
                tvStatus.text = "پرداخت با موفقیت: $amount (txId=$txId)"
            } else {
                tvStatus.text = "QR نامعتبر"
            }
        } catch (e: Exception) {
            tvStatus.text = "پردازش QR نامعتبر"
        }
    }

    private fun updateBalance() {
        tvBalance.text = balance.toString()
    }
}
