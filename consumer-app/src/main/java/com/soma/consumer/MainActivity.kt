package com.soma.consumer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnScanBLE: Button
    private lateinit var btnStopBLE: Button

    private var balance: Long = 300_000 // دمو

    private lateinit var ble: BleClient

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* نتیجه لازم نیست */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance   = findViewById(R.id.tvBalance)
        tvStatus    = findViewById(R.id.tvStatus)
        btnScanQR   = findViewById(R.id.btnScanQR)
        btnScanBLE  = findViewById(R.id.btnScanBLE)
        btnStopBLE  = findViewById(R.id.btnStopBLE)

        tvBalance.text = balance.toString()

        // QR قبلی شما هر طور که بود کار می‌کرد؛ همان را نگه دارید
        btnScanQR.setOnClickListener {
            // اینجا کدی که قبلاً برای اسکن QR داشتید را نگه دارید
            tvStatus.text = "اسکن QR (طبق مرحله قبل)"
        }

        ble = BleClient(this)

        btnScanBLE.setOnClickListener {
            ensureBlePermissions()
            tvStatus.text = "در حال اسکن BLE…"
            ble.startScan(
                onFound = { msg ->
                    runOnUiThread {
                        tvStatus.text = "پیام دریافتی: $msg"
                        handleOfferAndPay(msg)
                    }
                },
                onStop = {
                    runOnUiThread { tvStatus.text = "اسکن متوقف شد" }
                }
            )
        }

        btnStopBLE.setOnClickListener {
            ble.stopScan { runOnUiThread { tvStatus.text = "اسکن متوقف شد" } }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopScan {}
    }

    private fun ensureBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ))
        }
    }

    /** پردازش JSON آفر و کم‌کردن موجودی */
    private fun handleOfferAndPay(json: String) {
        try {
            val obj = JSONObject(json)
            if (obj.optString("type") == "purchase_offer") {
                val amount = obj.optLong("amount", 0)
                val txId = obj.optString("txId")
                if (amount > 0 && balance >= amount) {
                    balance -= amount
                    tvBalance.text = balance.toString()
                    tvStatus.text = "پرداخت با موفقیت: ${amount} (txId=$txId)"
                } else {
                    tvStatus.text = "موجودی کافی نیست / مبلغ نامعتبر"
                }
            } else {
                tvStatus.text = "پیام نامعتبر"
            }
        } catch (e: Exception) {
            tvStatus.text = "خطا در پردازش پیام"
        }
    }
}
