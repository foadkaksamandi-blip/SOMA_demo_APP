package com.soma.merchant.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRHandler {
    fun renderTo(target: ImageView, text: String, size: Int = 600) {
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        target.setImageBitmap(bmp)
    }
}
