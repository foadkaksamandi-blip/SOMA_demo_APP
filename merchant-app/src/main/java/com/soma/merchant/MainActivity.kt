package com.soma.merchant

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import shared.utils.DateUtils
import shared.utils.QRCodec
import shared.utils.QRPayload

class MainActivity : AppCompatActivity() {

    private lateinit var editAmount: EditText
    private lateinit var btnGenerateQR: Button
    private lateinit var imgQr: ImageView
    private lateinit var txtStatus: TextView
    // دکمه‌های BLE (بدون تغییر رفتار)
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // حتماً Theme.AppCompat در مانیفست/استایل ست شده باشد
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views (IDها را با لایهٔ خودت هماهنگ کن)
        editAmount   = findViewById(R.id.editAmount)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        imgQr        = findViewById(R.id.imgQr)
        txtStatus    = findViewById(R.id.txtStatus)
        btnBleStart  = findViewById(R.id.btnBleStart)
        btnBleStop   = findViewById(R.id.btnBleStop)

        btnGenerateQR.setOnClickListener { generateStandardQr() }

        // رفتار فعلی BLE دست‌نخورده بماند (در صورت داشتن کد)
        btnBleStart.setOnClickListener {
            txtStatus.text = "BLE: شروع شد"
            // TODO: BLE start (کد فعلی پروژه)
        }
        btnBleStop.setOnClickListener {
            txtStatus.text = "BLE: متوقف شد"
            // TODO: BLE stop (کد فعلی پروژه)
        }
    }

    private fun generateStandardQr() {
        val amountStr = editAmount.text?.toString()?.trim().orEmpty()
        val amount = amountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L

        val payload = QRPayload(
            amount = amount,
            txId = DateUtils.generateTxId(),
            createdAt = DateUtils.nowJalaliDateTime()
        )

        val qrText = QRCodec.encodeToQrText(payload)

        try {
            val writer = QRCodeWriter()
            val size = 800 // px
            val bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, size, size, null)

            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            imgQr.setImageBitmap(bmp)
            txtStatus.text = "QR ساخته شد (TX=${payload.txId})"
        } catch (e: Exception) {
            txtStatus.text = "خطا در ساخت QR"
        }
    }
}
