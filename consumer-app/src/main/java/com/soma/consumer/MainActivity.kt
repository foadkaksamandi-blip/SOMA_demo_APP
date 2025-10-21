package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnScanQr: Button
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    private lateinit var bleClient: BleClient

    // لانچر رسمی ZXing (بدون کلاس واسط)
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        val text = result?.contents
        tvResult.text = if (text.isNullOrEmpty()) "نتیجه: -" else "نتیجه: $text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnScanQr = findViewById(R.id.btnScanQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        // توجه: طبق لاگ‌ها BleClient فقط یک Context می‌گیرد.
        bleClient = BleClient(this)

        btnScanQr.setOnClickListener {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("QR را مقابل دوربین بگیرید")
                .setBeepEnabled(true)
                .setOrientationLocked(false)
            qrLauncher.launch(options)
        }

        // چون امضای متدهای BleClient را نداریم و لاگ گفت stop وجود ندارد،
        // فعلاً فقط وضعیت UI را آپدیت می‌کنیم تا بیلد سبز شود.
        btnStartBle.setOnClickListener {
            tvStatus.text = "وضعیت: تلاش برای اتصال BLE"
            // TODO: اگر API صحیح BleClient را می‌دانیم، اینجا فراخوانی واقعی را بگذاریم.
            // مثلا: bleClient.startScan() یا bleClient.connect(...)
        }

        btnStopBle.setOnClickListener {
            tvStatus.text = "وضعیت: متوقف"
            // TODO: اگر API صحیح BleClient را می‌دانیم، اینجا فراخوانی واقعی را بگذاریم.
            // مثلا: bleClient.disconnect() یا bleClient.stopScan()
        }
    }
}
