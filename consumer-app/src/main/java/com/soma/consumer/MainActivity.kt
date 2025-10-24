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
            // در صورت نیاز به دوربین، فعلاً فقط QR تولید می‌کنیم تا بیلد سبز شود
            val content = "SOMA|TX|${amount}|${System.currentTimeMillis()}"
            ivQR.setImageBitmap(QRHandler.generate(content))
            tvStatus.text = "QR ساخته شد"
        }

        btnStartBLE.setOnClickListener {
            val ok = Perms.ensureBleScan(this)
            if (ok) {
                tvStatus.text = "در حال جستجو برای دستگاه فروشنده…"
                // اینجا میتوانی کد BLE واقعی خودت را صدا بزنی
            } else {
                tvStatus.text = "برای BLE، مجوز لازم است"
            }
        }

        btnStopBLE.setOnClickListener {
            tvStatus.text = "BLE متوقف شد"
            // اینجا stop واقعی BLE را فراخوانی کن
        }
    }
}
