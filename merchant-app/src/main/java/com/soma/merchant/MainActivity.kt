package com.soma.merchant

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.soma.merchant.ble.BlePeripheralService

class MainActivity : AppCompatActivity() {

    private val bleService by lazy { BlePeripheralService() }

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnGenerateQR: Button
    private lateinit var btnStartBLE: Button

    private var amount = 200000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)

        tvAmount.text = amount.toString()

        btnGenerateQR.setOnClickListener { generateQR() }
        btnStartBLE.setOnClickListener { startBLE() }
    }

    private fun generateQR() {
        val content = "SOMA|TX|$amount|${System.currentTimeMillis()}"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 600, 600)
        val bmp = android.graphics.Bitmap.createBitmap(600, 600, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until 600) {
            for (y in 0 until 600) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        ivQR.setImageBitmap(bmp)
        tvStatus.text = "وضعیت: QR ساخته شد"
    }

    private fun startBLE() {
        val payload = "SOMA|TX|$amount".toByteArray()
        if (!bleService.isReady(this)) {
            tvStatus.text = "BLE در این دستگاه در دسترس نیست"
            return
        }
        bleService.startAdvertising(
            context = this,
            payload = payload,
            onStart = { runOnUiThread { tvStatus.text = "BLE فعال شد و در حال انتشار است" } },
            onFail = { code -> runOnUiThread { tvStatus.text = "خطای BLE ($code)" } }
        )
    }
}
