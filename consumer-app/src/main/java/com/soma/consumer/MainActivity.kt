package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import com.soma.shared.utils.Perms

class MainActivity : AppCompatActivity() {

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private val ble by lazy { BleClient(this) }
    private var amount = -300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = amount.toString()
        tvStatus.text = "آماده"

        // فعلاً فقط پیام نمایشی؛ بعداً اسکن واقعی QR را وصل می‌کنیم
        btnScanQR.setOnClickListener {
            tvStatus.text = "اسکن QR به‌زودی اضافه می‌شود"
        }

        btnStartBLE.setOnClickListener {
            // اجازه‌ها (API 31+ اسکن/کانکت؛ قدیمی‌تر لوکیشن)
            if (!Perms.ensureBleScan(this)) return@setOnClickListener
            tvStatus.text = "در حال جستجو برای دستگاه فروشنده…"

            ble.startScan(
                onFound = { name ->
                    runOnUiThread { tvStatus.text = "پیدا شد: $name" }
                },
                onStop = {
                    runOnUiThread {
                        if (tvStatus.text?.startsWith("پیدا شد") != true) {
                            tvStatus.text = "اسکن تمام شد"
                        }
                    }
                }
            )
        }

        btnStopBLE.setOnClickListener {
            ble.stopScan()
            tvStatus.text = "BLE متوقف شد"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopScan()
    }
}
