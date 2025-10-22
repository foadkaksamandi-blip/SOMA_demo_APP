package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    private lateinit var ble: BleClient

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnQr: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnQr = findViewById(R.id.btnQr)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        ble = BleClient(this)

        btnQr.setOnClickListener {
            QrScanner.startScan(
                activity = this@MainActivity,
                onResult = { text: String -> tvResult.text = "QR: $text" },
                onError = { t: Throwable -> tvResult.text = "خطا در QR: ${t.message}" }
            )
        }

        btnStart.setOnClickListener {
            tvStatus.text = "وضعیت: شروع اتصال BLE…"
            ble.start(
                listener = object : BleClient.Listener {
                    override fun onStatus(status: BleClient.Status) {
                        tvStatus.text = "وضعیت: $status"
                    }

                    override fun onLog(msg: String) {
                        // می‌توانی اگر خواستی لاگ‌ها را هم نمایش بدهی
                        // tvResult.text = msg
                    }

                    override fun onTxResult(ok: Boolean) {
                        tvResult.text = if (ok) "ارسال موفق" else "ارسال ناموفق"
                    }
                }
            )
        }

        btnStop.setOnClickListener {
            ble.stop()
            tvStatus.text = "وضعیت: متوقف شد"
        }
    }
}
