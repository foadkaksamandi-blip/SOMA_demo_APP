package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.utils.Perms
import com.soma.consumer.utils.QRHandler

class MainActivity : AppCompatActivity() {

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var amount: Long = -300000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = amount.toString()
        tvStatus.text = "آماده"

        btnScanQR.setOnClickListener {
            val content = "SOMA|TX|$amount|${System.currentTimeMillis()}"
            ivQR.setImageBitmap(QRHandler.generate(content))
            tvStatus.text = "QR ساخته شد"
        }

        btnStartBLE.setOnClickListener {
            val ok = Perms.ensureBleScan(this)
            if (ok) {
                tvStatus.text = "در حال جستجو برای دستگاه فروشنده…"
                // اینجا می‌تونی کد واقعی BLE رو اضافه کنی
            } else {
                tvStatus.text = "مجوز لازم برای BLE داده نشده"
            }
        }

        btnStopBLE.setOnClickListener {
            tvStatus.text = "BLE متوقف شد"
            // اینجا می‌تونی stop واقعی BLE رو بنویسی
        }
    }
}
