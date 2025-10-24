package com.soma.merchant

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

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnGenerateQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button
    private lateinit var ivQR: ImageView

    private val bleService by lazy { BLEPeripheralService() }
    private var amount = 200000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)
        ivQR = findViewById(R.id.ivQR)

        tvAmount.text = amount.toString()
        tvStatus.text = "آماده"

        btnGenerateQR.setOnClickListener { generateQR() }

        btnStartBLE.setOnClickListener {
            if (!Perms.ensureBleAdvertise(this)) return@setOnClickListener

            val payload = "SOMA|TX|$amount".toByteArray()
            tvStatus.text = "در حال انتشار BLE…"
            bleService.startAdvertising(
                context = this,
                payload = payload,
                onStart = { runOnUiThread { tvStatus.text = "فعال شد و در حال انتشار است BLE" } },
                onFail = { code -> runOnUiThread { tvStatus.text = "BLE خطا: $code" } }
            )
        }

        btnStopBLE.setOnClickListener {
            bleService.stopAdvertising()
            tvStatus.text = "BLE متوقف شد"
        }
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
        tvStatus.text = "ساخته شد: QR"
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService.stopAdvertising()
    }
}
