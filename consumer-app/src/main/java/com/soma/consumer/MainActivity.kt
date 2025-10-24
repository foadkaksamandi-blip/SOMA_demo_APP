package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import com.soma.shared.utils.Perms
import com.soma.shared.utils.QRHandler   // اگر پکیج QRHandler شما فرق دارد، همین import را مطابق پروژه‌تان تغییر دهید.

class MainActivity : AppCompatActivity() {

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var bleClient: BleClient? = null
    private var balance: Int = -300000  // فقط برای نمایش دمو

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = balance.toString()
        tvStatus.text = "آماده"

        // --- QR Scan (دقیقا میره سراغ اسکنر قبلی خودت) ---
        btnScanQR.setOnClickListener {
            if (!Perms.ensureCamera(this)) return@setOnClickListener
            QRHandler.startScan(this) { resultText ->
                // اینجا همان منطق مرحلهٔ QR قبلی‌ات را صدا بزن
                tvStatus.text = "QR: $resultText"
            }
        }

        // --- BLE Scan Start ---
        btnStartBLE.setOnClickListener {
            if (!Perms.ensureBleScan(this)) return@setOnClickListener
            tvStatus.text = "در حال جستجو برای دستگاه فروشنده…"
            if (bleClient == null) bleClient = BleClient(this)

            bleClient?.startScan(
                onFound = { msg -> runOnUiThread { tvStatus.text = msg } },
                onStop  = { runOnUiThread { tvStatus.text = "اسکن تمام شد" } }
            )
        }

        // --- BLE Scan Stop ---
        btnStopBLE.setOnClickListener {
            bleClient?.stopScan()
            tvStatus.text = "BLE متوقف شد"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient?.stopScan()
    }
}
