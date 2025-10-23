package com.soma.merchant

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import shared.utils.DateUtils // از پوشه‌ی shared خودت

class MainActivity : AppCompatActivity() {

    private lateinit var editAmount: EditText
    private lateinit var btnGenerateQR: Button
    private lateinit var imageQR: ImageView      // ← مطابق XML شما
    private lateinit var txtStatus: TextView
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // تم AppCompat را در مانفیست ست کرده‌ای؛ پس نیازی به تغییر نیست
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind ها مطابق XML
        editAmount = findViewById(R.id.editAmount)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        imageQR = findViewById(R.id.imageQR)
        txtStatus = findViewById(R.id.txtStatus)
        btnBleStart = findViewById(R.id.btnBleStart)
        btnBleStop = findViewById(R.id.btnBleStop)

        txtStatus.text = "وضعیت: آماده"

        btnGenerateQR.setOnClickListener { generateQrClicked() }

        // BLE فعلاً ماک/غیرفعال
        btnBleStart.setOnClickListener { txtStatus.text = "وضعیت: BLE شروع شد" }
        btnBleStop.setOnClickListener { txtStatus.text = "وضعیت: BLE متوقف شد" }
    }

    private fun generateQrClicked() {
        val amountText = editAmount.text?.toString()?.trim().orEmpty()
        val amount = amountText.toLongOrNull()
        if (amount == null || amount <= 0L) {
            Toast.makeText(this, "مبلغ معتبر وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        // تولید شناسه تراکنش پایدار با util خودت
        val txId = DateUtils.generateTxId()  // مثلا SOMA-YYMMDD-HHMMSS-XXXX-CC
        val payload = """{
            "type":"PURCHASE",
            "txId":"$txId",
            "amount":$amount,
            "ts":"${DateUtils.nowJalaliDateTime()}",
            "merchant":"DEMO"
        }""".trimIndent()

        // ساخت Bitmap QR
        val size = 720 // پیکسل
        val bitmap = makeQrBitmap(payload, size)
        imageQR.setImageBitmap(bitmap)
        txtStatus.text = "وضعیت: QR ساخته شد"
    }

    private fun makeQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
