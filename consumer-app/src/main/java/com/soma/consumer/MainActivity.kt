package com.soma.consumer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.soma.consumer.ble.BleClient
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnScanBLE: Button
    private lateinit var btnStopBLE: Button

    private var balance: Long = 300_000 // موجودی دمو

    private lateinit var ble: BleClient

    // لانچر مجوزهای BLE برای Android 12+
    private val blePermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    // لانچر مجوز دوربین
    private val camPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startQrScan()
            else Toast.makeText(this, "مجوز دوربین رد شد", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance   = findViewById(R.id.tvBalance)
        tvStatus    = findViewById(R.id.tvStatus)
        btnScanQR   = findViewById(R.id.btnScanQR)
        btnScanBLE  = findViewById(R.id.btnScanBLE)
        btnStopBLE  = findViewById(R.id.btnStopBLE)

        tvBalance.text = balance.toString()
        tvStatus.text = "آماده"

        // --- QR واقعی (IntentIntegrator) ---
        btnScanQR.setOnClickListener {
            ensureCameraAndScan()
        }

        // --- BLE ---
        ble = BleClient(this)

        btnScanBLE.setOnClickListener {
            ensureBlePermissions()
            tvStatus.text = "در حال اسکن BLE…"
            ble.startScan(
                onFound = { msg ->
                    runOnUiThread {
                        tvStatus.text = "پیشنهاد دریافت شد"
                        handleOfferAndPay(msg)
                    }
                },
                onStop = {
                    runOnUiThread { tvStatus.text = "اسکن متوقف شد" }
                }
            )
        }

        btnStopBLE.setOnClickListener {
            ble.stopScan {
                runOnUiThread { tvStatus.text = "اسکن متوقف شد" }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopScan {}
    }

    // ---------------- QR Scan ----------------

    private fun ensureCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startQrScan()
        } else {
            camPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startQrScan() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("QR را داخل کادر قرار دهید")
            setBeepEnabled(false)
            setBarcodeImageEnabled(false)
        }.initiateScan()
    }

    @Deprecated("Deprecated in Android API but still works with IntentIntegrator")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "اسکن لغو شد", Toast.LENGTH_SHORT).show()
            } else {
                // محتوای QR باید JSON آفر باشد (همانی که مرچنت می‌سازد)
                handleOfferAndPay(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // -------------- BLE Permissions --------------

    private fun ensureBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val needs = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.BLUETOOTH_CONNECT
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) needs += Manifest.permission.BLUETOOTH_SCAN

            if (needs.isNotEmpty()) {
                blePermLauncher.launch(needs.toTypedArray())
            }
        }
    }

    // -------------- پردازش آفر و پرداخت دمو --------------

    private fun handleOfferAndPay(json: String) {
        try {
            val obj = JSONObject(json)
            val type = obj.optString("type")
            val amount = obj.optLong("amount", 0)
            val txId = obj.optString("txId")

            if (type == "purchase_offer" && amount > 0) {
                if (balance >= amount) {
                    balance -= amount
                    tvBalance.text = balance.toString()
                    tvStatus.text = "پرداخت موفق: $amount (txId=$txId)"
                    // اینجا می‌تونیم تاریخچه/انیمیشن/ثبت تراکنش رو هم اضافه کنیم
                } else {
                    tvStatus.text = "موجودی کافی نیست"
                }
            } else {
                tvStatus.text = "پیام نامعتبر"
            }
        } catch (e: Exception) {
            tvStatus.text = "خطا در پردازش QR/BLE"
        }
    }
}
