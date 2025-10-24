package com.soma.merchant.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Hashtable

/**
 * تولید QR و ساخت payload پرداخت.
 */
object QRHandler {

    /** ساخت متن پرداخت استاندارد ما */
    fun buildPaymentContent(amount: Int): String =
        "SOMA|TX|$amount|${System.currentTimeMillis()}"

    /** تولید QR بیت‌مپ از هر متن */
    fun generate(content: String, size: Int = 600): Bitmap {
        val hints = Hashtable<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (y in 0 until size) {
            for (x in 0 until size) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
