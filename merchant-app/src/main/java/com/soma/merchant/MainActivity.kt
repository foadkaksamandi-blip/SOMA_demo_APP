package com.soma.merchant

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // دکمه ساخت QR
        binding.btnGenerateQr.setOnClickListener {
            val data = "SOMA-DEMO"
            val bmp = generateDummyQr(data)
            binding.ivQr.setImageBitmap(bmp)
        }
    }

    /** یک QR سادهٔ نمایشی تا بیلد خطا نده (بعداً می‌تونیم ZXing اضافه کنیم) */
    private fun generateDummyQr(text: String): Bitmap {
        val size = 512
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        // الگوی ساده شطرنجی برای اینکه خروجی دیده شود
        for (y in 0 until size) {
            for (x in 0 until size) {
                val on = ((x / 16) + (y / 16)) % 2 == 0
                bmp.setPixel(x, y, if (on) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
