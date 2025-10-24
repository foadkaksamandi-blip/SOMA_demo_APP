package com.soma.merchant

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.soma.merchant.ble.BLEPeripheralService
import com.soma.shared.utils.Perms

class MainActivity : AppCompatActivity() {

    private val bleService by lazy { BLEPeripheralService() }

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnGenerateQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var amount = 200000  // فقط نمایش دمو

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = amount.toString()
        tvStatus.text = "آماده"

        // --- QR (همان منطق مرحلهٔ قبل؛ فقط روی دکمه سیم‌کشی شده) ---
        btnGenerateQR.setOnClickListener { generateQR() }

        // --- BLE Advertise Start ---
        btnStartBLE.setOnClickListener {
            if (!Perms.ensureBleAdvertise(this)) return@setOnClickListener
            val payload = "SOMA|TX|$amount".toByteArray()
            tvStatus.text = "در حال فعال‌سازی BLE…"

            bleService.startAdvertising(
                context = this,
                payload = payload,
                onStart = {
                    runOnUiThread { tvStatus.text = "BLE فعال شد و در حال انتشار است" }
                },
                onFail = { code ->
                    runOnUiThread { tvStatus.text = "خطای BLE: $code" }
                }
            )
        }

        // --- BLE Advertise Stop ---
        btnStopBLE.setOnClickListener {
            bleService.stopAdvertising()
            tvStatus.text = "BLE متوقف شد"
        }
    }

    private fun generateQR() {
        // همان ساخت QR قبلی؛ تنها مقدار content را طبق نیاز تنظیم کن
        val content = "SOMA|TX|$amount|${System.currentTimeMillis()}"
        val bmp = buildQrBitmap(content, 600)
        ivQR.setImageBitmap(bmp)
        tvStatus.text = "وضعیت: QR ساخته شد"
    }

    private fun buildQrBitmap(content: String, size: Int): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService.stopAdvertising()
    }
}
