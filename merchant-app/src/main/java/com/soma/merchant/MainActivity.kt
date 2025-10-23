package com.soma.merchant

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.soma.merchant.databinding.ActivityMainBinding
import java.util.EnumMap

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtStatus.text = "وضعیت: آماده"

        binding.btnGenerateQR.setOnClickListener {
            val amountStr = binding.editAmount.text?.toString()?.trim().orEmpty()
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateQr(amountStr)
        }

        // دکمه‌های BLE فقط پیام وضعیت می‌دهند (ماک تا مرحله BLE واقعی)
        binding.btnBleStart.setOnClickListener {
            binding.txtStatus.text = "وضعیت: BLE شروع شد"
        }
        binding.btnBleStop.setOnClickListener {
            binding.txtStatus.text = "وضعیت: BLE متوقف شد"
        }
    }

    private fun generateQr(amount: String) {
        try {
            // Payload ساده خرید (می‌توانید بعداً ساختار بدهید)
            val payload = "PURCHASE|AMOUNT=$amount"

            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 0)
            }

            val size = 512
            val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }

            binding.imageQR.setImageBitmap(bmp)
            binding.txtStatus.text = "وضعیت: QR ساخته شد"
        } catch (e: Exception) {
            binding.txtStatus.text = "خطا در ساخت QR"
            Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
