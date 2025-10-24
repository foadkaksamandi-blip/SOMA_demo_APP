package com.soma.consumer

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : AppCompatActivity() {

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button
    private lateinit var ivQR: ImageView

    // برای تست – هر زمان خواستی از ورودی کاربر بگیر
    private var amount: Int = -300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)
        ivQR = findViewById(R.id.ivQR)

        tvAmount.text = amount.toString()
        tvStatus.text = "آماده"

        // تولید QR (صرفاً نمایش؛ اسکنِ دوربین فعلاً نداریم)
        btnScanQR.setOnClickListener {
            val payload = "SOMA|TX|$amount|${System.currentTimeMillis()}"
            val bmp = makeQR(payload, 600, 600)
            ivQR.setImageBitmap(bmp)
            tvStatus.text = "QR تولید شد"
        }

        // شروع BLE (فعلاً فقط پیام وضعیت؛ برای اتصال واقعی مرحله‌ی بعد)
        btnStartBLE.setOnClickListener {
            tvStatus.text = "در حال جستجو برای دستگاه فروشنده..."
            // TODO: پیاده‌سازی اسکن واقعی BLE در مرحله بعد
        }

        // توقف BLE (فعلاً پیام)
        btnStopBLE.setOnClickListener {
            tvStatus.text = "BLE متوقف شد"
            // TODO: توقف اسکن/اتصال واقعی
        }
    }

    /** تولید Bitmap از متن QR با ZXing */
    private fun makeQR(content: String, width: Int, height: Int): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
